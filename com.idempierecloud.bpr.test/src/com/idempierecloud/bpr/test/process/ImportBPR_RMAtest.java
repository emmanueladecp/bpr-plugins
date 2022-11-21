package com.idempierecloud.bpr.test.process;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.compiere.model.MProcess;
import org.compiere.model.MRMA;
import org.compiere.model.Query;
import org.compiere.model.X_M_RelatedProduct;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.MBPRRMA;
import com.idempierecloud.bpr.model.MBPRRMALine;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class ImportBPR_RMAtest extends AbstractTestCase {
	
	private final static int M_Product_ID = 1000007;
	private final static int C_BPartner_ID = 1000020;
	private final static int AD_ORG_ID = 1000007;
	private final static int AD_PROCESS_IMPORT_BPRRMA = 1000007;
	private final static int salesRep_ID = 1007882;
	private final static BigDecimal Qty  = BigDecimal.ONE;
	private final static String Name = "Test RMA";
	boolean run = false;
	@Test
	public void test_import_rma_from_bprrma() throws Exception{
		
		MBPRRMA bprRMA = new MBPRRMA(Env.getCtx(), 0, getTrxName());
		bprRMA.setAD_Org_ID(AD_ORG_ID);
		bprRMA.setC_BPartner_ID(C_BPartner_ID);
		bprRMA.setSalesRep_ID(salesRep_ID);
		bprRMA.saveEx();
		
		MBPRRMALine bprRMALine = new MBPRRMALine(Env.getCtx(), 0 , getTrxName());
		bprRMALine.setAD_Org_ID(AD_ORG_ID);
		bprRMALine.setC_BPartner_ID(C_BPartner_ID);
		bprRMALine.setM_Product_ID(M_Product_ID);
		bprRMALine.setBPR_RMA_ID(bprRMA.get_ID());
		bprRMALine.setQty(Qty);
		bprRMALine.saveEx();
		
		MProcess process = MProcess.get(Env.getCtx(), AD_PROCESS_IMPORT_BPRRMA);
		ProcessInfo pi = new ProcessInfo(process.getName(), process.get_ID());
		pi.setAD_Client_ID(getAD_Client_ID());
		pi.setAD_User_ID(getAD_User_ID());
		
		ProcessInfoParameter[] params = new  ProcessInfoParameter[3];
		params[0] = new ProcessInfoParameter("AD_Org_ID", AD_ORG_ID, null, null, null);
		params[1] = new ProcessInfoParameter("Name", Name, null, null, null);
		params[2] = new ProcessInfoParameter("BPR_RMA_ID", bprRMA.getBPR_RMA_ID(), null, null, null);
		pi.setParameter(params);
		pi.setTransactionName(getTrxName());
		
		boolean ok = process.processIt(pi, getTrx(), false);
		if (!ok || pi.isError()) {
			fail("Error running Import BPR RMA to RMA " + (Util.isEmpty(pi.getSummary()) ? "" : " : "+pi.getSummary()));
			return;
		}
		
		boolean isInserted = new Query(Env.getCtx(), MRMA.Table_Name, " Name = ? and docstatus = 'CO' ", getTrxName())
				.setParameters(Name)
				.count()>0;
		assertTrue(isInserted);
		
	}

}
