package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBPRQualityControl extends X_BPR_QualityControl{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8987655183408134958L;
	
	public MBPRQualityControl(Properties ctx, int BPR_QualityControl_ID, String trxName) {
		super(ctx, BPR_QualityControl_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	public MBPRQualityControl(Properties ctx, int BPR_QualityControl_ID, String trxName, String[] virtualColumns) {
		super(ctx, BPR_QualityControl_ID, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}
	public MBPRQualityControl(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

}
