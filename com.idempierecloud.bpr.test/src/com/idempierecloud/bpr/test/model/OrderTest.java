package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class OrderTest extends AbstractTestCase {

	private static final int C_DOCTYPE_GT_ORDER_BPR1 = 1000048;
	private static final int C_BPARTNER_ARI_SAPUTRA = 1001591;
	private static final int C_BPARTNER_LOCATION_ARI_SAPUTRA = 1001584;
	private static final int AD_ORG_KANTOR_16 = 1000006;
	private static final int M_WAREHOUSE_KANTOR_16 = 1000014;
	private static final int C_PAYMENT_TERM_IMMEDIATE = 1000000;
	private static final int M_PRODUCT_ULTIMA_10KG = 1000163;
	private static final int C_UOM_ZAK = 1000018;
	private static final int C_TAX_STANDARD = 1000000;
	private static final int M_PRICELIST_SUMATERA_GT = 1000004;
	private static final String DeliveryViaRule_Pickup = "P";
	private static final String DeliveryViaRule_Delivery = "D";
	private static final int M_PRICELIST_PEMBELIAN = 1000005;
	private static final int C_BPARTNER_CV_PADI_JAYA = 1000003;
	private static final int C_BPARTNER_LOCATION_CV_PADI_JAYA = 1000003;
	private static final int M_WAREHOUSE_GUDANG_BPR1 = 1000001;
	private static final int M_PRODUCT_GABAH_64 = 1000272;
	private static final int M_PRODUCT_BELITANG_BASAH = 1003329;
	private static final int C_UOM_KG = 1000013;
	private static final int C_DocType_PO_Non_Bahan_Baku = 1000053;
	private static final int USER_SALES = 1007908;
	private static final int C_BPartner_ID_AHONG = 1000031;
	private static final int C_BPartner_Location_ID_PALEMBANG=1000024;
	private static final int AD_Org_ID_BPR1=1000003;
	
	@Test
	public void test_so_credit_available() throws Exception{
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(AD_Org_ID_BPR1);
		order.setIsSOTrx(true);
		order.setC_DocTypeTarget_ID(C_DOCTYPE_GT_ORDER_BPR1);
		order.setDateOrdered(getLoginDate());
		order.setM_PriceList_ID(M_PRICELIST_SUMATERA_GT);
		order.setC_BPartner_ID(C_BPartner_ID_AHONG);
		order.setC_BPartner_Location_ID(C_BPartner_Location_ID_PALEMBANG);
		order.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		order.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);
		order.setC_PaymentTerm_ID(C_PAYMENT_TERM_IMMEDIATE);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDeliveryViaRule(DeliveryViaRule_Delivery);
		order.saveEx();
		
		assertEquals(order.getC_BPartner_ID(), C_BPartner_ID_AHONG);
		assertFalse(order.getDocumentNo().isEmpty());
		
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setM_Product_ID(M_PRODUCT_ULTIMA_10KG);
		orderLine.setQty(new BigDecimal(100000000));
		orderLine.setQtyOrdered(new BigDecimal(100000000));
		orderLine.setC_UOM_ID(C_UOM_ZAK);
		orderLine.setPrice();
		orderLine.setPriceEntered(new BigDecimal(100000));
		orderLine.setC_Tax_ID(C_TAX_STANDARD);
		
		BigDecimal expectedLineNetAmt = orderLine.getPriceEntered().multiply(orderLine.getQtyOrdered());
		BigDecimal SO_CreditAvaiable = orderLine.getC_Order().getC_BPartner().getSO_CreditLimit().subtract(orderLine.getC_Order().getC_BPartner().getSO_CreditUsed());
		boolean isCorrect = true;
		if(expectedLineNetAmt.compareTo(SO_CreditAvaiable)>0)
			isCorrect = false;
		assertFalse(isCorrect);
	}
	@Test
	public void test_linenetamt_include_ongkos_angkut() throws Exception{
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(AD_ORG_KANTOR_16);
		order.setIsSOTrx(true);
		order.setC_DocTypeTarget_ID(C_DOCTYPE_GT_ORDER_BPR1);
		order.setDateOrdered(getLoginDate());
		order.setM_PriceList_ID(M_PRICELIST_SUMATERA_GT);
		order.setC_BPartner_ID(C_BPARTNER_ARI_SAPUTRA);
		order.setC_BPartner_Location_ID(C_BPARTNER_LOCATION_ARI_SAPUTRA);
		order.setM_Warehouse_ID(M_WAREHOUSE_KANTOR_16);
		order.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);
		order.setC_PaymentTerm_ID(C_PAYMENT_TERM_IMMEDIATE);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDocAction(MOrder.ACTION_Complete);
		order.setDeliveryViaRule(DeliveryViaRule_Delivery);
		order.saveEx();
		
		assertEquals(order.getC_BPartner_ID(), C_BPARTNER_ARI_SAPUTRA);
		assertFalse(order.getDocumentNo().isEmpty());
		
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setM_Product_ID(M_PRODUCT_ULTIMA_10KG);
		orderLine.setQty(Env.ONE);
		orderLine.setC_UOM_ID(C_UOM_ZAK);
		orderLine.setPrice();
		orderLine.setC_Tax_ID(C_TAX_STANDARD);
		orderLine.saveEx();
		
		BigDecimal expectedLineNetAmt = orderLine.getPriceEntered().multiply(orderLine.getQtyOrdered());
		if(order.getDeliveryViaRule().equalsIgnoreCase(DeliveryViaRule_Delivery))
			assertEquals(orderLine.getLineNetAmt().setScale(2), expectedLineNetAmt.setScale(2));
		
		order.processIt(MOrder.ACTION_Complete);
		order.saveEx();
	
		assertEquals(order.getGrandTotal().setScale(2), expectedLineNetAmt.setScale(2));
	}
	
	@Test
	public void test_ongkosangkut_bpr_ongkosangkut() throws Exception{
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(AD_ORG_KANTOR_16);
		order.setIsSOTrx(true);
		order.setC_DocTypeTarget_ID(C_DOCTYPE_GT_ORDER_BPR1);
		order.setDateOrdered(getLoginDate());
		order.setM_PriceList_ID(M_PRICELIST_SUMATERA_GT);
		order.setC_BPartner_ID(C_BPARTNER_ARI_SAPUTRA);
		order.setC_BPartner_Location_ID(C_BPARTNER_LOCATION_ARI_SAPUTRA);
		order.setM_Warehouse_ID(M_WAREHOUSE_KANTOR_16);
		order.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);
		order.setC_PaymentTerm_ID(C_PAYMENT_TERM_IMMEDIATE);
		order.setDeliveryViaRule(DeliveryViaRule_Pickup);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDocAction(MOrder.ACTION_Complete);
		order.saveEx();
		
		assertEquals(order.getC_BPartner_ID(), C_BPARTNER_ARI_SAPUTRA);
		assertFalse(order.getDocumentNo().isEmpty());
		
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setM_Product_ID(M_PRODUCT_ULTIMA_10KG);
		orderLine.setQty(Env.ONE);
		orderLine.setC_UOM_ID(C_UOM_ZAK);
		orderLine.setPrice();
		orderLine.setC_Tax_ID(C_TAX_STANDARD);
		orderLine.saveEx();
		
		MBPartnerLocation bpLocation = (MBPartnerLocation)order.getC_BPartner_Location();
		BigDecimal BPR_OngkosAngkut = DB.getSQLValueBD(getTrxName(), "Select OngkosAngkut from BPR_OngkosAngkutDetail where C_City_ID = ? ", bpLocation.get_ValueAsInt("C_City_ID"));
		
		BigDecimal expectedOngkosKirim = orderLine.getQtyOrdered().multiply(BPR_OngkosAngkut).multiply(orderLine.getM_Product().getWeight()); 
		
		if(order.getDeliveryViaRule().equalsIgnoreCase(DeliveryViaRule_Delivery))
			assertEquals((BigDecimal)orderLine.get_Value("OngkosAngkut"), expectedOngkosKirim.setScale(2));
		
		order.processIt(MOrder.ACTION_Complete);
		order.saveEx();
	}
	
	@Test
	public void test_purchase_order_price() throws Exception{
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(BPR_BPR1_ORG);
		order.setIsSOTrx(false);
		order.setC_DocTypeTarget_ID(C_DocType_PO_Non_Bahan_Baku);
		order.setDateOrdered(getLoginDate());
		order.setM_PriceList_ID(M_PRICELIST_PEMBELIAN);
		order.setC_BPartner_ID(C_BPARTNER_CV_PADI_JAYA);
		order.setC_BPartner_Location_ID(C_BPARTNER_LOCATION_CV_PADI_JAYA);
		order.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		order.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);
		order.setC_PaymentTerm_ID(C_PAYMENT_TERM_IMMEDIATE);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDocAction(MOrder.ACTION_Complete);
		order.saveEx();
		
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setM_Product_ID(M_PRODUCT_GABAH_64);
		orderLine.set_ValueOfColumn("RelatedProduct_ID", M_PRODUCT_BELITANG_BASAH);
		orderLine.setQty(Env.ONEHUNDRED);
		orderLine.setC_UOM_ID(C_UOM_KG);
		orderLine.setPriceList(new BigDecimal(200000));
		orderLine.setPriceEntered(new BigDecimal(200000));
		orderLine.setC_Tax_ID(C_TAX_NON_PPN);
		orderLine.saveEx();
		
		order.processIt(MOrder.ACTION_Complete);
		order.saveEx();
		BigDecimal expectedLineNetAmt = orderLine.getPriceEntered().multiply(orderLine.getQtyOrdered());
		assertEquals(order.getGrandTotal().setScale(2), expectedLineNetAmt.setScale(2));
	}
	
	@Test
	public void test_salesrep_on_sales_order() throws Exception{
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(AD_ORG_KANTOR_16);
		order.setIsSOTrx(true);
		order.setC_DocTypeTarget_ID(C_DOCTYPE_GT_ORDER_BPR1);
		order.setDateOrdered(getLoginDate());
		order.setM_PriceList_ID(M_PRICELIST_SUMATERA_GT);
		order.set_ValueOfColumn("SalesRep_ID2", USER_SALES);
		order.setC_BPartner_ID(C_BPARTNER_ARI_SAPUTRA);
		order.setC_BPartner_Location_ID(C_BPARTNER_LOCATION_ARI_SAPUTRA);
		order.setM_Warehouse_ID(M_WAREHOUSE_KANTOR_16);
		order.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);
		order.setC_PaymentTerm_ID(C_PAYMENT_TERM_IMMEDIATE);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDocAction(MOrder.ACTION_Complete);
		order.setDeliveryViaRule(DeliveryViaRule_Delivery);
		order.saveEx();
		
		assertEquals(USER_SALES, order.getSalesRep_ID());
		assertEquals(USER_SALES, order.get_ValueAsInt("SalesRep_ID2"));
	}
	
	@Test
	public void test_additional_cost_on_sales_order_per_order_line() throws Exception{
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(BPR_BPR2_ORG);
		order.setIsSOTrx(true);
		order.setC_DocTypeTarget_ID(1000060); //MANUAL ORDER BPR
		order.setDateOrdered(getLoginDate());
		order.setM_PriceList_ID(M_PRICELIST_SUMATERA_GT);
		order.set_ValueOfColumn("SalesRep_ID2", USER_SALES);
		order.setC_BPartner_ID(1000306); //CV JAYA PELITA SEMPURNA
		order.setC_BPartner_Location_ID(1000299);
		order.setM_Warehouse_ID(1000008); //GUDANG BPR2
		order.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);
		order.setC_PaymentTerm_ID(C_PAYMENT_TERM_IMMEDIATE);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDocAction(MOrder.ACTION_Complete);
		order.setDeliveryViaRule(DeliveryViaRule_Pickup);
		order.saveEx();
		
		assertNotNull(order.getC_Order_ID());
		
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setM_Product_ID(1003635); //RAJA BIRU 5KG
		orderLine.setQtyEntered(BigDecimal.valueOf(20));
		orderLine.setQtyOrdered(BigDecimal.valueOf(100));
		orderLine.setPrice();
		orderLine.setC_UOM_ID(1000018); //ZAK
		orderLine.setC_Tax_ID(C_TAX_NON_PPN);
		orderLine.saveEx();
		
		assertEquals(BigDecimal.valueOf(9650), orderLine.getPriceActual().setScale(0));
		assertEquals(BigDecimal.valueOf(47500), orderLine.getPriceEntered().setScale(0));
		BigDecimal subsidiAmt = (BigDecimal) orderLine.get_Value("subsidiAmt");
		assertEquals(BigDecimal.valueOf(-150), subsidiAmt.setScale(0));
		assertEquals(BigDecimal.valueOf(950000), orderLine.getLineNetAmt().setScale(0));
	}
}
