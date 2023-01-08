package com.idempierecloud.bpr.test.model;


import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.globalqss.model.MLCOInvoiceWithholding;
import org.globalqss.model.X_LCO_WithholdingRule;
import org.globalqss.model.X_LCO_WithholdingRuleConf;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.MBPRHistoryFakturPajak;
import com.idempierecloud.bpr.model.MBPRListFakturPajak;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class InvoiceTest extends AbstractTestCase{
	
	private static final int C_DocType_ID_AR_Invoice = 1000002;
	private static final int C_DocType_ID_AP_Invoice_Turus = 1000095;
	private static final int C_BPARTNER_ARI_SAPUTRA = 1001591;
	private static final int C_BPARTNER_LOCATION_ARI_SAPUTRA = 1001584;
	private static final int M_PriceList_ID_Sumatra_GT=1000004;
	private static final int M_PriceList_ID_Pembelian_Bahan_Baku =1000005;
	private static final int C_Currency_ID_Rupiah=303;
	private static final int C_PaymentTerm_ID_Immediate=1000000;
	private static final int M_PRODUCT_ULTIMA_10KG = 1003643;
	private static final int C_UOM_ID_Each=100;
	private static final int C_UOM_ID_KG = 1000013;
	private static final int C_Tax_ID=1000000;
	private static final int AD_Org_ID_DEPO_Balikpapan=1000013;
	private static final int C_BPartner_ID_ACAI=1000020;
	private static final int C_BPartner_Location_ID_ACAI=1000013;
	private static final int C_BPartner_Location_ID_Hanafi = 1009761;
	private static final int Product_ULTIMA25KG = 1003682;
	private static final int C_UOM_ID_ZAK=1000018;
	private static final int AD_Org_ID_BPR2=1000013;
	private static final int C_BPartner_HANAFI = 1010966;


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
		pajak.setIsActive(true);
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
		invoice.setDateInvoiced(Timestamp.valueOf("2022-12-01 00:00:00"));
		invoice.setDateAcct(Timestamp.valueOf("2022-12-01 00:00:00"));
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
		
		MBPRHistoryFakturPajak history = MBPRHistoryFakturPajak.byInvoice(invoice2);
		assertEquals(history.getBPR_HistoryFakturPajak_ID(), invoice2.get_ValueAsInt("BPR_HistoryFakturPajak_ID"));
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
	@Test
	public void test_invoice_vendor_create_witholding_baseon_witholding_orderline() throws Exception{
		
		MInvoice invoice = new MInvoice(Env.getCtx(),0,getTrxName());
		invoice.setAD_Org_ID(AD_Org_ID_BPR2);
		invoice.setIsSOTrx(false);
		invoice.setC_DocTypeTarget_ID(C_DocType_ID_AP_Invoice_Turus);
		invoice.setC_BPartner_ID(C_BPartner_HANAFI);
		invoice.setC_BPartner_Location_ID(C_BPartner_Location_ID_Hanafi);
		invoice.setM_PriceList_ID(M_PriceList_ID_Pembelian_Bahan_Baku);
		invoice.setPaymentRule("P");
		invoice.setC_Currency_ID(C_Currency_ID_Rupiah);
		invoice.setDateInvoiced(getLoginDate());
		invoice.setC_PaymentTerm_ID(C_PaymentTerm_ID_Immediate);
		invoice.setDocStatus(MInvoice.DOCSTATUS_Drafted);
		invoice.setDocAction(MInvoice.DOCACTION_Complete);
		invoice.saveEx();
		
		assertEquals(C_BPartner_HANAFI, invoice.getC_BPartner_ID());
		
		MInvoiceLine line = new MInvoiceLine(Env.getCtx(), 0, getTrxName());
		line.setAD_Org_ID(AD_Org_ID_BPR2);
		line.setC_Invoice_ID(invoice.get_ID());
		line.setLine(10);
		line.setM_Product_ID(1000035);
		line.setC_OrderLine_ID(1001607);
		line.setQty(new BigDecimal(30));
		line.setC_UOM_ID(C_UOM_ID_KG);
		line.setC_Tax_ID(C_Tax_ID);
		line.setPrice(new BigDecimal(8610));
		line.saveEx();
		
		invoice.processIt(MOrder.ACTION_Complete);
		invoice.saveEx();
		
		boolean success = false;
		MLCOInvoiceWithholding miw= new Query(invoice.getCtx(),MLCOInvoiceWithholding.Table_Name," C_Invoice_ID=? ",invoice.get_TrxName())
				.setOnlyActiveRecords(true)
				.setParameters(invoice.getC_Invoice_ID())
				.first();
		if(miw==null) 
			success = false;
		else 
			success = true;
		
		assertTrue(success);
		assertEquals(BigDecimal.valueOf(7749).setScale(2), ((BigDecimal) invoice.get_Value("WithholdingAmt")).setScale(2));
		BigDecimal big = ((BigDecimal) invoice.get_Value("WithholdingAmt")).setScale(2);
	}
}
