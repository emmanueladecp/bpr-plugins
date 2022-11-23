package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.compiere.util.DB;
import org.compiere.util.Env;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.MBPRPicklist;
import com.idempierecloud.bpr.model.MBPRPicklistLine;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class PicklistTest extends AbstractTestCase {


	private static final int M_PRODUCT_ULTIMA_10KG = 1000163;
	private MBPRPicklist picklist;
	
	@Test
	public void test_complete_picklist_and_close() throws Exception{
		createPicklist();
		
		picklist.processIt(MBPRPicklist.ACTION_Complete);
		
		assertEquals(MBPRPicklist.STATUS_Completed, picklist.getDocStatus());	

		picklist.processIt(MBPRPicklist.ACTION_Close);
		
		assertEquals(MBPRPicklist.STATUS_Closed, picklist.getDocStatus());	
	}
	
	@Test
	public void test_complete_picklist_and_void() throws Exception{
		createPicklist();
		
		picklist.processIt(MBPRPicklist.ACTION_Complete);
		
		assertEquals(MBPRPicklist.STATUS_Completed, picklist.getDocStatus());	

		picklist.processIt(MBPRPicklist.ACTION_Void);
		
		assertEquals(MBPRPicklist.STATUS_Voided, picklist.getDocStatus());
	}

	@Test
	public void test_create_and_void_picklist() throws Exception{
		createPicklist();
		
		picklist.processIt(MBPRPicklist.ACTION_Void);
		
		assertEquals(MBPRPicklist.STATUS_Voided, picklist.getDocStatus());
	}

	private void createPicklist() {
		picklist = new MBPRPicklist(Env.getCtx(), 0, getTrxName());
		picklist.setAD_Org_ID(getAD_Org_ID());
		picklist.setDateDoc(getLoginDate());
		picklist.setDocStatus(MBPRPicklist.STATUS_Drafted);
		picklist.setDocAction(MBPRPicklist.ACTION_Complete);
		picklist.saveEx();
		
		MBPRPicklistLine line = new MBPRPicklistLine(picklist.getCtx(), 0, getTrxName());
		line.setAD_Org_ID(picklist.getAD_Org_ID());
		line.setBPR_Picklist_ID(picklist.getBPR_Picklist_ID());
		line.setLineNo(10);
		line.setM_Product_ID(M_PRODUCT_ULTIMA_10KG);
		line.setMovementQty(Env.ONE);
		line.saveEx();
		
	}
	
}
