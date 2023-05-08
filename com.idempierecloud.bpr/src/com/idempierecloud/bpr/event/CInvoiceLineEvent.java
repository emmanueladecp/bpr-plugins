package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.MUOMConversion;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class CInvoiceLineEvent extends CustomEvent {

private static CLogger log = CLogger.getCLogger(CInvoiceLineEvent.class);
	
	private MInvoiceLine invoiceLine = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("invoice line Event : "+event.getTopic());
		
		invoiceLine = (MInvoiceLine) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			setWitholdingType();
			setOngkosAngkut_SubsidiAmt();
			setQtyInvoice();
			setIfOrderlineFOC();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			setQtyInvoice();
			setIfOrderlineFOC();
			recalculatePriceActual();
		}else if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			setPaymentTermHeader();
		}
			
	}
	
	private void setPaymentTermHeader() {
		if(invoiceLine.getC_Invoice().isSOTrx()) {
			MInvoice invoice = (MInvoice) invoiceLine.getC_Invoice();
			MBPartner bp = (MBPartner) invoice.getC_BPartner();
			int count_corder = DB.getSQLValue(invoiceLine.get_TrxName(),"select count(distinct co.c_order_id) from c_invoiceline ci join c_orderline co on ci.c_orderline_id = co.c_orderline_id "
					+ "where ci.c_invoice_id = ?", invoiceLine.getC_Invoice_ID());
			if(count_corder>1) {
				if(bp.getC_PaymentTerm_ID()<=0)
					throw new AdempiereException("Invoice Terdiri lebih dari 1 SO, Tidak ditemukan Payment Term pada Business Partner");
				invoice.setC_PaymentTerm_ID(bp.getC_PaymentTerm_ID());
				invoice.saveEx();
			}else {
				MOrderLine oline = (MOrderLine) invoiceLine.getC_OrderLine();
				invoice.setC_PaymentTerm_ID(oline.getC_Order().getC_PaymentTerm_ID());
				invoice.saveEx();
			}
		}
	}
	private void recalculatePriceActual() {
		if(invoiceLine.getC_Invoice().isSOTrx()&&invoiceLine.is_ValueChanged("PriceList")) {
			BigDecimal OngkosAngkut = (BigDecimal) invoiceLine.get_Value("OngkosAngkut");
			BigDecimal SubsidiAmt = (BigDecimal) invoiceLine.get_Value("SubsidiAmt");
			BigDecimal priceActual = invoiceLine.getPriceList().add(OngkosAngkut).add(SubsidiAmt);
			invoiceLine.setPriceActual(priceActual);
			BigDecimal LineNetAmt = invoiceLine.getPriceActual().multiply(invoiceLine.getQtyInvoiced());	
			invoiceLine.setLineNetAmt(LineNetAmt);
		}
	}

	private void setIfOrderlineFOC() {
		if(invoiceLine.getC_OrderLine_ID()>0 && invoiceLine.getC_Invoice().isSOTrx()) {
			MOrderLine oline = (MOrderLine) invoiceLine.getC_OrderLine();
			if(oline.get_ValueAsBoolean("isFOC")) {
				invoiceLine.setPrice(BigDecimal.ZERO);
			}
		}
	}

	private void setQtyInvoice() {
		if(invoiceLine.getC_UOM_ID()!=invoiceLine.getM_Product().getC_UOM_ID()) {
			if(invoiceLine.getM_Product_ID()==0)
				return;
			BigDecimal qtyInvoice = MUOMConversion.convertProductFrom(invoiceLine.getCtx(), invoiceLine.getM_Product_ID(), invoiceLine.getC_UOM_ID(), invoiceLine.getQtyEntered());
			invoiceLine.setQtyInvoiced(qtyInvoice);
		}
	}

	private void setOngkosAngkut_SubsidiAmt() {
		if(invoiceLine.getC_OrderLine_ID()>0) {
			MOrderLine oLine = (MOrderLine) invoiceLine.getC_OrderLine();
			BigDecimal ongkosAngkut = (BigDecimal)oLine.get_Value("OngkosAngkut");
			BigDecimal SubsidiAmt = (BigDecimal)oLine.get_Value("SubsidiAmt");
			if(ongkosAngkut!=null) {
				if(ongkosAngkut.compareTo(BigDecimal.ZERO)>0 && ongkosAngkut!=null) 
					invoiceLine.set_ValueOfColumn("OngkosAngkut", ongkosAngkut);
			}
			if(SubsidiAmt!=null) {
				if(SubsidiAmt.compareTo(BigDecimal.ZERO)>0) 
					invoiceLine.set_ValueOfColumn("SubsidiAmt", SubsidiAmt);
			}
				
		}
	}

	private void setWitholdingType() {
		if(invoiceLine.getC_Invoice().isSOTrx() || invoiceLine.getC_OrderLine_ID()==0)
			return;
		
		MOrderLine orderLine = (MOrderLine) invoiceLine.getC_OrderLine();
		if(orderLine.get_ValueAsInt("LCO_WithholdingType_ID")>0)
			invoiceLine.set_ValueOfColumn("LCO_WithholdingType_ID", orderLine.get_ValueAsInt("LCO_WithholdingType_ID"));
	}
	
	

	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub

	}

}
