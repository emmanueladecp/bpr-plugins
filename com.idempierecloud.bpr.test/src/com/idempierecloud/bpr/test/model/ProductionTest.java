package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.compiere.model.MProductionLine;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.MProductionExt;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class ProductionTest extends AbstractTestCase {

	private static final int DocType_Repacking =1000057;
	private static final int Product_Beras36Patahan = 1003353;
	private static final int Locator_GBBBahanBaku = 1000001;
	private static final int Product_BerasAsalanKWBagus = 1000066;

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
		
	}
	
}
