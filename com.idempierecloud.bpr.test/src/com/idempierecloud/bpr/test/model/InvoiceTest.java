package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.MBPRHistoryFakturPajak;
import com.idempierecloud.bpr.model.MBPRListFakturPajak;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class InvoiceTest extends AbstractTestCase{
	
	private static final int C_DocType_ID_AR_Invoice = 1000002;
	private static final int C_BPARTNER_ARI_SAPUTRA = 1001591;
	private static final int C_BPARTNER_LOCATION_ARI_SAPUTRA = 1001584;
	private static final int M_PriceList_ID_Sumatra_GT=1000004;
	private static final int C_Currency_ID_Rupiah=303;
	private static final int C_PaymentTerm_ID_Immediate=1000000;
	private static final int M_PRODUCT_ULTIMA_10KG = 1003643;
	private static final int C_UOM_ID_Each=100;
	private static final int C_Tax_ID=1000000;
	private static final int AD_Org_ID_DEPO_Balikpapan=1000013;
	private static final int C_BPartner_ID_ACAI=1000020;
	private static final int C_BPartner_Location_ID_ACAI=1000013;
	private static final int Product_ULTIMA25KG = 1003682;
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
		invoice.setDocStatus(MInvoice.DOCSTATUS_Drafted);
		invoice.setDocAction(MInvoice.DOCACTION_Complete);
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
		
		assertEquals(MInvoice.DOCSTATUS_Invalid, MInvoice.DOCSTATUS_Invalid);
	}
	
	@Test
	public void test_set_faktur_on_invoice_customer() throws Exception{
		MBPRListFakturPajak pajak = new MBPRListFakturPajak(Env.getCtx(), 0, getTrxName());
		pajak.setFiscalYear("2022");
		pajak.setName("001");
		pajak.saveEx();
		
		assertNotNull(pajak.getBPR_ListFakturPajak_ID());
		assertEquals("001", pajak.getName());
		
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
		invoice.set_ValueOfColumn("TypeFaktur", "080");
		invoice.setDocStatus(MInvoice.DOCSTATUS_Drafted);
		invoice.setDocAction(MInvoice.DOCACTION_Complete);
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
		
		MInvoice invoice2 = new MInvoice(Env.getCtx(), invoice.getC_Invoice_ID(), getTrxName());
		assertEquals(pajak.getBPR_ListFakturPajak_ID(), invoice2.get_ValueAsInt("BPR_ListFakturPajak_ID"));
		
		int history = DB.getSQLValue(getTrxName(), "SELECT 1 FROM BPR_HistoryFakturPajak WHERE C_Invoice_ID=?", invoice2.getC_Invoice_ID());
		assertEquals(1, history);
	}
	
	@Test
	public void test_cannot_void_invoice_customer_if_faktur_pajak_uploaded() throws Exception{
		MBPRListFakturPajak pajak = new MBPRListFakturPajak(Env.getCtx(), 0, getTrxName());
		pajak.setFiscalYear("2022");
		pajak.setName("001");
		pajak.saveEx();
		
		assertNotNull(pajak.getBPR_ListFakturPajak_ID());
		assertEquals("001", pajak.getName());
		
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
		invoice.set_ValueOfColumn("TypeFaktur", "080");
		invoice.setDocStatus(MInvoice.DOCSTATUS_Drafted);
		invoice.setDocAction(MInvoice.DOCACTION_Complete);
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
		
		MInvoice invoice2 = new MInvoice(Env.getCtx(), invoice.getC_Invoice_ID(), getTrxName());
		assertEquals(pajak.getBPR_ListFakturPajak_ID(), invoice2.get_ValueAsInt("BPR_ListFakturPajak_ID"));
		
		int history_id = DB.getSQLValue(getTrxName(), "SELECT BPR_HistoryFakturPajak_ID FROM BPR_HistoryFakturPajak WHERE C_Invoice_ID=?", invoice2.getC_Invoice_ID());
		MBPRHistoryFakturPajak history = new MBPRHistoryFakturPajak(Env.getCtx(), history_id, getTrxName()); 
		assertEquals(invoice2.getC_Invoice_ID(), history.getC_Invoice_ID());
		
		history.setIsUploaded(true);
		history.saveEx();

		invoice.processIt(MInvoice.ACTION_Void);
		assertNotEquals(MInvoice.STATUS_Voided, invoice.getDocStatus());
		
		
	}
}
