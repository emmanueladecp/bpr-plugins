package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.compiere.model.MProductionLine;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.MProductionExt;
import com.idempierecloud.bpr.model.MProductionLineExt;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class ProductionTest extends AbstractTestCase {

	private static final int DocType_Repacking =1000057;
	private static final int Product_Beras36Patahan = 1003353;
	private static final int Product_Tomato5Kg = 1000058;
	private static final int Locator_GBBBahanBaku = 1000001;
	private static final int Product_BerasAsalanKWBagus = 1000066;
	private static final int Product_Bangau20Kg = 1000020;
	private static final int Product_TurunanColourSorterReproses = 1000457;
	private static final int Product_ComponentBeras64BelitangSupplier = 1000038;
	private static final int Product_ComponentBeras64JalurSupplier = 1000042;

	@Test
	public void test_perubahan_type() throws Exception{
		MProductionExt production = new MProductionExt(Env.getCtx(), 0, getTrxName());
		production.setAD_Org_ID(BPR_BPR1_ORG);
		production.set_ValueOfColumn("C_DocType_ID", DocType_Repacking);
		production.setMovementDate(getLoginDate());
		production.setM_Product_ID(Product_Beras36Patahan);
		production.setM_Locator_ID(Locator_GBBBahanBaku);
		production.setProductionQty(Env.ONE);
		production.setIsCreated("Y");
		production.saveEx();
		
		assertEquals(production.get_ValueAsInt("C_DocType_ID"), DocType_Repacking);
		
		MProductionLine line = new MProductionLine(production);
		line.setLine(10);
		line.setM_Product_ID(Product_Beras36Patahan);
		line.setIsEndProduct(true);
		line.setM_Locator_ID(Locator_GBBBahanBaku);
		line.setMovementQty(Env.ONE);
		line.saveEx();
		
		
		MProductionLine line2 = new MProductionLine(production);
		line2.setLine(20);
		line2.setM_Product_ID(Product_BerasAsalanKWBagus);
		line2.setIsEndProduct(false);
		line2.setM_Locator_ID(Locator_GBBBahanBaku);
		line2.setMovementQty(Env.ONE);
		line2.saveEx();
		
		production.processIt(MProductionExt.ACTION_Complete);
		
		assertEquals(MProductionExt.STATUS_Completed, production.getDocStatus());
		
		production.processIt(MProductionExt.ACTION_Reverse_Correct);
		
		assertEquals(MProductionExt.STATUS_Reversed, production.getDocStatus());
		
	}
	
	@Test
	public void test_production_bpr() throws Exception{
		MProductionExt production = new MProductionExt(Env.getCtx(), 0, getTrxName());
		production.setAD_Org_ID(BPR_BPR1_ORG);
		production.set_ValueOfColumn("C_DocType_ID", DocType_Repacking);
		production.setMovementDate(getLoginDate());
		production.setM_Product_ID(Product_Tomato5Kg);
		production.setM_Locator_ID(Locator_GBBBahanBaku);
		production.setProductionQty(BigDecimal.valueOf(10));
		production.setIsCreated("Y");
		production.saveEx();
		
		assertEquals(production.get_ValueAsInt("C_DocType_ID"), DocType_Repacking);
		
		MProductionLineExt FG1 = new MProductionLineExt(production);
		FG1.setLine(10);
		FG1.setM_Product_ID(Product_Tomato5Kg);
		FG1.setIsEndProduct(true);
		FG1.setM_Locator_ID(Locator_GBBBahanBaku);
		FG1.setMovementQty(BigDecimal.valueOf(10));
		FG1.saveEx();
		
		assertTrue(FG1.isEndProduct());
		
		MProductionLineExt FG2 = new MProductionLineExt(production);
		FG2.setLine(20);
		FG2.setM_Product_ID(Product_Bangau20Kg);
		FG2.setIsEndProduct(true);
		FG2.setM_Locator_ID(Locator_GBBBahanBaku);
		FG2.setMovementQty(BigDecimal.valueOf(2));
		FG2.saveEx();
		
		assertTrue(FG2.isEndProduct());
		
		
		MProductionLineExt turunan = new MProductionLineExt(production);
		turunan.setLine(30);
		turunan.setM_Product_ID(Product_TurunanColourSorterReproses);
		turunan.setIsEndProduct(false);
		turunan.setM_Locator_ID(Locator_GBBBahanBaku);
		turunan.setMovementQty(BigDecimal.valueOf(5));
		turunan.saveEx();
		
		assertFalse(turunan.isEndProduct());
		
		MProductionLineExt component1 = new MProductionLineExt(production);
		component1.setLine(40);
		component1.setM_Product_ID(Product_ComponentBeras64BelitangSupplier);
		component1.setIsEndProduct(false);
		component1.setM_Locator_ID(Locator_GBBBahanBaku);
		component1.setQtyUsed(BigDecimal.valueOf(35));
		component1.saveEx();

		
		assertFalse(component1.isEndProduct());
		
		
		MProductionLineExt component2 = new MProductionLineExt(production);
		component2.setLine(50);
		component2.setM_Product_ID(Product_ComponentBeras64JalurSupplier);
		component2.setIsEndProduct(false);
		component2.setM_Locator_ID(Locator_GBBBahanBaku);
		component2.setQtyUsed(BigDecimal.valueOf(60));
		component2.saveEx();

		assertFalse(component2.isEndProduct());
		
		production.processIt(MProductionExt.ACTION_Complete);
		
		assertEquals(MProductionExt.STATUS_Completed, production.getDocStatus());
	}
	
}
