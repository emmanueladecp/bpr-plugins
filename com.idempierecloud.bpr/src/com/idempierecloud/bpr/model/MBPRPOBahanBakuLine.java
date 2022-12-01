package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.MCost;
import org.compiere.model.Query;

public class MBPRPOBahanBakuLine extends X_BPR_POBahanBakuLine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7854684871601126881L;

	public MBPRPOBahanBakuLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	public MBPRPOBahanBakuLine(Properties ctx, int BPR_POBahanBakuLine_ID, String trxName) {
		super(ctx, BPR_POBahanBakuLine_ID, trxName);
	}

	public MBPRPOBahanBakuLine(MBPRPOBahanBaku parent) {
		super(parent.getCtx(), 0, parent.get_TrxName());
		setAD_Org_ID(parent.getAD_Org_ID());
		setBPR_POBahanBaku_ID(parent.getBPR_POBahanBaku_ID());
	}
	
	public MCost getCost() {
		MCost cost = new Query(getCtx(), MCost.Table_Name, MCost.COLUMNNAME_M_Cost_UU+"=?", get_TrxName())
				.setParameters(getM_Cost_UU())
				.first();
		
		return cost;
	}


}
