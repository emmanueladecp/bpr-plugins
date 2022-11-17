package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class MovementTest extends AbstractTestCase {
	
	private static final int DocType_MaterialMovement = 1000022;
	private static final int DocType_ConfirmMovement = 1000059;
	private static final int Warehouse_BPR1=1000001;
	private static final int Warehouse_BPR2=1000008;
	private static final int Product_BerasPera50Kg = 1000234;
	private static final int Locator_BPR1FinishGood = 1000005;
	private static final int Locator_BPR1InTransit = 1000004;
	private static final int Locator_BPR1Susut = 1000011;
	private static final int Locator_BPR2FinishGood = 1000009;

	@Test
	public void test_materialmovement_cannot_complete_over_movement_qty() throws Exception{
		MMovement movement = new MMovement(Env.getCtx(), 0, getTrxName());
		movement.setAD_Org_ID(getAD_Org_ID());
		movement.setC_DocType_ID(DocType_MaterialMovement);
		movement.setM_Warehouse_ID(Warehouse_BPR1);
		movement.setM_WarehouseTo_ID(Warehouse_BPR2);
		movement.setMovementDate(getLoginDate());
		movement.setIsInTransit(true);
		movement.setDocAction(MMovement.ACTION_Complete);
		movement.saveEx();
		
		MMovementLine line = new MMovementLine(movement);
		line.setM_Product_ID(Product_BerasPera50Kg);
		line.setM_Locator_ID(Locator_BPR1FinishGood);
		line.setM_LocatorTo_ID(Locator_BPR1InTransit);
		line.set_ValueOfColumn("M_LocatorToAlias_ID", Locator_BPR2FinishGood);
		line.setMovementQty(BigDecimal.valueOf(10));
		line.saveEx();
		
		movement.processIt(MMovement.ACTION_Complete);
		
		assertEquals(MMovement.STATUS_Completed, movement.getDocStatus());
		
		MMovement movementConfirm = new MMovement(Env.getCtx(), 0, getTrxName());
		movementConfirm.setAD_Org_ID(getAD_Org_ID());
		movementConfirm.setC_DocType_ID(DocType_ConfirmMovement);
		movementConfirm.setM_Warehouse_ID(Warehouse_BPR1);
		movementConfirm.setM_WarehouseTo_ID(Warehouse_BPR2);
		movementConfirm.setMovementDate(getLoginDate());
		movementConfirm.setIsInTransit(true);
		movementConfirm.setDocAction(MMovement.ACTION_Complete);
		movementConfirm.saveEx();
		
		MMovementLine confirmLine = new MMovementLine(movementConfirm);
		confirmLine.setM_Product_ID(Product_BerasPera50Kg);
		confirmLine.setM_Locator_ID(Locator_BPR1FinishGood);
		confirmLine.setM_LocatorTo_ID(Locator_BPR1InTransit);
		confirmLine.set_ValueOfColumn("M_LocatorToAlias_ID", Locator_BPR2FinishGood);
		confirmLine.setMovementQty(BigDecimal.valueOf(11));
		confirmLine.setTargetQty(BigDecimal.valueOf(10));
		confirmLine.saveEx();
		
		movementConfirm.processIt(MMovement.ACTION_Complete);
		
		assertNotEquals(MMovement.STATUS_Completed, movementConfirm.getDocStatus());
	}
	
	@Test
	public void test_materialmovement_create_confirmmovement() throws Exception{
		MMovement movement = new MMovement(Env.getCtx(), 0, getTrxName());
		movement.setAD_Org_ID(getAD_Org_ID());
		movement.setC_DocType_ID(DocType_MaterialMovement);
		movement.setM_Warehouse_ID(Warehouse_BPR1);
		movement.setM_WarehouseTo_ID(Warehouse_BPR2);
		movement.setMovementDate(getLoginDate());
		movement.setIsInTransit(true);
		movement.setDocAction(MMovement.ACTION_Complete);
		movement.saveEx();
		
		MMovementLine line = new MMovementLine(movement);
		line.setM_Product_ID(Product_BerasPera50Kg);
		line.setM_Locator_ID(Locator_BPR1FinishGood);
		line.setM_LocatorTo_ID(Locator_BPR1InTransit);
		line.set_ValueOfColumn("M_LocatorToAlias_ID", Locator_BPR2FinishGood);
		line.setMovementQty(new BigDecimal(10));
		line.saveEx();
		
		movement.processIt(MMovement.ACTION_Complete);
		
		MMovement movementConfirm = new Query(Env.getCtx(), MMovement.Table_Name, "C_DocType_ID=? AND MoveReference=?", getTrxName())
				.setParameters(DocType_ConfirmMovement, movement.getDocumentNo())
				.first();
		
		assertEquals(MMovement.STATUS_Completed, movement.getDocStatus());
		assertEquals(movement.getDocumentNo(), movementConfirm.get_ValueAsString("moveReference"));
		
		assertEquals(movement.getLines(false).length, movementConfirm.getLines(false).length);
		
		for(MMovementLine confirmLine:movementConfirm.getLines(false)) {
			assertEquals(line.getMovementQty(), confirmLine.getTargetQty());
			
			confirmLine.setMovementQty(new BigDecimal(5));
			confirmLine.saveEx();
		}
		
		movementConfirm.processIt(MMovement.ACTION_Complete);
		
		assertEquals(MMovement.STATUS_Completed, movementConfirm.getDocStatus());
		
		int susut = 0;
		for(MMovementLine confirmLine:movementConfirm.getLines(true)) {
			if(confirmLine.getM_LocatorTo_ID()==Locator_BPR1Susut) {
				susut++;
				assertEquals(5, confirmLine.getMovementQty().intValue());
			}
		}
		
		assertEquals(1, susut);
	}
	
}
