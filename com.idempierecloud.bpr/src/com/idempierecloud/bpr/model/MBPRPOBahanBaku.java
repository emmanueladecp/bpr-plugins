package com.idempierecloud.bpr.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MCost;
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
		
		StringBuffer whereSql = new StringBuffer("AD_Org_ID=?")
				.append(" AND M_AttributeSetInstance_ID=0")
				.append(" AND C_AcctSchema_ID=1000003")
				.append(" AND M_CostType_ID=1000003")
				.append(" AND M_CostElement_ID=1000003")
				.append(" AND EXISTS(")
				.append(" select 1 from m_product")
				.append(" where m_cost.m_product_id=m_product.m_product_id")
				.append(" and m_product_category_id in(")
				.append(" select M_Product_Category_id from M_Product_Category_Acct")
				.append(" where C_AcctSchema_ID=1000003")
				.append(" and costingMethod=?")
				.append(" and Costinglevel='O')")
				.append(" )")
				.append(" and exists(SELECT 1 FROM BPR_POBahanBakuHeader bahanbaku")
				.append(" WHERE bahanbaku.m_product_id=m_cost.m_product_id")
				.append(" )")
				;
		
		List<MCost> costs = new Query(getCtx(), MCost.Table_Name, whereSql.toString(), get_TrxName())
				.setParameters(getAD_Org_ID(), getCostingMethod())
				.list();
		
		for(MCost cost : costs) {
			MBPRPOBahanBakuHeader master = MBPRPOBahanBakuHeader.get(cost.getCtx(), cost.getM_Product_ID(), cost.get_TrxName());
			
			MBPRPOBahanBakuLine line = new MBPRPOBahanBakuLine(this);
			line.setName(cost.getM_Product().getName());
			line.setM_Cost_UU(cost.getM_Cost_UU());
			line.setM_Product_ID(cost.getM_Product_ID());
			line.setCurrentCostPrice(cost.getCurrentCostPrice());
			line.setNewCostPrice(master.getAmount().multiply(this.getAmount()).setScale(2, RoundingMode.HALF_UP));
			line.saveEx();
		}
		return success;
	}


}
