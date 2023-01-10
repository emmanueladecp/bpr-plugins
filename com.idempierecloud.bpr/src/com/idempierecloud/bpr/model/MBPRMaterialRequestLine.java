package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBPRMaterialRequestLine extends X_BPR_MaterialRequestLine {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9072057481834732838L;

	public MBPRMaterialRequestLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	public MBPRMaterialRequestLine(Properties ctx, int BPR_MaterialRequestLine_ID, String trxName) {
		super(ctx, BPR_MaterialRequestLine_ID, trxName);
	}


}
