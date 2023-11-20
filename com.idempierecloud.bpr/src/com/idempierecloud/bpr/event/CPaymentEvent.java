package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MPayment;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class CPaymentEvent extends CustomEvent {

	
	private static CLogger log = CLogger.getCLogger(MPayment.class);
	
	private MPayment payment = null;
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Payment Event : "+event.getTopic());
		
		payment = (MPayment) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			setBankAccount();
		}else if (event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			setBankAccount();
		}else if (event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			setIsPrepayment();
		}else if (event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			setCreditUsed();
		}
	}
	
	private void setCreditUsed() {
		MBPartner bpartner = new MBPartner(payment.getCtx(), payment.getC_BPartner_ID(), payment.get_TrxName());
		BigDecimal creditUsed = DB.getSQLValueBD(payment.get_TrxName(), "SELECT calculate_credituse(?)", payment.getC_BPartner_ID());            
		bpartner.setSO_CreditUsed(creditUsed);
		bpartner.saveEx();
	}
	
	private void setIsPrepayment() { /*Ticket #request-001160 [BPR] setIsPrepayment pada prepayment*/
		int C_DocType_ID_Prepayment = DB.getSQLValue(payment.get_TrxName(), "Select C_DocType_ID from C_Doctype where name like 'Prepayment'");
		int C_DocType_ID_Kasbon = DB.getSQLValue(payment.get_TrxName(), "Select C_DocType_ID from C_Doctype where name like 'Kasbon'");
		
		if((payment.getC_DocType_ID()==C_DocType_ID_Prepayment)||(payment.getC_DocType_ID()==C_DocType_ID_Kasbon)){
			payment.setIsPrepayment(true);
		}
	}
	private void setBankAccount() {
		if(!payment.get_ValueAsBoolean("isReceipt"))
			return;
		int POSOrder = DB.getSQLValue(payment.get_TrxName(), "select 1 from c_order co join c_orderline co2 on co2.c_order_id = co.c_order_id join m_inoutline mi on mi.c_orderline_id = co2.c_orderline_id "
				+ "  join m_inout mi2 on mi2.m_inout_id = mi.m_inout_id join c_invoiceline ci on mi.m_inoutline_id = ci.m_inoutline_id "
				+ "  join c_invoice ci2 on ci2.c_invoice_id = ci.c_invoice_id join c_payment cp on cp.c_invoice_id = ci2.c_invoice_id "
				+ "  where cp.c_invoice_id = ? and co.IsSOTrx='Y' and co.C_DocTypeTarget_ID in (select dt.C_DocType_ID from C_DocType dt where dt.DocSubTypeSO IN ('WR') AND dt.IsSoTrx='Y')", payment.getC_Invoice_ID());
		int C_BankAccount_ID = DB.getSQLValue(payment.get_TrxName(), "select coalesce(c_bankaccount_id, 0) from c_bankaccount bc where bc.bankaccounttype = 'B' AND bc.AD_OrG_id = ? AND bc.isdefault ='Y'", payment.getC_Invoice().getAD_Org_ID());
		//if POS Order
		if(POSOrder > 0 ) {
			payment.setAD_Org_ID(payment.getC_Invoice().getAD_Org_ID());
			payment.setC_BankAccount_ID(C_BankAccount_ID);
		}
	}
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
