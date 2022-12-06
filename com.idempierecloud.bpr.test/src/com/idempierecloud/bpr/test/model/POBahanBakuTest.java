package com.idempierecloud.bpr.test.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MCost;
import org.compiere.model.MProcess;
import org.compiere.process.ProcessInfo;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.MBPRPOBahanBaku;
import com.idempierecloud.bpr.model.MBPRPOBahanBakuHeader;
import com.idempierecloud.bpr.model.MBPRPOBahanBakuLine;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class POBahanBakuTest extends AbstractTestCase {

	private static final String COSTINGMETHOD_StandardCosting = "S";
	private static final int C_Period_Jan22 = 1000036;
	private static final int AD_PROCESS_UPDATE_BAHAN_BAKU = 1000024;
	private static final int M_PRODUCT_TAMPIAN40Kg = 1000454;

	@Test
	public void test_create_po_bahan_baku() throws Exception{
		MBPRPOBahanBakuHeader master = new MBPRPOBahanBakuHeader(Env.getCtx(), 0, getTrxName());
		master.setM_Product_ID(M_PRODUCT_TAMPIAN40Kg);
		master.setAmount(BigDecimal.valueOf(1));
		
		MBPRPOBahanBaku bahanBaku = new MBPRPOBahanBaku(Env.getCtx(), 0, getTrxName());
		bahanBaku.setName("Jan-22");
		bahanBaku.setAD_Org_ID(BPR_BPR1_ORG);
		bahanBaku.setCostingMethod(COSTINGMETHOD_StandardCosting);
		bahanBaku.setC_Period_ID(C_Period_Jan22);
		bahanBaku.setAmount(BigDecimal.valueOf(5000));
		bahanBaku.saveEx();
		
		for(MBPRPOBahanBakuLine line : bahanBaku.getLines()) {
			if(line.getM_Product_ID()==M_PRODUCT_TAMPIAN40Kg)
				assertEquals(BigDecimal.valueOf(5000), line.getNewCostPrice());
		}
	}
	
	@Test
	public void test_create_po_bahan_baku_cannot_duplicate() throws Exception{
		
		MBPRPOBahanBaku bahanBaku = new MBPRPOBahanBaku(Env.getCtx(), 0, getTrxName());
		bahanBaku.setName("Jan-22");
		bahanBaku.setAD_Org_ID(BPR_BPR1_ORG);
		bahanBaku.setCostingMethod(COSTINGMETHOD_StandardCosting);
		bahanBaku.setC_Period_ID(C_Period_Jan22);
		bahanBaku.setAmount(BigDecimal.valueOf(5));
		bahanBaku.saveEx();
		
		MBPRPOBahanBaku bahanBaku2 = new MBPRPOBahanBaku(Env.getCtx(), 0, getTrxName());
		bahanBaku2.setName("Jan-22");
		bahanBaku2.setAD_Org_ID(BPR_BPR1_ORG);
		bahanBaku2.setCostingMethod(COSTINGMETHOD_StandardCosting);
		bahanBaku2.setC_Period_ID(C_Period_Jan22);
		bahanBaku2.setAmount(BigDecimal.valueOf(5));
		
		assertThrows(AdempiereException.class, ()->bahanBaku2.saveEx());
	}
	

	
	@Test
	public void test_process_bahan_baku() throws Exception{
		
		MBPRPOBahanBaku bahanBaku = new MBPRPOBahanBaku(Env.getCtx(), 0, getTrxName());
		bahanBaku.setName("Jan-22");
		bahanBaku.setAD_Org_ID(BPR_BPR1_ORG);
		bahanBaku.setCostingMethod(COSTINGMETHOD_StandardCosting);
		bahanBaku.setC_Period_ID(C_Period_Jan22);
		bahanBaku.setAmount(BigDecimal.valueOf(5));
		bahanBaku.saveEx();
		
		MProcess process = MProcess.get(Env.getCtx(), AD_PROCESS_UPDATE_BAHAN_BAKU);
		ProcessInfo pi = new ProcessInfo(process.getName(), process.get_ID());
		pi.setAD_Client_ID(getAD_Client_ID());
		pi.setAD_User_ID(getAD_User_ID());
		pi.setRecord_ID(bahanBaku.getBPR_POBahanBaku_ID());
		pi.setTransactionName(getTrxName());
		boolean ok = process.processIt(pi, getTrx(), false);
		if (!ok || pi.isError()) {
			fail("Error running Update Bahan Baku" + (Util.isEmpty(pi.getSummary()) ? "" : " : "+pi.getSummary()));
			return;
		}

		bahanBaku = new MBPRPOBahanBaku(Env.getCtx(), bahanBaku.getBPR_POBahanBaku_ID(), getTrxName());
		assertTrue(bahanBaku.isProcessed());
		
		for(MBPRPOBahanBakuLine line : bahanBaku.getLines()) {
			assertTrue(line.isProcessed());
			
			MCost cost = line.getCost();
			assertEquals(line.getNewCostPrice().setScale(2), cost.getCurrentCostPrice().setScale(2));
		}
		
	}
	
}
