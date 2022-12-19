package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MCost;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class MInventoryTest extends AbstractTestCase {
	
	private static final int Warehouse_BPR1=1000001;

	@Test
	public void test_inventory_cost_adjustment() throws Exception{
		MInventory inventory = new MInventory(Env.getCtx(), 0, getTrxName());
		inventory.setAD_Org_ID(BPR_BPR1_ORG);
		inventory.setC_DocType_ID(1000091); // inventory & cost adjustment
		inventory.setM_Warehouse_ID(Warehouse_BPR1);
		inventory.setMovementDate(getLoginDate());
		inventory.set_ValueOfColumn("isUpdateCosting", "Y");
		inventory.saveEx();
		
		assertNotNull(inventory.getM_Inventory_ID());
		
		MInventoryLine line = new MInventoryLine(inventory.getCtx(), 0, inventory.get_TrxName());
		line.setM_Inventory_ID(inventory.getM_Inventory_ID());
		line.setAD_Org_ID(inventory.getAD_Org_ID());
		line.setM_Product_ID(1000065); // BERAS ASALAN PRODUKSI
		line.setM_Locator_ID(1000001); // LOCATOR BAHAN BAKU
		line.setQtyCsv(TEN);
		line.setQtyCount(BigDecimal.valueOf(110));
		line.setQtyBook(Env.ONEHUNDRED);
		line.setNewCostPrice(BigDecimal.valueOf(1000));
		line.saveEx();
		
		assertEquals(BigDecimal.valueOf(110), line.getQtyCount().setScale(0));
		
		inventory.processIt(MInventory.ACTION_Complete);
		

		MInventory inventory2 = new MInventory(inventory.getCtx(), inventory.getM_Inventory_ID(), inventory.get_TrxName());
		
		assertNotEquals("", inventory2.getDescription());
		assertNotNull(inventory.getDescription());
		
		for(MInventoryLine line2 : inventory.getLines(true)) {
			assertNotEquals("", line2.getDescription());
		}
		
		BigDecimal currentCost = DB.getSQLValueBD(inventory.get_TrxName(), "SELECT currentcostprice from m_cost where ad_org_id=? and m_product_id=? and M_CostElement_ID = 1000004", inventory.getAD_Org_ID(), 1000065);
		assertEquals(BigDecimal.valueOf(1000), currentCost.setScale(0));
	}
	
	@Test
	public void test_inventory_cost_adjustment_line_not_zero() throws Exception{
		MInventory inventory = new MInventory(Env.getCtx(), 0, getTrxName());
		inventory.setAD_Org_ID(BPR_BPR1_ORG);
		inventory.setC_DocType_ID(1000091); // inventory & cost adjustment
		inventory.setM_Warehouse_ID(Warehouse_BPR1);
		inventory.setMovementDate(getLoginDate());
		inventory.set_ValueOfColumn("isUpdateCosting", "Y");
		inventory.saveEx();
		
		assertNotNull(inventory.getM_Inventory_ID());
		
		MInventoryLine line = new MInventoryLine(inventory.getCtx(), 0, inventory.get_TrxName());
		line.setM_Inventory_ID(inventory.getM_Inventory_ID());
		line.setAD_Org_ID(inventory.getAD_Org_ID());
		line.setM_Product_ID(1000065); // BERAS ASALAN PRODUKSI
		line.setM_Locator_ID(1000001); // LOCATOR BAHAN BAKU
		line.setQtyCsv(TEN);
		line.setQtyCount(BigDecimal.valueOf(110));
		line.setQtyBook(Env.ONEHUNDRED);
		line.setNewCostPrice(Env.ZERO);
		assertThrows(AdempiereException.class, () -> line.saveEx());
	}
}
