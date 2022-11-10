package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
		orderLine.setQty(Env.ONEHUNDRED);
		orderLine.setC_UOM_ID(C_UOM_ZAK);
		orderLine.setPrice();
		orderLine.setC_Tax_ID(C_TAX_STANDARD);
		orderLine.saveEx();
		
		BigDecimal expectedLineNetAmt = orderLine.getPriceEntered().multiply(orderLine.getQtyEntered());
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
		orderLine.setQty(Env.ONEHUNDRED);
		orderLine.setC_UOM_ID(C_UOM_ZAK);
		orderLine.setPrice();
		orderLine.setC_Tax_ID(C_TAX_STANDARD);
		orderLine.saveEx();
		
		MBPartnerLocation bpLocation = (MBPartnerLocation)order.getC_BPartner_Location();
		BigDecimal BPR_OngkosAngkut = DB.getSQLValueBD(getTrxName(), "Select OngkosAngkut from BPR_OngkosAngkutDetail where C_City_ID = ? ", bpLocation.get_ValueAsInt("C_City_ID"));
		
		BigDecimal expectedOngkosKirim = orderLine.getQtyEntered().multiply(BPR_OngkosAngkut).multiply(orderLine.getM_Product().getWeight()); 
		
		if(order.getDeliveryViaRule().equalsIgnoreCase(DeliveryViaRule_Delivery))
			assertEquals((BigDecimal)orderLine.get_Value("OngkosAngkut"), expectedOngkosKirim.setScale(2));
		
		order.processIt(MOrder.ACTION_Complete);
		order.saveEx();
	}
	
}
