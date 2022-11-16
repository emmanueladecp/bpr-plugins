package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProductPrice;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.X_BPR_Timbangan;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class OrderTest extends AbstractTestCase {

	private static final int C_DOCTYPE_GT_ORDER_BPR1 = 1000048;
	private static final int C_DOCTYPE_PO_BahanBaku = 1000051;
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
	private static final int M_PRODUCT_GABAH_HAMPA = 1003324;

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
		orderLine.setQty(Env.ONEHUNDRED);
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
	public void test_purchase_order_turus_calculate_qty_ordered_when_timbangan_updated() throws Exception{
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(BPR_BPR1_ORG);
		order.setIsSOTrx(false);
		order.setC_DocTypeTarget_ID(C_DOCTYPE_PO_BahanBaku);
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
		orderLine.setPrice();
		orderLine.setC_Tax_ID(C_TAX_NON_PPN);
		orderLine.saveEx();
		
		int M_PriceList_Version_ID = DB.getSQLValue(orderLine.get_TrxName(), "SELECT M_PriceList_Version_ID FROM M_PriceList_Version WHERE M_PriceList_ID=? AND ValidFrom<=? order By ValidFrom DESC Limit 1", order.getM_PriceList_ID(), order.getDateOrdered());
		MProductPrice price = MProductPrice.get(orderLine.getCtx(), M_PriceList_Version_ID, M_PRODUCT_BELITANG_BASAH, orderLine.get_TrxName());
		assertEquals(price.getPriceList(), orderLine.getPriceList());
		
		MOrderLine orderLine2 = new MOrderLine(order);
		orderLine2.setM_Product_ID(M_PRODUCT_GABAH_64);
		orderLine2.set_ValueOfColumn("RelatedProduct_ID", M_PRODUCT_GABAH_HAMPA);
		orderLine2.setQty(Env.ONEHUNDRED);
		orderLine2.setC_UOM_ID(C_UOM_KG);
		orderLine2.setPrice();
		orderLine2.setC_Tax_ID(C_TAX_NON_PPN);
		orderLine2.saveEx();
		
		M_PriceList_Version_ID = DB.getSQLValue(orderLine2.get_TrxName(), "SELECT M_PriceList_Version_ID FROM M_PriceList_Version WHERE M_PriceList_ID=? AND ValidFrom<=? order By ValidFrom DESC Limit 1", order.getM_PriceList_ID(), order.getDateOrdered());
		price = MProductPrice.get(orderLine2.getCtx(), M_PriceList_Version_ID, M_PRODUCT_GABAH_HAMPA, orderLine2.get_TrxName());
		assertEquals(price.getPriceList(), orderLine2.getPriceList());
		
		BigDecimal TimbanganNetAmt = BigDecimal.valueOf(180);
		order.set_ValueOfColumn("TimbanganNetAmt", TimbanganNetAmt);
		order.saveEx();
		
		for(MOrderLine line : order.getLines(true, "Line")) {
			BigDecimal newQtyOrdered = line.getQtyEntered().subtract(line.getQtyEntered().divide(BigDecimal.valueOf(200), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(20)));
			assertEquals(newQtyOrdered.setScale(2), line.getQtyOrdered().setScale(2));
		}
	}
	
	@Test
	public void test_purchase_order_no_duplicate_bpr_timbangan_id() throws Exception{
		X_BPR_Timbangan timbangan = new X_BPR_Timbangan(Env.getCtx(), 0, getTrxName());
		timbangan.setAD_Org_ID(getAD_Org_ID());
		timbangan.setBPR_NoKendaraan("B1234AA");
		timbangan.setM_Product_ID(M_PRODUCT_GABAH_64);
		timbangan.setValue("NOTA1");
		timbangan.setC_BPartner_ID(C_BPARTNER_CV_PADI_JAYA);
		timbangan.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		timbangan.setTimbangIsi(BigDecimal.valueOf(200));
		timbangan.setTimbangKosong(BigDecimal.valueOf(20));
		timbangan.setTimbanganNetAmt(BigDecimal.valueOf(180));
		timbangan.saveEx();
		
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(BPR_BPR1_ORG);
		order.setIsSOTrx(false);
		order.set_ValueOfColumn("BPR_Timbangan_ID", timbangan.getBPR_Timbangan_ID());
		order.setC_DocTypeTarget_ID(C_DOCTYPE_PO_BahanBaku);
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
		
		assertEquals(timbangan.getBPR_Timbangan_ID(), order.get_ValueAsInt("BPR_Timbangan_ID"));
		
		
		MOrder order2 = new MOrder(Env.getCtx(), 0, getTrxName());
		order2.setAD_Org_ID(BPR_BPR1_ORG);
		order2.setIsSOTrx(false);
		order2.set_ValueOfColumn("BPR_Timbangan_ID", timbangan.getBPR_Timbangan_ID());
		order2.setC_DocTypeTarget_ID(C_DOCTYPE_PO_BahanBaku);
		order2.setDateOrdered(getLoginDate());
		order2.setM_PriceList_ID(M_PRICELIST_PEMBELIAN);
		order2.setC_BPartner_ID(C_BPARTNER_CV_PADI_JAYA);
		order2.setC_BPartner_Location_ID(C_BPARTNER_LOCATION_CV_PADI_JAYA);
		order2.setM_Warehouse_ID(M_WAREHOUSE_GUDANG_BPR1);
		order2.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);
		order2.setC_PaymentTerm_ID(C_PAYMENT_TERM_IMMEDIATE);
		order2.setDocStatus(MOrder.STATUS_Drafted);
		order2.setDocAction(MOrder.ACTION_Complete);
		assertThrows(AdempiereException.class, () -> order2.saveEx(), "Timbangan sudah digunakan di Order "+order.getDocumentNo());
	}
	
	
}
