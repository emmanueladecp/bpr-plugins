package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MSysConfig;
import org.compiere.model.MTax;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.globalqss.model.MLCOInvoiceWithholding;
import org.globalqss.model.X_LCO_WithholdingCalc;
import org.globalqss.model.X_LCO_WithholdingRule;
import org.globalqss.model.X_LCO_WithholdingRuleConf;
import org.globalqss.model.X_LCO_WithholdingType;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRHistoryFakturPajak;
import com.idempierecloud.bpr.model.MBPRListFakturPajak;

public class CInvoiceEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	
	private MInvoice invoice = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("invoice Event : "+event.getTopic());
		
		invoice = (MInvoice) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID))
			checkFaktur();
		else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			setFaktur();
			checkDocStatusShipment();
			withholding();
		}
	}

	private void checkFaktur() {
		if(invoice.get_ValueAsInt("BPR_ListFakturPajak_ID")==0)
			return;
		
		int history = DB.getSQLValue(invoice.get_TrxName(), "SELECT BPR_HistoryFakturPajak_ID FROM BPR_HistoryFakturPajak WHERE BPR_ListFakturPajak_ID=? AND C_Invoice_ID=? AND isUploaded='Y'", invoice.get_ValueAsInt("BPR_ListFakturPajak_ID"), invoice.getC_Invoice_ID());
		if(history>0)
			throw new AdempiereException("Faktur Pajak telah diupload. Invoice tidak bisa dibatalkan");
	}

	private void checkDocStatusShipment() {
		if(!invoice.get_ValueAsBoolean("isSOTrx"))
			return;
		MInvoiceLine[] lines = invoice.getLines();
		for(MInvoiceLine line:lines) {
			if(line.getM_InOutLine_ID()>0) {
				MInOut shipment = (MInOut) line.getM_InOutLine().getM_InOut();
				if(shipment.getDocStatus().equalsIgnoreCase("CO"))
					return;
				else if(shipment.getDocStatus().equalsIgnoreCase("CL"))
					return;
				else
					throw new AdempiereException("Shipment Document No : "+shipment.getDocumentNo()+" pada Invoice Line No "+ line.getLine()+" Belum complete!");
			}
		}
	}
	private void setFaktur() {
		if(!invoice.isSOTrx() || invoice.get_ValueAsString("TypePajak")==null || invoice.get_ValueAsInt("BPR_ListFakturPajak_ID")>0)
			return;
		
		MBPRListFakturPajak pajak = MBPRListFakturPajak.getNext(invoice);
		if(pajak==null)
			throw new AdempiereException("Tidak ada nomor faktur pajak yang tersedia");
		
		invoice.set_ValueOfColumn("BPR_ListFakturPajak_ID", pajak.getBPR_ListFakturPajak_ID());
		invoice.saveEx();
		
		MBPRHistoryFakturPajak.addHistory(invoice, pajak);
	}
	
	public void withholding() {
		MInvoiceLine[] lines = invoice.getLines(false);
		for(MInvoiceLine line : lines) {
			MOrderLine orderline = (MOrderLine)line.getC_OrderLine(); 
			if(orderline.get_ValueAsInt("LCO_WithholdingType_ID")>0) {
				recalcWithholdings(orderline.get_ValueAsInt("LCO_WithholdingType_ID"));
			}
		}
	}
	
	public void recalcWithholdings(int LCO_WithholdingType_ID) {
		
		int noins = 0;
		log.info("");
		BigDecimal totwith = new BigDecimal("0");

		
		// Search withholding types applicable depending on IsSOTrx
		List<X_LCO_WithholdingType> wts = new Query(invoice.getCtx(), X_LCO_WithholdingType.Table_Name, "LCO_WithholdingType_ID=?", invoice.get_TrxName())
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.setParameters(LCO_WithholdingType_ID)
			.list();
		for (X_LCO_WithholdingType wt : wts)
		{
			// For each applicable withholding
			log.info("Withholding Type: "+wt.getLCO_WithholdingType_ID()+"/"+wt.getName());

			X_LCO_WithholdingRuleConf wrc = new Query(invoice.getCtx(),X_LCO_WithholdingRuleConf.Table_Name,"LCO_WithholdingType_ID=?",invoice.get_TrxName())
					.setOnlyActiveRecords(true)
					.setParameters(wt.getLCO_WithholdingType_ID())
					.first();
			if (wrc == null) {
				log.warning("No LCO_WithholdingRuleConf for LCO_WithholdingType = "+wt.getLCO_WithholdingType_ID());
				continue;
			}

			// look for applicable rules according to config fields (rule)
			StringBuffer wherer = new StringBuffer(" LCO_WithholdingType_ID=? AND ValidFrom<=? ");
			List<Object> paramsr = new ArrayList<Object>();
			paramsr.add(wt.getLCO_WithholdingType_ID());
			paramsr.add(invoice.getDateInvoiced());
			
			List<X_LCO_WithholdingRule> wrs = new Query(invoice.getCtx(), X_LCO_WithholdingRule.Table_Name, wherer.toString(), invoice.get_TrxName())
				.setOnlyActiveRecords(true)
				.setParameters(paramsr)
				.list();
			for (X_LCO_WithholdingRule wr : wrs)
			{
				// for each applicable rule
				// bring record for withholding calculation
				X_LCO_WithholdingCalc wc = (X_LCO_WithholdingCalc) wr.getLCO_WithholdingCalc();
				if (wc == null || wc.getLCO_WithholdingCalc_ID() == 0) {
					log.severe("Rule without calc " + wr.getLCO_WithholdingRule_ID());
					continue;
				}

				// bring record for tax
				MTax tax = new MTax(invoice.getCtx(), wc.getC_Tax_ID(), invoice.get_TrxName());

				log.info("WithholdingRule: "+wr.getLCO_WithholdingRule_ID()+"/"+wr.getName()
						+" BaseType:"+wc.getBaseType()
						+" Calc: "+wc.getLCO_WithholdingCalc_ID()+"/"+wc.getName()
						+" CalcOnInvoice:"+wc.isCalcOnInvoice()
						+" Tax: "+tax.getC_Tax_ID()+"/"+tax.getName());

				// calc base
				// apply rule to calc base
				BigDecimal base = null;

				if (wc.getBaseType() == null) {
					log.severe("Base Type null in calc record "+wr.getLCO_WithholdingCalc_ID());
				} else if (wc.getBaseType().equals(X_LCO_WithholdingCalc.BASETYPE_Document)) {
					base = invoice.getTotalLines();
				} else if (wc.getBaseType().equals(X_LCO_WithholdingCalc.BASETYPE_Line)) {
					List<Object> paramslca = new ArrayList<Object>();
					paramslca.add(invoice.getC_Invoice_ID());
					String sqllca;
					if (wrc.isUseWithholdingCategory() && wrc.isUseProductTaxCategory()) {
						// base = lines of the withholding category and tax category
						sqllca =
							"SELECT SUM (LineNetAmt) "
							+ "  FROM C_InvoiceLine il "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? "
							+ "   AND (   EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM M_Product p "
							+ "               WHERE il.M_Product_ID = p.M_Product_ID "
							+ "                 AND p.C_TaxCategory_ID = ? "
							+ "                 AND p.LCO_WithholdingCategory_ID = ?) "
							+ "        OR EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM C_Charge c "
							+ "               WHERE il.C_Charge_ID = c.C_Charge_ID "
							+ "                 AND c.C_TaxCategory_ID = ? "
							+ "                 AND c.LCO_WithholdingCategory_ID = ?) "
							+ "       ) ";
						paramslca.add(wr.getC_TaxCategory_ID());
						paramslca.add(wr.getLCO_WithholdingCategory_ID());
						paramslca.add(wr.getC_TaxCategory_ID());
						paramslca.add(wr.getLCO_WithholdingCategory_ID());
					} else if (wrc.isUseWithholdingCategory()) {
						// base = lines of the withholding category
						sqllca =
							"SELECT SUM (LineNetAmt) "
							+ "  FROM C_InvoiceLine il "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? "
							+ "   AND (   EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM M_Product p "
							+ "               WHERE il.M_Product_ID = p.M_Product_ID "
							+ "                 AND p.LCO_WithholdingCategory_ID = ?) "
							+ "        OR EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM C_Charge c "
							+ "               WHERE il.C_Charge_ID = c.C_Charge_ID "
							+ "                 AND c.LCO_WithholdingCategory_ID = ?) "
							+ "       ) ";
						paramslca.add(wr.getLCO_WithholdingCategory_ID());
						paramslca.add(wr.getLCO_WithholdingCategory_ID());
					} else if (wrc.isUseProductTaxCategory()) {
						// base = lines of the product tax category
						sqllca =
							"SELECT SUM (LineNetAmt) "
							+ "  FROM C_InvoiceLine il "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? "
							+ "   AND (   EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM M_Product p "
							+ "               WHERE il.M_Product_ID = p.M_Product_ID "
							+ "                 AND p.C_TaxCategory_ID = ?) "
							+ "        OR EXISTS ( "
							+ "              SELECT 1 "
							+ "                FROM C_Charge c "
							+ "               WHERE il.C_Charge_ID = c.C_Charge_ID "
							+ "                 AND c.C_TaxCategory_ID = ?) "
							+ "       ) ";
						paramslca.add(wr.getC_TaxCategory_ID());
						paramslca.add(wr.getC_TaxCategory_ID());
					} else {
						// base = all lines
						sqllca =
							"SELECT SUM (LineNetAmt) "
							+ "  FROM C_InvoiceLine il "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? ";
					}
					base = DB.getSQLValueBD(invoice.get_TrxName(), sqllca, paramslca);
				} else if (wc.getBaseType().equals(X_LCO_WithholdingCalc.BASETYPE_Tax)) {
					// if specific tax
					if (wc.getC_BaseTax_ID() != 0) {
						// base = value of specific tax
						String sqlbst = "SELECT SUM(TaxAmt) "
							+ " FROM C_InvoiceTax "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? "
							+ "   AND C_Tax_ID = ?";
						base = DB.getSQLValueBD(invoice.get_TrxName(), sqlbst, new Object[] {invoice.getC_Invoice_ID(), wc.getC_BaseTax_ID()});
					} else {
						// not specific tax
						// base = value of all taxes
						String sqlbsat = "SELECT SUM(TaxAmt) "
							+ " FROM C_InvoiceTax "
							+ " WHERE IsActive='Y' AND C_Invoice_ID = ? ";
						base = DB.getSQLValueBD(invoice.get_TrxName(), sqlbsat, new Object[] {invoice.getC_Invoice_ID()});
					}
				}
				log.info("Base: "+base+ " Thresholdmin:"+wc.getThresholdmin());

				// if base between thresholdmin and thresholdmax inclusive
				// if thresholdmax = 0 it is ignored
				if (base != null &&
						base.compareTo(Env.ZERO) != 0 &&
						base.compareTo(wc.getThresholdmin()) >= 0 &&
						(wc.getThresholdMax() == null || wc.getThresholdMax().compareTo(Env.ZERO) == 0 || base.compareTo(wc.getThresholdMax()) <= 0) &&
						tax.getRate() != null &&
						tax.getRate().compareTo(Env.ZERO) != 0) {

					// insert new withholding record
					// with: type, tax, base amt, percent, tax amt, trx date, acct date, rule
					MLCOInvoiceWithholding iwh = new MLCOInvoiceWithholding(invoice.getCtx(), 0, invoice.get_TrxName());
					iwh.setAD_Org_ID(invoice.getAD_Org_ID());
					iwh.setC_Invoice_ID(invoice.getC_Invoice_ID());
					iwh.setDateAcct(invoice.getDateAcct());
					iwh.setDateTrx(invoice.getDateInvoiced());
					iwh.setIsCalcOnPayment( ! wc.isCalcOnInvoice() );
					iwh.setIsTaxIncluded(false);
					iwh.setLCO_WithholdingRule_ID(wr.getLCO_WithholdingRule_ID());
					iwh.setLCO_WithholdingType_ID(wt.getLCO_WithholdingType_ID());
					iwh.setC_Tax_ID(tax.getC_Tax_ID());
					iwh.setPercent(tax.getRate());
					iwh.setProcessed(false);
					int stdPrecision = MPriceList.getStandardPrecision(invoice.getCtx(), invoice.getM_PriceList_ID());
					BigDecimal taxamt = tax.calculateTax(base, false, stdPrecision);
					if (wc.getAmountRefunded() != null &&
							wc.getAmountRefunded().compareTo(Env.ZERO) > 0) {
						taxamt = taxamt.subtract(wc.getAmountRefunded());
					}
					iwh.setTaxAmt(taxamt);
					iwh.setTaxBaseAmt(base);
					if (    (  invoice.isSOTrx() && MSysConfig.getBooleanValue("QSSLCO_GenerateWithholdingInactiveSO", false, invoice.getAD_Client_ID(), invoice.getAD_Org_ID()) )
						 || ( !invoice.isSOTrx() && MSysConfig.getBooleanValue("QSSLCO_GenerateWithholdingInactivePO", false, invoice.getAD_Client_ID(), invoice.getAD_Org_ID()) )) {
						iwh.setIsActive(false);
					}
					iwh.saveEx();
					totwith = totwith.add(taxamt);
					noins++;
					log.info("LCO_InvoiceWithholding saved:"+iwh.getTaxAmt());
				}
			} // while each applicable rule

		} // while type
		updateHeaderWithholding(invoice.getC_Invoice_ID(), invoice.get_TrxName());
		invoice.saveEx();

		if (noins == -1)
			throw new AdempiereUserError("Error calculating withholding, please check log");
	}

	/**
	 *	Update Withholding in Header
	 *	@return true if header updated with withholding
	 */
	public static boolean updateHeaderWithholding(int C_Invoice_ID, String trxName)
	{
		//	Update Invoice Header
		String sql =
			"UPDATE C_Invoice "
			+ " SET WithholdingAmt="
				+ "(SELECT COALESCE(SUM(TaxAmt),0) FROM LCO_InvoiceWithholding iw WHERE iw.IsActive = 'Y' " +
						"AND iw.IsCalcOnPayment = 'N' AND C_Invoice.C_Invoice_ID=iw.C_Invoice_ID) "
			+ "WHERE C_Invoice_ID=?";
		int no = DB.executeUpdateEx(sql, new Object[] {C_Invoice_ID}, trxName);

		return no == 1;
	}	//	updateHeaderWithholding

	
	@Override
	protected void doHandleEvent() {
		
	}

}
