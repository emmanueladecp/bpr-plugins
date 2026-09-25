package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MCurrency;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MMatchPO;
import org.compiere.model.MOrderLine;
import org.compiere.model.MTax;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRHistoryFakturPajak;
import com.idempierecloud.bpr.model.MBPRListFakturPajak;

public class CInvoiceEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	
	private MInvoice invoice = null;
	private final static int M_LocatorType_CustomerShipment = 1000002;
	private final static int C_Doctype_AR_CreditMemo = 1000004;
	//private final static int C_CHARGE_ID_PPN_KELUARAN_2501009 = 1000344;
	private final static int C_CHARGE_ID_PENJUALAN_OA = 1000429;
	private final static int C_CHARGE_ID_PENJUALAN_KEMASAN = 1000430;
	private final static int C_TAX_RATE_11 = 1000003;
	private final static int C_Doctype_AR_Invoice_Customer = 1000002;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("invoice Event : "+event.getTopic());
		
		invoice = (MInvoice) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID))
			checkFaktur();
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			checkFaktur();
		}	
		else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			checkHeaderWithRelatedDocument();
			checkMovementDate();
			checkqtyShipment();
			checkDocStatusShipment();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {


		}
		else if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			checkProductType();
			setCreditUsed();
		}
		else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_PREPARE)) {
			
			if (!invoice.isSOTrx())
		        return;
			
			//if (invoice.getC_DocTypeTarget_ID() != C_Doctype_AR_Invoice_Customer )
		    //    return;
			
			if (invoice.isReversal())
			    return;
			
			if (invoice.getC_DocTypeTarget_ID() == C_Doctype_AR_Invoice_Customer || invoice.getC_DocTypeTarget_ID() == C_Doctype_AR_CreditMemo) {
				calculateAdditionalCharge(invoice);
			}
			 
		} 
	}
	
	
	 private void calculateAdditionalCharge(MInvoice invoice)
    {
        // ============================================================
        // 1. Hitung total OA + Subsidi dalam kondisi GROSS
        //    karena nilai OA/Subsidi sudah termasuk PPN 11%
        // ============================================================

        //BigDecimal grossAdditionalValue = BigDecimal.	ZERO;
        BigDecimal lineOngkosAngkut = BigDecimal.ZERO;
        BigDecimal lineSubsidi = BigDecimal.ZERO;

        MInvoiceLine[] lines = invoice.getLines(true);

        for (MInvoiceLine invoiceLine : lines)
        {
            // Jangan ikut menghitung line tambahan kita sendiri
            if (isAdditionalChargeLine(invoiceLine, "IsAdditionalCharge"))
                continue;
            
            if (isAdditionalChargeLine(invoiceLine, "IsAdditionalOA"))
                continue;
            
            if (isAdditionalChargeLine(invoiceLine, "IsAdditionalSubsidi"))
                continue;

            BigDecimal qtyKg = invoiceLine.getQtyInvoiced();

            BigDecimal ongkosAngkut =
                (BigDecimal) invoiceLine.get_Value("OngkosAngkut");

            BigDecimal subsidiAmt =
                (BigDecimal) invoiceLine.get_Value("SubsidiAmt");

            if (qtyKg == null)
                qtyKg = BigDecimal.ZERO;

            if (ongkosAngkut == null)
                ongkosAngkut = BigDecimal.ZERO;

            if (subsidiAmt == null)
                subsidiAmt = BigDecimal.ZERO;

            lineOngkosAngkut = lineOngkosAngkut
                    .add(ongkosAngkut.multiply(qtyKg));

            lineSubsidi = lineSubsidi
                    .add(subsidiAmt.multiply(qtyKg));

            //grossAdditionalValue = grossAdditionalValue
            //    .add(lineOngkosAngkut)
            //    .add(lineSubsidi);
        }

        // ============================================================
        // 2. Kalau <= 0, hapus/nonaktifkan line tambahan
        // ============================================================

        //if (grossAdditionalValue.compareTo(BigDecimal.ZERO) <= 0)
        //{
        //    removeAdditionalChargeLine(invoice);
        //    return;
        //}
        
        if (lineOngkosAngkut.compareTo(BigDecimal.ZERO) <= 0)
        {
        	removeAdditionalChargeLine(invoice, "IsAdditionalOA");
        }
        
        if (lineSubsidi.compareTo(BigDecimal.ZERO) <= 0)
        {
        	removeAdditionalChargeLine(invoice, "IsAdditionalSubsidi");
        }

        // ============================================================
        // 3. Currency precision
        // ============================================================

        MCurrency currency = MCurrency.get(
            invoice.getCtx(),
            invoice.getC_Currency_ID()
        );

        int precision = currency.getStdPrecision();

        // ============================================================
        // 4. Ambil tax
        // ============================================================

        MTax tax = MTax.get(
            invoice.getCtx(),
            C_TAX_RATE_11
        );

        BigDecimal taxRate = tax.getRate();

        // ============================================================
        // 5. Karena gross sudah termasuk PPN:
        //
        // DPP = Gross / (1 + TaxRate/100)
        // ============================================================

        BigDecimal divisor = BigDecimal.ONE.add(
            taxRate.divide(
                BigDecimal.valueOf(100),
                10,
                RoundingMode.HALF_UP
            )
        );

        //BigDecimal dpp = grossAdditionalValue.divide(
        //    divisor,
        //    precision,
        //    RoundingMode.HALF_UP
        //);
        
        BigDecimal dppOA = lineOngkosAngkut.divide(
                divisor,
                precision,
                RoundingMode.HALF_UP
        );
        
        BigDecimal dppSubsidi = lineSubsidi.divide(
                divisor,
                precision,
                RoundingMode.HALF_UP
        );

        // ============================================================
        // 6. Cari existing additional charge line
        // ============================================================

        if (lineOngkosAngkut.compareTo(BigDecimal.ZERO) > 0)
        {
        	MInvoiceLine additionalLine =
                    findAdditionalChargeLine(invoice,"IsAdditionalOA");

                if (additionalLine == null)
                {
                    additionalLine = new MInvoiceLine(
                        invoice.getCtx(),
                        0,
                        invoice.get_TrxName()
                    );

                    int lineNo = getNextLineNo(invoice);

                    additionalLine.setAD_Org_ID(invoice.getAD_Org_ID());
                    additionalLine.setC_Invoice_ID(invoice.getC_Invoice_ID());
                    additionalLine.setLine(lineNo);

                    additionalLine.setC_Charge_ID(
                    		C_CHARGE_ID_PENJUALAN_OA
                    );

                    additionalLine.setQtyEntered(
                        BigDecimal.ONE
                    );

                    additionalLine.setQtyInvoiced(
                        BigDecimal.ONE
                    );
                }

                // ============================================================
                // 7. Set DPP sebagai Price
                //
                // Karena PriceList IsTaxIncluded = N
                // ============================================================

                additionalLine.setC_Tax_ID(
                		C_TAX_RATE_11
                );

                additionalLine.setPriceList(dppOA);
                additionalLine.setPrice(dppOA);
                additionalLine.setPriceEntered(dppOA);
                
                additionalLine.set_ValueOfColumn("IsAdditionalOA", true);
                additionalLine.set_ValueOfColumn("IsAdditionalCharge", true);

                additionalLine.saveEx();
        }
        
        if (lineSubsidi.compareTo(BigDecimal.ZERO) > 0)
        {
        	MInvoiceLine additionalLine =
                    findAdditionalChargeLine(invoice,"IsAdditionalSubsidi");

                if (additionalLine == null)
                {
                    additionalLine = new MInvoiceLine(
                        invoice.getCtx(),
                        0,
                        invoice.get_TrxName()
                    );

                    int lineNo = getNextLineNo(invoice);

                    additionalLine.setAD_Org_ID(invoice.getAD_Org_ID());
                    additionalLine.setC_Invoice_ID(invoice.getC_Invoice_ID());
                    additionalLine.setLine(lineNo);

                    additionalLine.setC_Charge_ID(
                    		C_CHARGE_ID_PENJUALAN_KEMASAN
                    );

                    additionalLine.setQtyEntered(
                        BigDecimal.ONE
                    );

                    additionalLine.setQtyInvoiced(
                        BigDecimal.ONE
                    );
                }

                // ============================================================
                // 7. Set DPP sebagai Price
                //
                // Karena PriceList IsTaxIncluded = N
                // ============================================================

                additionalLine.setC_Tax_ID(
                		C_TAX_RATE_11
                );

                additionalLine.setPriceList(dppSubsidi);
                additionalLine.setPrice(dppSubsidi);
                additionalLine.setPriceEntered(dppSubsidi);
                
                additionalLine.set_ValueOfColumn("IsAdditionalSubsidi", true);
                additionalLine.set_ValueOfColumn("IsAdditionalCharge", true);

                additionalLine.saveEx();
        }
    }

    private boolean isAdditionalChargeLine(
        MInvoiceLine line, String additionalCharge)
    {
        return line.get_ValueAsBoolean(additionalCharge);
    }

    private MInvoiceLine findAdditionalChargeLine(
        MInvoice invoice, String additionalCharge)
    {
        for (MInvoiceLine line : invoice.getLines(true))
        {
            if (isAdditionalChargeLine(line, additionalCharge))
                return line;
        }

        return null;
    }

    private void removeAdditionalChargeLine(
        MInvoice invoice, String additionalCharge)
    {
        MInvoiceLine line =
            findAdditionalChargeLine(invoice, additionalCharge);

        if (line != null)
        {
            line.deleteEx(true);
        }
    }

    private int getNextLineNo(MInvoice invoice)
    {
        int maxLine = 0;

        for (MInvoiceLine line : invoice.getLines(true))
        {
            if (line.getLine() > maxLine)
                maxLine = line.getLine();
        }

        return maxLine + 10;
    }
	
	private void checkProductType() {
		if(!invoice.isSOTrx()) {
			for(MInvoiceLine invoiceLine: invoice.getLines()) {
				 StringBuilder sql = new StringBuilder ("select mm.m_matchpo_id from c_invoiceline ci "
				 		+ "	join m_matchpo mm on ci.c_invoiceline_id = mm.c_invoiceline_id "
				 		+ "	where mm.c_orderline_id = ? and ci.c_invoiceline_id =?");
					PreparedStatement pstmnt = null;
					ResultSet rsl = null;
					try
					{
						pstmnt = DB.prepareStatement (sql.toString(), invoice.get_TrxName());
						int index = 1; 
			            pstmnt.setInt(index++, invoiceLine.getC_OrderLine_ID());
			            pstmnt.setInt(index++, invoiceLine.getC_InvoiceLine_ID());
						rsl = pstmnt.executeQuery ();
						while (rsl.next ()){
							MMatchPO mpo = new MMatchPO(invoiceLine.getCtx(), rsl.getInt(1), invoiceLine.get_TrxName());							
							if(!mpo.getC_OrderLine().getC_Order().isSOTrx()) {
								if(!mpo.getM_Product().getProductType().equals("I")) {
									mpo.setRef_MatchPO_ID(mpo.get_ID());
									mpo.saveEx();
								}
							}
						}
					}
					catch (SQLException e)
					{
						log.log(Level.SEVERE, " CInvoiceEvent - " + sql.toString(), e);
					}
					finally
					{
						DB.close(rsl, pstmnt);
						rsl = null;
						pstmnt = null;
					}
			}
		}
	}
	
	private void setCreditUsed() {
		MBPartner bpartner = new MBPartner(invoice.getCtx(), invoice.getC_BPartner_ID(), invoice.get_TrxName());
		BigDecimal creditUsed = DB.getSQLValueBD(invoice.get_TrxName(), "SELECT calculate_credituse(?)", bpartner.getC_BPartner_ID());            
		bpartner.setSO_CreditUsed(creditUsed);
		bpartner.saveEx();
	}
	
	private void checkqtyShipment() {
		if(invoice.isSOTrx()) {
			for(MInvoiceLine invoiceLine : invoice.getLines(true)){
				if(invoiceLine.getM_InOutLine_ID()>0) {
					MInOutLine shipLine = (MInOutLine) invoiceLine.getM_InOutLine();
					if(shipLine.getMovementQty().compareTo(invoiceLine.getQtyInvoiced())!=0) {
						if(shipLine.getM_Locator().getM_LocatorType_ID()==M_LocatorType_CustomerShipment) {
							throw new AdempiereException("Qty Shipment tidak sama dengan Qty Invoice,"
									+ " Qty Shipment : "+shipLine.getQtyEntered()
									+ " Qty Invoice  : "+invoiceLine.getQtyEntered()
									+ " Product : "+invoiceLine.getM_Product().getName());
						}
					}	
				}
			}
		}
	}
	
	private void checkMovementDate() {
		if(invoice.isSOTrx()){
			if(invoice.getReversal_ID()>0)
				return;
			Date DateAcc = invoice.getDateAcct();
		    int monthInv = DateAcc.getMonth();
		    StringBuilder sql = new StringBuilder ("select distinct mi.m_inout_id from c_invoiceline ci "
		    		+ "	join c_invoice ci2 on ci.c_invoice_id = ci2.c_invoice_id  "
		    		+ "	left join m_inoutline mi on ci.m_inoutline_id = mi.m_inoutline_id "
		    		+ "	where ci2.issotrx = 'Y' and ci2.c_invoice_id = ? and mi.m_inout_id>0");
			PreparedStatement pstmnt = null;
			ResultSet rsl = null;
			try
			{
				pstmnt = DB.prepareStatement (sql.toString(), invoice.get_TrxName());
				int index = 1; 
	            pstmnt.setInt(index++, invoice.getC_Invoice_ID());
				rsl = pstmnt.executeQuery ();
				while (rsl.next ()){
					int inout =  rsl.getInt(1);
					if(inout>0) {
						MInOut shipment = new MInOut(invoice.getCtx(), rsl.getInt(1), invoice.get_TrxName());
						Date DateAcc2 = shipment.getDateAcct();
						int mountShp = DateAcc2.getMonth();
						if(mountShp!=monthInv) {
							throw new AdempiereException("Periode Invoice berbeda dengan Periode Shipment!");
						}
					}
				}
			}
			catch (SQLException e)
			{
				log.log(Level.SEVERE, " CInvoiceEvent - " + sql.toString(), e);
			}
			finally
			{
				DB.close(rsl, pstmnt);
				rsl = null;
				pstmnt = null;
			}
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
		/*
		 * //**for(MInvoiceLine line:lines) { if(line.getM_InOutLine_ID()>0) { MInOut
		 * shipment = (MInOut) line.getM_InOutLine().getM_InOut();
		 * if(shipment.getDocStatus().equalsIgnoreCase("CO")) return; else
		 * if(shipment.getDocStatus().equalsIgnoreCase("CL")) return; else throw new
		 * AdempiereException("Shipment Document No : "+shipment.getDocumentNo()
		 * +" pada Invoice Line No "+ line.getLine()+" Belum complete!"); } }
		 */
		
		for(MInvoiceLine line:lines) {
			if(line.getM_InOutLine_ID()>0) {
				MInOut shipment = (MInOut) line.getM_InOutLine().getM_InOut();
				if(shipment.getDocStatus().equalsIgnoreCase("CO"))
					continue;
				else if(shipment.getDocStatus().equalsIgnoreCase("CL"))
					continue;
				else
					throw new AdempiereException("Shipment Document No : "+shipment.getDocumentNo()+" pada Invoice Line No "+ line.getLine()+" Belum complete!");
			}
		}
	}
	
	private void checkHeaderWithRelatedDocument() {
		if(!invoice.get_ValueAsBoolean("isSOTrx"))
			return;
		
		if(invoice.isSOTrx()){
			if(invoice.getReversal_ID()>0)
				return;
		}
		
		//check dulu per lines nya wajib ambil dari Shipment
		int c_bpartner_id = invoice.getC_BPartner_ID();
		MInvoiceLine[] lines = invoice.getLines();
		
		boolean getFromOrderOnly = false;
		boolean getFromShipmentOnly = false;
		boolean getFromOrderAndShipment = false;
		
		for(MInvoiceLine line:lines) {
			// Jangan ikut menghitung line tambahan kita sendiri
            if (isAdditionalChargeLine(line, "IsAdditionalCharge"))
                continue;
            
            if (isAdditionalChargeLine(line, "IsAdditionalOA"))
                continue;
            
            if (isAdditionalChargeLine(line, "IsAdditionalSubsidi"))
                continue;
			
			if(line.getC_OrderLine_ID()>0 && line.getM_InOutLine_ID()>0) {
				getFromOrderAndShipment = true;
			} else {
				if(line.getM_InOutLine_ID()>0) {
					getFromShipmentOnly = true;
				} else {
					getFromOrderOnly = true;
					break;
				}
			}
		}
		
		if (getFromOrderOnly) {
			throw new AdempiereException("Dokumen Invoice tidak ada pasangan dokumen SJ");
		}
		
		
		//int c_bpartner_id = invoice.getC_BPartner_ID();
		//MInvoiceLine[] lines = invoice.getLines();
		boolean sameDataBp = true;
		
		for(MInvoiceLine line:lines) {
			if(line.getM_InOutLine_ID()>0) {
				MInOut shipment = (MInOut) line.getM_InOutLine().getM_InOut();
				int bp_lines = shipment.get_ValueAsInt("C_BPartner_ID");
				
				if (bp_lines != c_bpartner_id) {
					sameDataBp = false;
				}
			}
		}
		
		if (!sameDataBp) {
			throw new AdempiereException("BP Invoice Missmatch dengan BP Invoice Lines");
		}
		
	}

	
	@Override
	protected void doHandleEvent() {
		
	}

}
