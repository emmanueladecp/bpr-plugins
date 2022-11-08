package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBPROngkosAngkutDetail extends X_BPR_OngkosAngkutDetail{

	private static final long serialVersionUID = -5113460662235861317L;
	
	public MBPROngkosAngkutDetail(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MBPROngkosAngkutDetail(Properties ctx, int BPR_OngkosAngkutDetail_ID, String trxName) {
		super(ctx, BPR_OngkosAngkutDetail_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MBPROngkosAngkutDetail(Properties ctx, int BPR_OngkosAngkutDetail_ID, String trxName,
			String[] virtualColumns) {
		super(ctx, BPR_OngkosAngkutDetail_ID, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}

	

}
