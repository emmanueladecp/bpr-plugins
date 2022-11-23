package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBPRPicklistLine extends X_BPR_PicklistLine {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4619308260461964771L;

	public MBPRPicklistLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public MBPRPicklistLine(Properties ctx, int BPR_PicklistLine_ID, String trxName) {
		super(ctx, BPR_PicklistLine_ID, trxName);
	}


}
