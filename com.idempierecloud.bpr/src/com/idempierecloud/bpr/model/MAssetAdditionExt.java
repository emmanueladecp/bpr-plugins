package com.idempierecloud.bpr.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAsset;
import org.compiere.model.MAssetAddition;
import org.compiere.model.MAssetChange;
import org.compiere.model.MAssetGroupAcct;
import org.compiere.model.MAssetProduct;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MConversionRate;
import org.compiere.model.MDepreciationExp;
import org.compiere.model.MDepreciationWorkfile;
import org.compiere.model.MIFixedAsset;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProject;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProjectClose;
import org.compiere.util.Env;
import org.compiere.util.Trx;

public class MAssetAdditionExt extends MAssetAddition {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4804233327219405665L;
	
	/**	Process Message 			*/
	private String		m_processMsg = null;
	/**	Just Prepared Flag			*/
	private boolean		m_justPrepared = false;
	
	public MAssetAdditionExt(Properties ctx, int A_Asset_Addition_ID, String trxName) {
		super(ctx, A_Asset_Addition_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MAssetAdditionExt(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String completeIt() 
	{
		//	Re-Check
		if (!m_justPrepared)
		{
			String status = prepareIt();
			m_justPrepared = false;
			if (!DocAction.STATUS_InProgress.equals(status))
				return status;
		}
		//	User Validation
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null) {
			return DocAction.STATUS_Invalid;
		}
		
		//	Implicit Approval
		if (!isApproved())
			approveIt();
		if (log.isLoggable(Level.INFO)) log.info(toString());
		//
		
		// Check/Create ASI:
		checkCreateASI();
		
		//loading asset
		MAsset asset = getA_Asset(!m_justPrepared); // requery if not just prepared
		if (log.isLoggable(Level.FINE)) log.fine("asset=" + asset);
		

		// Setting locator if is CreateAsset
		if (isA_CreateAsset() && getM_Locator_ID() > 0)
		{
			asset.setM_Locator_ID(getM_Locator_ID());
		}
		
		// Creating/Updating asset product
		updateA_Asset_Product(false);
		//
		// Changing asset status to Activated or Depreciated
		if (isA_CreateAsset())
		{
			asset.setAssetServiceDate(getDateDoc());
		}
		asset.changeStatus(MAsset.A_ASSET_STATUS_Activated, getDateAcct());
		asset.saveEx();

		//
		// Get/Create Asset Workfile:
		// If there Worksheet creates a new file in this asset
		MDepreciationWorkfile assetwk = MDepreciationWorkfile.get(getCtx(), getA_Asset_ID(), getPostingType(), get_TrxName());
		if (assetwk == null)
		{
			for (MAssetGroupAcct assetgrpacct :  MAssetGroupAcct.forA_Asset_Group_ID(getCtx(), asset.getA_Asset_Group_ID(), getPostingType()))
			{
				if (A_SOURCETYPE_Imported.equals(getA_SourceType()) && assetgrpacct.getC_AcctSchema_ID() != getI_FixedAsset().getC_AcctSchema_ID())
					continue;
				assetwk = new MDepreciationWorkfile(asset, getPostingType(), assetgrpacct);
				if (assetwk != null) {
					assetwk.saveEx(get_TrxName());	
				}
			}
		}
		if (log.isLoggable(Level.FINE)) log.fine("workfile: " + assetwk);

		for (MDepreciationWorkfile assetworkFile :  MDepreciationWorkfile.forA_Asset_ID(getCtx(), getA_Asset_ID(), get_TrxName()))
		{
			if (A_SOURCETYPE_Imported.equals(getA_SourceType()) && assetworkFile.getC_AcctSchema_ID() != getI_FixedAsset().getC_AcctSchema_ID())
				continue;
			
			assetworkFile.setDateAcct(getDateAcct());
			if (A_SOURCETYPE_Imported.equals(getA_SourceType())) {
				assetworkFile.adjustCost(getI_FixedAsset().getA_Asset_Cost(), getA_QTY_Current(), isA_CreateAsset());
			} else {
				if (assetworkFile.getC_AcctSchema().getC_Currency_ID() != getC_Currency_ID()) 
				{				
					BigDecimal convertedAssetCost  =  MConversionRate.convert(getCtx(), getAssetSourceAmt(),
							getC_Currency_ID(), assetworkFile.getC_AcctSchema().getC_Currency_ID() ,
							getDateAcct(), getC_ConversionType_ID(),
							getAD_Client_ID(), getAD_Org_ID());
					assetworkFile.adjustCost(convertedAssetCost, getA_QTY_Current(), isA_CreateAsset()); // reset if isA_CreateAsset
				} else {
					assetworkFile.adjustCost(getAssetSourceAmt(), getA_QTY_Current(), isA_CreateAsset()); // reset if isA_CreateAsset
				}				
			}
			// Do we have entries that are not processed and before this date:
			if (this.getA_CapvsExp().equals(A_CAPVSEXP_Capital)) { 
			//@win modification to asset value and use life should be restricted to Capital
			MDepreciationExp.checkExistsNotProcessedEntries(assetworkFile.getCtx(), assetworkFile.getA_Asset_ID(), getDateAcct(), assetworkFile.getPostingType(), assetworkFile.get_TrxName());
			//
			if (this.getA_Salvage_Value().signum() > 0) {
				if (A_SOURCETYPE_Imported.equals(getA_SourceType())) {
					assetworkFile.setA_Salvage_Value(this.getA_Salvage_Value());
				} else {
					if (assetworkFile.getC_AcctSchema().getC_Currency_ID() != getC_Currency_ID()) 
					{
						BigDecimal salvageValue = MConversionRate.convert(getCtx(), this.getA_Salvage_Value(),
								getC_Currency_ID(), assetworkFile.getC_AcctSchema().getC_Currency_ID() ,
								getDateAcct(), getC_ConversionType_ID(),
								getAD_Client_ID(), getAD_Org_ID());
						assetworkFile.setA_Salvage_Value(salvageValue);
					} else{
						assetworkFile.setA_Salvage_Value(this.getA_Salvage_Value());
					}
				}
			}
			assetworkFile.setDateAcct(getDateAcct());
			assetworkFile.setProcessed(true);
			assetworkFile.saveEx();
			}
			//@win set initial depreciation period = 1 
			if (isA_CreateAsset())
			{
				if (assetworkFile.getA_Current_Period() == 0)
				{
					assetworkFile.setA_Current_Period(1);
					assetworkFile.saveEx();
				}
			}
			//
			// Rebuild depreciation:
			assetworkFile.buildDepreciation();
		}		
		
		MAssetChange.createAddition(this, assetwk);
		
		//
		updateSourceDocument(false);
		
		// finish
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		//
		//	User Validation
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (m_processMsg != null) {
			return DocAction.STATUS_Invalid;
		}
		//
		return DocAction.STATUS_Completed;
	}	//	completeIt
	
	/**
	 * Check/Create ASI for Product (if any). If there is no product, no ASI will be created
	 */
	private void checkCreateASI() 
	{
		MProduct product = MProduct.get(getCtx(), getM_Product_ID());
		// Check/Create ASI:
		MAttributeSetInstance asi = null;
		if (product != null && getM_AttributeSetInstance_ID() == 0)
		{
			asi = new MAttributeSetInstance(getCtx(), 0, get_TrxName());
			asi.setAD_Org_ID(0);
			asi.setM_AttributeSet_ID(product.getM_AttributeSet_ID());
			asi.saveEx();
			setM_AttributeSetInstance_ID(asi.getM_AttributeSetInstance_ID());
		}
	}	
	
	/**
	 * Creating/Updating asset product
	 * @param isReversal
	 */
	private void updateA_Asset_Product(boolean isReversal)
	{
		// Skip if no product
		if (getM_Product_ID() <= 0)
		{
			return;
		}
		//
		MAssetProduct assetProduct = MAssetProduct.getCreate(getCtx(),
										getA_Asset_ID(), getM_Product_ID(), getM_AttributeSetInstance_ID(),
										get_TrxName());
		//
		if (assetProduct.get_ID() <= 0 && isReversal)
		{
			log.warning("No Product found "+this+" [IGNORE]");
			return;
		}
		//
		BigDecimal adjQty = getA_QTY_Current();
		
		if (isReversal)
		{
			adjQty = adjQty.negate();
		}
		//
		assetProduct.addA_Qty_Current(getA_QTY_Current());
		assetProduct.setAD_Org_ID(getA_Asset().getAD_Org_ID()); 
		assetProduct.saveEx();
		if (isA_CreateAsset())
		{
			MAsset asset = getA_Asset(false);
			assetProduct.updateAsset(asset);
			asset.saveEx();
		}
	}
	
	/**
	 * Update Source Document (Invoice, Project etc) Status
	 * @param isReversal is called from a reversal action (like Void, Reverse-Correct).
	 * 					We need this flag because that when we call the method from voidIt()
	 * 					the document is not marked as voided yet. Same thing applies for reverseCorrectIt too. 
	 */
	private void updateSourceDocument(final boolean isReversalParam)
	{
		boolean isReversal = isReversalParam;
		// Check if this document is reversed/voided
		String docStatus = getDocStatus();
		if (!isReversal && (DOCSTATUS_Reversed.equals(docStatus) || DOCSTATUS_Voided.equals(docStatus)))
		{
			isReversal = true;
		}
		final String sourceType = getA_SourceType();
		//
		// Invoice: mark C_InvoiceLine.A_Processed='Y' and set C_InvoiceLine.A_Asset_ID
		if (A_SOURCETYPE_Invoice.equals(sourceType) && isProcessed())
		{
			int C_InvoiceLine_ID = getC_InvoiceLine_ID();
			MInvoiceLine invoiceLine = new MInvoiceLine(getCtx(), C_InvoiceLine_ID, get_TrxName());
			invoiceLine.setA_Processed(!isReversal);
			invoiceLine.setA_Asset_ID(isReversal ? 0 : getA_Asset_ID());
			invoiceLine.saveEx();
		}
		//
		// Project
		else if (A_SOURCETYPE_Project.equals(sourceType) && isProcessed())
		{
			if (isReversal)
			{
				// Project remains closed. We just void/reverse/reactivate the Addition
			}
			else
			{
				//TODO decide whether to close project first or later
				
				int project_id = getC_Project_ID();
				ProcessInfo pi = new ProcessInfo("", 0, MProject.Table_ID, project_id);
				pi.setAD_Client_ID(getAD_Client_ID());
				pi.setAD_User_ID(Env.getAD_User_ID(getCtx()));
				//
				ProjectClose proc = new ProjectClose();
				proc.startProcess(getCtx(), pi, Trx.get(get_TrxName(), false));
				if (pi.isError())
				{
					throw new AdempiereException(pi.getSummary());
				}
				
			}
		}
		//
		// Import
		else if (A_SOURCETYPE_Imported.equals(sourceType) && !isProcessed())
		{
			if (is_new() && getI_FixedAsset_ID() > 0)
			{
				MIFixedAsset ifa = getI_FixedAsset(false);
				if (ifa != null)
				{
					ifa.setI_IsImported(true);
					ifa.setA_Asset_ID(getA_Asset_ID());
					ifa.saveEx(get_TrxName());
				}
			}
		}
		//
		// Manual
		else if (A_SOURCETYPE_Manual.equals(sourceType) && isProcessed())
		{
		  // nothing to do
		 log.fine("Nothing to do");
		}
	}
}
