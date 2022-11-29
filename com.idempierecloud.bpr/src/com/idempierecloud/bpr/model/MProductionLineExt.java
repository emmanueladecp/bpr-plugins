package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.I_M_ProductionPlan;
import org.compiere.model.MProduction;
import org.compiere.model.MProductionLine;
import org.compiere.model.MQualityTest;
import org.compiere.model.Query;
import org.compiere.model.X_M_RelatedProduct;
import org.compiere.util.Msg;


public class MProductionLineExt extends MProductionLine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7565360753519006179L;

	public MProductionLineExt(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	public MProductionLineExt(Properties ctx, int M_ProductionLine_ID, String trxName) {
		super(ctx, M_ProductionLine_ID, trxName);
	}

	public MProductionLineExt(MProduction production) {
		super(production);
	}

	@Override
	protected boolean beforeSave(boolean newRecord) 
	{
		if (productionParent == null && getM_Production_ID() > 0)
			productionParent = new MProduction(getCtx(), getM_Production_ID(), get_TrxName());

		if (getM_Production_ID() > 0) 
		{
			if (newRecord && productionParent.isProcessed()) {
				log.saveError("ParentComplete", Msg.translate(getCtx(), "M_Production_ID"));
				return false;
			}
//			if ( productionParent.getM_Product_ID() == getM_Product_ID() && productionParent.getProductionQty().signum() == getMovementQty().signum())
//				setIsEndProduct(true);
//			else 
//				setIsEndProduct(false);
		} 
		else 
		{
			I_M_ProductionPlan plan = getM_ProductionPlan();
			MProduction prod = new MProduction(getCtx(), plan.getM_Production_ID(), get_TrxName());
			if (newRecord && prod.isProcessed()) {
				log.saveError("ParentComplete", Msg.translate(getCtx(), "M_Production_ID"));
				return false;
			}
			if (plan.getM_Product_ID() == getM_Product_ID() && plan.getProductionQty().signum() == getMovementQty().signum())
				setIsEndProduct(true);
			else 
				setIsEndProduct(false);
		}
		
		if ( isEndProduct() && getM_AttributeSetInstance_ID() != 0 )
		{
			String where = "M_QualityTest_ID IN (SELECT M_QualityTest_ID " +
			"FROM M_Product_QualityTest WHERE M_Product_ID=?) " +
			"AND M_QualityTest_ID NOT IN (SELECT M_QualityTest_ID " +
			"FROM M_QualityTestResult WHERE M_AttributeSetInstance_ID=?)";

			List<MQualityTest> tests = new Query(getCtx(), MQualityTest.Table_Name, where, get_TrxName())
			.setOnlyActiveRecords(true).setParameters(getM_Product_ID(), getM_AttributeSetInstance_ID()).list();
			// create quality control results
			for (MQualityTest test : tests)
			{
				test.createResult(getM_AttributeSetInstance_ID());
			}
		}
		
		if ( !isEndProduct() )
		{
			setMovementQty(getQtyUsed().negate());
		}
		
		return true;
	}
	
	public static MProductionLineExt getLine(Properties ctx, int M_Product_ID, int M_Production_ID, String trxName) {
		return new Query(ctx, MProductionLineExt.Table_Name, "M_Product_ID=? AND M_Production_ID=?", trxName)
				.setParameters(M_Product_ID, M_Production_ID)
				.first();
	}
	
	public X_M_RelatedProduct getRelatedProduct() {
		return new Query(getCtx(), X_M_RelatedProduct.Table_Name, "RelatedProduct_ID=? AND EXISTS(SELECT 1 FROM M_ProductionLine pl WHERE pl.M_Product_ID=M_RelatedProduct.M_Product_ID AND pl.isendproduct='Y' ANd pl.M_Production_ID=?) ", get_TrxName())
				.setParameters(getM_Product_ID(), getM_Production_ID())
				.first();
	}
	
}
