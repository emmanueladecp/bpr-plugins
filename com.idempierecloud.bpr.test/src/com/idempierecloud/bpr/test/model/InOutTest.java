package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.compiere.model.MInOut;
import org.compiere.model.MMovement;
import org.compiere.model.MProcess;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.test.AbstractTestCase;

public class InOutTest extends AbstractTestCase{

	@Test
	public void test_cancel_and_move_shipment() throws Exception{
		MProcess process = MProcess.get(Env.getCtx(), 1000030);
		ProcessInfo pi = new ProcessInfo(process.getName(), process.get_ID());
		pi.setAD_Client_ID(getAD_Client_ID());
		pi.setAD_User_ID(getAD_User_ID());
		
		ProcessInfoParameter[] params = new  ProcessInfoParameter[2];
		params[0] = new ProcessInfoParameter("M_InOut_ID", 1000367, null, null, null); // BPR 2
		params[1] = new ProcessInfoParameter("M_Warehouse_ID", 1000123, null, null, null); // BPR 1
		pi.setParameter(params);
		pi.setTransactionName(getTrxName());
		
		boolean ok = process.processIt(pi, getTrx(), false);
		if (!ok || pi.isError()) {
			fail("Error running Import cancel and move shipment " + (Util.isEmpty(pi.getSummary()) ? "" : " : "+pi.getSummary()));
			return;
		}
		
		MInOut shipment = new MInOut(Env.getCtx(), 1000349, getTrxName());
		assertEquals(MInOut.STATUS_Voided, shipment.getDocStatus());
		
		int M_Movement_ID = DB.getSQLValue(getTrxName(), "SELECT M_Movement_ID FROM M_Movement WHERE description=?", shipment.getDocumentNo());
		MMovement move = new MMovement(Env.getCtx(), M_Movement_ID, getTrxName());
		assertEquals(MMovement.STATUS_Completed, move.getDocStatus());
	}
}
