package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBPRListFakturPajak extends X_BPR_ListFakturPajak {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3794968571348189584L;

	public MBPRListFakturPajak(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public MBPRListFakturPajak(Properties ctx, int BPR_ListFakturPajak_ID, String trxName) {
		super(ctx, BPR_ListFakturPajak_ID, trxName);
	}


}
