package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBPRTimbangan extends X_BPR_Timbangan{

	/**
	 * 
	 */
	private static final long serialVersionUID = 950406825454023514L;
	
	public MBPRTimbangan(Properties ctx, int BPR_Timbangan_ID, String trxName) {
		super(ctx, BPR_Timbangan_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	public MBPRTimbangan(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	public MBPRTimbangan(Properties ctx, int BPR_Timbangan_ID, String trxName, String[] virtualColumns) {
		super(ctx, BPR_Timbangan_ID, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}

}
