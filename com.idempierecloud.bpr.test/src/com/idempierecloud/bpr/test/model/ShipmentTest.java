package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.compiere.model.MInOut;
import org.compiere.model.MInOutConfirm;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInOutLineConfirm;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class ShipmentTest extends AbstractTestCase{

	private static final int C_DocType_Shipment_With_Confirmation = 1000058;
	private static final int C_BPARTNER_HYPERMARTBEKASI = 1007596;

	@Test
	public void test_create_shipment_with_confirm() throws Exception{
		MInOut shipment = new MInOut(Env.getCtx(), 0, getTrxName());
		shipment.setAD_Org_ID(BPR_BPR1_ORG);
		shipment.setC_DocType_ID(C_DocType_Shipment_With_Confirmation);
		shipment.setC_BPartner_ID(C_BPARTNER_HYPERMARTBEKASI);
		shipment.setC_BPartner_Location_ID(1010330);
		shipment.setM_Warehouse_ID(1000001);
		shipment.setMovementType(MInOut.MOVEMENTTYPE_CustomerShipment);
		shipment.setMovementDate(getLoginDate());
		shipment.saveEx();
		
		MInOutLine line = new MInOutLine(shipment);
		line.setM_Product_ID(1003668); // FG-PLM-64-020
		line.setM_Locator_ID(1000005); // FINISH GOOD
		line.setC_UOM_ID(C_UOM_ZAK);	
		line.setQtyEntered(TEN);
		line.setMovementQty(BigDecimal.valueOf(200));
		line.saveEx();
		
		assertNotNull(shipment.getDocumentNo());
		
		shipment.processIt(MInOut.ACTION_Complete);
		shipment.saveEx();

		assertEquals(MInOut.STATUS_InProgress, shipment.getDocStatus());
		
		MInOutConfirm confirm = new Query(Env.getCtx(), MInOutConfirm.Table_Name, MInOutConfirm.COLUMNNAME_M_InOut_ID+"=?", getTrxName())
				.setParameters(shipment.getM_InOut_ID())
				.first();
		
		assertNotNull(confirm);
		assertEquals(shipment.getM_InOut_ID(), confirm.getM_InOut_ID());
		
		for(MInOutLineConfirm confirmLine : confirm.getLines(false)) {
			assertEquals(C_UOM_ZAK, confirmLine.get_Value("C_UOM_ID"));
			BigDecimal qtyEntered = (BigDecimal) confirmLine.get_Value("QtyEntered");
			assertEquals(TEN.intValue(), qtyEntered.intValue());
			assertEquals(200, confirmLine.getConfirmedQty().intValue());
			
			confirmLine.set_ValueOfColumn("QtyEntered", Env.ONE);
			confirmLine.setConfirmedQty(BigDecimal.valueOf(20));
			confirmLine.saveEx();
			
		}
		
		confirm.processIt(MInOut.ACTION_Complete);
		
		assertEquals(MInOutConfirm.STATUS_Completed, confirm.getDocStatus());
		
		MInOut shipment2 = (MInOut) confirm.getM_InOut();
		assertEquals(MInOutConfirm.STATUS_Completed, shipment2.getDocStatus());
		
		for(MInOutLine line2 : shipment2.getLines()) {
			assertEquals(1, line2.getQtyEntered().intValue());
		}
	}
}
