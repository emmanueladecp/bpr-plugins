package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.Query;
import org.compiere.util.Env;

public class MBPRPOBahanBakuHeader extends X_BPR_POBahanBakuHeader {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8889118083416499995L;

	public MBPRPOBahanBakuHeader(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public MBPRPOBahanBakuHeader(Properties ctx, int BPR_POBahanBakuHeader_ID, String trxName) {
		super(ctx, BPR_POBahanBakuHeader_ID, trxName);
	}

	public static MBPRPOBahanBakuHeader get(Properties ctx, int m_Product_ID, String trxName) {
		return new Query(Env.getCtx(), MBPRPOBahanBakuHeader.Table_Name, MBPRPOBahanBakuHeader.COLUMNNAME_M_Product_ID+"=?", trxName)
				.setParameters(m_Product_ID)
				.first();
	}

}
