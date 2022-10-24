package com.idempierecloud.bpr.test.process;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.compiere.model.I_M_RelatedProduct;
import org.compiere.model.MProcess;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.model.X_M_RelatedProduct;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.ServerProcessCtl;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.junit.jupiter.api.Test;

import com.idempierecloud.bpr.model.I_I_RelatedProduct;
import com.idempierecloud.bpr.model.X_I_RelatedProduct;
import com.idempierecloud.bpr.test.AbstractTestCase;

public class ImportRelatedProductTest extends AbstractTestCase {

	private static final int AD_PROCESS_IMPORT_RELATED_PRODUCT = 1000000;
	private static final int M_PRODUCT_BERAS_42_PRODUKSI = 1000059;
	private static final int M_PRODUCT_BERAS_42_PRODUKSI_POLOS = 1000023;
	private static final String RELATED_PRODUCT_TYPE_ALTERNATIVE = "A";

	@Test
	public void test_import_related_product() throws Exception{
		MProduct productParent = new MProduct(Env.getCtx(), M_PRODUCT_BERAS_42_PRODUKSI, getTrxName());
		MProduct productRelated = new MProduct(Env.getCtx(), M_PRODUCT_BERAS_42_PRODUKSI_POLOS, getTrxName());
		
		// Clean Import First
		DB.executeUpdate("DELETE FROM "+X_I_RelatedProduct.Table_Name, getTrxName());
		
		X_I_RelatedProduct importRP = new X_I_RelatedProduct(Env.getCtx(), 0, getTrxName());
		importRP.setValue(productRelated.getValue());
		importRP.setName(productRelated.getName());
		importRP.setProductValue(productParent.getValue());
		importRP.setRelatedProductType(RELATED_PRODUCT_TYPE_ALTERNATIVE);
		importRP.saveEx();
		
		MProcess process = MProcess.get(Env.getCtx(), AD_PROCESS_IMPORT_RELATED_PRODUCT);
		ProcessInfo pi = new ProcessInfo(process.getName(), process.get_ID());
		pi.setAD_Client_ID(getAD_Client_ID());
		pi.setAD_User_ID(getAD_User_ID());
		ProcessInfoParameter[] params = new  ProcessInfoParameter[3];
		params[0] = new ProcessInfoParameter("AD_Client_ID", Env.getAD_Client_ID(Env.getCtx()), null, null, null);
		params[1] = new ProcessInfoParameter("DeleteOldImported", false, null, null, null);
		params[2] = new ProcessInfoParameter("IsValidateOnly", false, null, null, null);
		pi.setParameter(params);
		pi.setTransactionName(getTrxName());
		
		boolean ok = process.processIt(pi, getTrx(), false);
		if (!ok || pi.isError()) {
			fail("Error running Import Related Product" + (Util.isEmpty(pi.getSummary()) ? "" : " : "+pi.getSummary()));
			return;
		}
		
		boolean isInserted = new Query(Env.getCtx(), X_M_RelatedProduct.Table_Name, "M_Product_ID=?", getTrxName())
				.setParameters(productParent.getM_Product_ID())
				.count()>0;
				
		assertTrue(isInserted);
	}
}
