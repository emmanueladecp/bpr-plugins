package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBPRRMALine extends X_bpr_rmaline {

	
	private static final long serialVersionUID = 6045903315528340086L;
	
	public MBPRRMALine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	public MBPRRMALine(Properties ctx, int bpr_rmaline_ID, String trxName) {
		super(ctx, bpr_rmaline_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	public MBPRRMALine(Properties ctx, int bpr_rmaline_ID, String trxName, String[] virtualColumns) {
		super(ctx, bpr_rmaline_ID, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}
}
