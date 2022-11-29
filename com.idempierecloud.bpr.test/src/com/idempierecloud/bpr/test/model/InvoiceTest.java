package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class InvoiceTest extends AbstractTestCase{
	
	private static final int C_DocType_ID_AR_Invoice = 1000002;
	private static final int C_BPARTNER_ARI_SAPUTRA = 1001591;
	private static final int C_BPARTNER_LOCATION_ARI_SAPUTRA = 1001584;
	private static final int M_PriceList_ID_Sumatra_GT=1000004;
	private static final int C_Currency_ID_Rupiah=303;
	private static final int C_PaymentTerm_ID_Immediate=1000000;
	private static final int M_PRODUCT_ULTIMA_10KG = 1000163;
	private static final int C_UOM_ID_Each=100;
	private static final int BPR_ListFakturPajak_ID=1000000;
	private static final int C_Tax_ID=1000000;
	private static final int AD_Org_ID_DEPO_Balikpapan=1000013;
	private static final int C_BPartner_ID_ACAI=1000020;
	private static final int C_BPartner_Location_ID_ACAI=1000013;
	private static final int Product_ULTIMA25KG = 1000007;
	private static final int C_UOM_ID_ZAK=1000018;


	@Test
	public void test_checkDocStatusShipment_BeforeComplete() throws Exception{
		MInvoice invoice = new MInvoice(Env.getCtx(),0,getTrxName());
		invoice.setAD_Org_ID(AD_Org_ID_DEPO_Balikpapan);
		invoice.setIsSOTrx(true);
		invoice.setC_DocTypeTarget_ID(C_DocType_ID_AR_Invoice);
		invoice.setC_BPartner_ID(C_BPartner_ID_ACAI);
		invoice.setC_BPartner_Location_ID(C_BPartner_Location_ID_ACAI);
		invoice.setM_PriceList_ID(M_PriceList_ID_Sumatra_GT);
		invoice.setPaymentRule("B");
		invoice.setC_Currency_ID(C_Currency_ID_Rupiah);
		invoice.setDateInvoiced(getLoginDate());
		invoice.setC_PaymentTerm_ID(C_PaymentTerm_ID_Immediate);
		invoice.set_ValueOfColumn("BPR_ListFakturPajak_ID", 1000000);
		invoice.setDocStatus(invoice.DOCSTATUS_Drafted);
		invoice.setDocAction(invoice.DOCACTION_Complete);
		invoice.saveEx();
		
		assertEquals(C_BPartner_ID_ACAI, invoice.getC_BPartner_ID());
		
		MInvoiceLine line = new MInvoiceLine(Env.getCtx(), 0, getTrxName());
		line.setAD_Org_ID(AD_Org_ID_DEPO_Balikpapan);
		line.setC_Invoice_ID(invoice.get_ID());
		line.setLine(10);
		line.setM_InOutLine_ID(1000045);
		line.setM_Product_ID(Product_ULTIMA25KG);
		line.setQty(new BigDecimal(3));
		line.setC_UOM_ID(C_UOM_ID_ZAK);
		line.setC_Tax_ID(C_Tax_ID);
		line.setPrice();
		line.saveEx();
		
		invoice.processIt(MOrder.ACTION_Complete);
		invoice.saveEx();
		
		assertEquals(MInvoice.DOCSTATUS_Invalid, invoice.DOCSTATUS_Invalid);
	}
	
	@Test
	public void test_check_faktur_beforecomplete() throws Exception{
		MInvoice invoice = new MInvoice(Env.getCtx(),0,getTrxName());
		invoice.setAD_Org_ID(BPR_BPR1_ORG);
		invoice.setC_DocTypeTarget_ID(C_DocType_ID_AR_Invoice);
		invoice.setC_BPartner_ID(C_BPARTNER_ARI_SAPUTRA);
		invoice.setC_BPartner_Location_ID(C_BPARTNER_LOCATION_ARI_SAPUTRA);
		invoice.setM_PriceList_ID(M_PriceList_ID_Sumatra_GT);
		invoice.setPaymentRule("B");
		invoice.setIsSOTrx(true);
		invoice.setC_Currency_ID(C_Currency_ID_Rupiah);
		invoice.setDateInvoiced(getLoginDate());
		invoice.setC_PaymentTerm_ID(C_PaymentTerm_ID_Immediate);
		invoice.setDocStatus(invoice.DOCSTATUS_Drafted);
		invoice.setDocAction(invoice.DOCACTION_Complete);
		invoice.saveEx();
		
		assertEquals(C_BPARTNER_ARI_SAPUTRA, invoice.getC_BPartner_ID());
		
		MInvoiceLine line = new MInvoiceLine(Env.getCtx(), 0, getTrxName());
		line.setAD_Org_ID(BPR_BPR1_ORG);
		line.setC_Invoice_ID(invoice.get_ID());
		line.setLine(10);
		line.setM_Product_ID(M_PRODUCT_ULTIMA_10KG);
		line.setQty(TEN);
		line.setC_UOM_ID(C_UOM_ID_Each);
		line.setC_Tax_ID(C_Tax_ID);
		line.setPrice();
		line.saveEx();
		
		invoice.processIt(MInvoice.ACTION_Complete);
		invoice.saveEx();
		
		assertEquals(MInvoice.DOCSTATUS_Invalid, invoice.DOCSTATUS_Invalid);
		
	}
	
	@Test
	public void test_true_check_faktur_beforecomplete() throws Exception{
		MInvoice invoice = new MInvoice(Env.getCtx(),0,getTrxName());
		invoice.setAD_Org_ID(BPR_BPR1_ORG);
		invoice.setC_DocTypeTarget_ID(C_DocType_ID_AR_Invoice);
		invoice.setC_BPartner_ID(C_BPARTNER_ARI_SAPUTRA);
		invoice.setC_BPartner_Location_ID(C_BPARTNER_LOCATION_ARI_SAPUTRA);
		invoice.setM_PriceList_ID(M_PriceList_ID_Sumatra_GT);
		invoice.setPaymentRule("B");
		invoice.setC_Currency_ID(C_Currency_ID_Rupiah);
		invoice.setDateInvoiced(getLoginDate());
		invoice.setC_PaymentTerm_ID(C_PaymentTerm_ID_Immediate);
		invoice.set_ValueOfColumn("BPR_ListFakturPajak_ID ",BPR_ListFakturPajak_ID);
		invoice.setDocStatus(invoice.DOCSTATUS_Drafted);
		invoice.setDocAction(invoice.DOCACTION_Complete);
		invoice.saveEx();
		
		assertEquals(C_BPARTNER_ARI_SAPUTRA, invoice.getC_BPartner_ID());
		
		MInvoiceLine line = new MInvoiceLine(Env.getCtx(), 0, getTrxName());
		line.setAD_Org_ID(BPR_BPR1_ORG);
		line.setC_Invoice_ID(invoice.get_ID());
		line.setLine(10);
		line.setM_Product_ID(M_PRODUCT_ULTIMA_10KG);
		line.setQty(TEN);
		line.setC_UOM_ID(C_UOM_ID_Each);
		line.setC_Tax_ID(C_Tax_ID);
		line.setPrice();
		line.saveEx();
		
		invoice.processIt(MInvoice.ACTION_Complete);
		invoice.saveEx();
		
		assertEquals(MInvoice.DOCSTATUS_Completed, invoice.DOCSTATUS_Completed);
		
	}
}
