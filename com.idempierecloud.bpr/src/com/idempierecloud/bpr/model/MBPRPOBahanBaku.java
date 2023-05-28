package com.idempierecloud.bpr.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MCost;
import org.compiere.model.MOrg;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MBPRPOBahanBaku extends X_BPR_POBahanBaku {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2141704074470284136L;

	public MBPRPOBahanBaku(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	public MBPRPOBahanBaku(Properties ctx, int BPR_POBahanBaku_ID, String trxName) {
		super(ctx, BPR_POBahanBaku_ID, trxName);
	}
	
	public MBPRPOBahanBakuLine[] getLines() {
		List<MBPRPOBahanBakuLine> list = new Query(getCtx(), MBPRPOBahanBakuLine.Table_Name, COLUMNNAME_BPR_POBahanBaku_ID+"=?", get_TrxName())
				.setParameters(getBPR_POBahanBaku_ID())
				.list();
		
		MBPRPOBahanBakuLine[] lines = new MBPRPOBahanBakuLine[list.size()];
		list.toArray(lines);
		
		return lines;
	}
	
	protected boolean beforeSave (boolean newRecord)
	{
		super.beforeSave(newRecord);
		
		if(getAmount().signum()<=0)
			throw new AdempiereException("Amount harus lebih dari 0");
		
		int existingPeriod = DB.getSQLValue(get_TrxName(), 
				"SELECT count(1) FROM "+MBPRPOBahanBaku.Table_Name
				+ " WHERE "+COLUMNNAME_AD_Org_ID+"=?"
				+ " AND "+COLUMNNAME_C_Period_ID+"=?"
				+ " AND "+COLUMNNAME_CostingMethod+"=?"
				+ " AND "+COLUMNNAME_BPR_POBahanBaku_ID+"<>?", 
				getAD_Org_ID(), 
				getC_Period_ID(),
				getCostingMethod(), 
				getBPR_POBahanBaku_ID());
		
		if(existingPeriod>0)
			throw new AdempiereException("Duplikat Periode dengan costing method yang sama");
		
		return true;
	}
	
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		super.afterSave(newRecord, success);

		if(isProcessed())
			return success;
		
		int deletedLines = DB.executeUpdate("DELETE FROM "+MBPRPOBahanBakuLine.Table_Name+" WHERE "+COLUMNNAME_BPR_POBahanBaku_ID+"=?", getBPR_POBahanBaku_ID(), get_TrxName());
		log.info("Deleted Lines "+deletedLines);
		
		List<MBPRPOBahanBakuHeader> masters = new Query(getCtx(), MBPRPOBahanBakuHeader.Table_Name, "AD_Client_ID=?", get_TrxName())
				.setParameters(getAD_Client_ID())
				.setOnlyActiveRecords(true)
				.list();
		
		MAcctSchema as = MAcctSchema.get(1000003);
		
		for(MBPRPOBahanBakuHeader master : masters) {
			createLine(as, master, getAD_Org_ID());
			
			List<MOrg> orgs = new Query(getCtx(), MOrg.Table_Name, null, get_TrxName())
					.setClient_ID()
					.list();
			
			for(MOrg org : orgs) {
				createLine(as, master, org.getAD_Org_ID());
			}
		}
		return success;
	}

	private void createLine(MAcctSchema as, MBPRPOBahanBakuHeader master, int ad_Org_ID) {

		
		StringBuffer whereSql = new StringBuffer("AD_Org_ID=?")
				.append(" AND M_AttributeSetInstance_ID=0")
				.append(" AND C_AcctSchema_ID=1000003")
				.append(" AND M_CostType_ID=1000003")
				.append(" AND M_CostElement_ID=1000003")
				.append(" AND M_Product_ID=?")
				;
		
		MCost cost = new Query(getCtx(), MCost.Table_Name, whereSql.toString(), get_TrxName())
				.setParameters(ad_Org_ID, master.getM_Product_ID())
				.first();
		
		BigDecimal newCostPrice = Env.ZERO;
		if(cost==null) {
			cost = new MCost((MProduct)master.getM_Product(), 0, as, ad_Org_ID, 1000003);
			cost.saveEx();
			newCostPrice = getAmount();
		}else {
			newCostPrice = master.getAmount().multiply(this.getAmount()).setScale(2, RoundingMode.HALF_UP);
		}
		
		MBPRPOBahanBakuLine line = new MBPRPOBahanBakuLine(this);
		line.setAD_Org_ID(ad_Org_ID);
		line.setName(cost.getM_Product().getName());
		line.setM_Cost_UU(cost.getM_Cost_UU());
		line.setM_Product_ID(cost.getM_Product_ID());
		line.setCurrentCostPrice(cost.getCurrentCostPrice());
		line.setNewCostPrice(newCostPrice);
		line.saveEx();
	}


}
