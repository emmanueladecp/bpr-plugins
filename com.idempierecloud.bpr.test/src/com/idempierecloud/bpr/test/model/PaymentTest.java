package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.compiere.model.MOrder;
import org.compiere.model.MPayment;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class PaymentTest extends AbstractTestCase {

	private static final int AD_ORG_ID_BPR1 = 1000003;
	private static final int AD_Org_ID_DepoBalikpapan=1000013;
	private static final int C_Doctype_Ar_Receipt = 1000008;
	private static final int C_Currency_ID=303;
	private static final int C_Invoice_ID_PosOrder=1000014;
	private static final int C_Bank_KASKARAWANG = 1000021;
	private static final int C_DocType_ID_Kasbon =1000087;
	private static final int C_BankAccount_ID_KAS_BALIKPAPAN=1000020;
	
	@Test
	public void test_set_bankaccount() throws Exception{
		MPayment payment = new MPayment(Env.getCtx(), 0, getTrxName());
		payment.setAD_Org_ID(AD_ORG_ID_BPR1);
		payment.setC_DocType_ID(C_Doctype_Ar_Receipt);
		payment.setDateTrx(getLoginDate());
		payment.setDateAcct(getLoginDate());
		payment.setC_Invoice_ID(C_Invoice_ID_PosOrder);
		payment.setC_Currency_ID(C_Currency_ID);
		payment.setTenderType(MPayment.TENDERTYPE_Cash);
		payment.setC_BankAccount_ID(C_Bank_KASKARAWANG);
		payment.setDocStatus(MPayment.STATUS_Drafted);
		payment.setDocAction(MPayment.ACTION_Complete);
		payment.saveEx();
		
		payment.processIt(MOrder.ACTION_Complete);
		payment.saveEx();
		
		int C_BankAccount_ID = DB.getSQLValue(getTrxName(), "select c_bankaccount_id from c_bankaccount bc where bc.bankaccounttype = 'B' AND bc.AD_OrG_id = ? AND bc.isdefault ='Y'", payment.getC_Invoice().getAD_Org_ID());
		assertEquals(payment.getAD_Org_ID(), AD_ORG_ID_BPR1);
		assertEquals(payment.getC_BankAccount_ID(), C_BankAccount_ID);
		
	}
	
	@Test
	public void test_set_prepayment() throws Exception{
		MPayment payment = new MPayment(Env.getCtx(), 0, getTrxName());
		payment.setAD_Org_ID(AD_Org_ID_DepoBalikpapan);
		payment.setC_DocType_ID(C_DocType_ID_Kasbon);
		payment.setDateTrx(getLoginDate());
		payment.setDateAcct(getLoginDate());
		payment.setC_Currency_ID(C_Currency_ID);
		payment.setTenderType(MPayment.TENDERTYPE_Cash);
		payment.setC_BankAccount_ID(C_BankAccount_ID_KAS_BALIKPAPAN);
		payment.setDocStatus(MPayment.STATUS_Drafted);
		payment.setDocAction(MPayment.ACTION_Complete);
		payment.saveEx();
		
		payment.processIt(MOrder.ACTION_Complete);
		payment.saveEx();
		
		assertEquals(payment.getDocStatus(), MPayment.DOCSTATUS_Completed);
		assertTrue(payment.get_ValueAsBoolean("IsPrepayment"));
		
	}
}
