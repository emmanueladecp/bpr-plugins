package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBPRHistoryFakturPajak extends X_BPR_HistoryFakturPajak {

	/**
	 * 
	 */
	private static final long serialVersionUID = 724230471466599336L;

	public MBPRHistoryFakturPajak(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	public MBPRHistoryFakturPajak(Properties ctx, int BPR_HistoryFakturPajak_ID, String trxName) {
		super(ctx, BPR_HistoryFakturPajak_ID, trxName);
	}


}
