package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.MProduction;
import org.compiere.model.MSysConfig;
import org.compiere.process.DocAction;

public class MProductionExt extends MProduction implements DocAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4781557519166392871L;
	
	public MProductionExt(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	

	public MProductionExt(Properties ctx, int M_Production_ID, String trxName) {
		super(ctx, M_Production_ID, trxName);
	}

	/**
	 * 
	 * @param M_Product_ID
	 * @return error message (if any)
	 */
	public String validateEndProduct(int M_Product_ID) {
//		String msg = isBom(M_Product_ID);
//		if (!Util.isEmpty(msg))
//			return msg;

		if (!costsOK(M_Product_ID)) {
			String msg = "Excessive difference in standard costs";
			if (MSysConfig.getBooleanValue(MSysConfig.MFG_ValidateCostsDifferenceOnCreate, false, getAD_Client_ID())) {
				return msg;
			} else {
				log.warning(msg);
			}
		}

		return null;
	}

	
}
