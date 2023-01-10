package com.idempierecloud.bpr.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;

public class MBPRMaterialRequest extends X_BPR_MaterialRequest implements DocAction, DocOptions {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4552373274956073589L;
	private String m_processMsg;


	public MBPRMaterialRequest(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	

	public MBPRMaterialRequest(Properties ctx, int BPR_MaterialRequest_ID, String trxName) {
		super(ctx, BPR_MaterialRequest_ID, trxName);
	}
	
	public MBPRMaterialRequestLine[] getLines()
	{
		List<MBPRMaterialRequestLine> list = new Query(getCtx(), MBPRMaterialRequestLine.Table_Name, MBPRMaterialRequestLine.COLUMNNAME_BPR_MaterialRequest_ID+"=?", get_TrxName())
				.setParameters(getBPR_MaterialRequest_ID())
				.list();
		
		MBPRMaterialRequestLine[] lines = new MBPRMaterialRequestLine[list.size()];
		list.toArray(lines);
		return lines;
	}
	
	@Override
	public int customizeValidActions(String docStatus, Object processing, String orderType, String isSOTrx,
			int AD_Table_ID, String[] docAction, String[] options, int index) {
		index = 0;
		if(docStatus.equals(STATUS_Drafted)) {
			options[index++] = ACTION_Complete;
			options[index++] = ACTION_Void;
		}else if(docStatus.equals(STATUS_Completed)) {
			options[index++] = ACTION_Close;
			options[index++] = ACTION_Void;
		}else if(docStatus.equals(STATUS_Voided)) {
			options[index++] = ACTION_None;
		}else if(docStatus.equals(STATUS_Closed)) {
			options[index++] = ACTION_None;
		}else {
			options[index++] = ACTION_Complete;
			options[index++] = ACTION_Void;
		}
		return index;
	}

	@Override
	public boolean processIt(String action) throws Exception {
		m_processMsg = null;
		DocumentEngine engine = new DocumentEngine (this, getDocStatus());
		return engine.processIt (action, getDocAction());
	}

	@Override
	public boolean unlockIt() {
		
		return false;
	}

	@Override
	public boolean invalidateIt() {
		
		return false;
	}

	@Override
	public String prepareIt() {
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		if(getLines().length==0) {
			m_processMsg = "No Lines";
			return STATUS_Invalid;
		}
			
		for(MBPRMaterialRequestLine line : getLines()) {
			line.setProcessing(true);
			line.saveEx();
		}
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		setProcessing(true);
		return DocAction.STATUS_InProgress;
	}

	@Override
	public boolean approveIt() {
		
		return false;
	}

	@Override
	public boolean rejectIt() {
		
		return false;
	}

	@Override
	public String completeIt() {
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		if(getLines().length==0) {
			m_processMsg = "No Lines";
			return STATUS_Invalid;
		}
			
		for(MBPRMaterialRequestLine line : getLines()) {
			line.setProcessed(true);
			line.saveEx();
		}
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		setProcessed(true);
		return DocAction.STATUS_Completed;
	}

	@Override
	public boolean voidIt() {
		if (log.isLoggable(Level.INFO)) log.info(toString());
		// Before Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_VOID);
		if (m_processMsg != null)
			return false;
		
		for(MBPRMaterialRequestLine line : getLines()) {
			line.setProcessed(true);
			line.saveEx();
		}
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_VOID);
		if (m_processMsg != null)
			return false;
		
		setProcessed(true);
		setDocAction(DOCACTION_None);
		return true;
	}

	@Override
	public boolean closeIt() {
		setDocAction(DOCACTION_Close);
		return true;
	}

	@Override
	public boolean reverseCorrectIt() {
		
		return false;
	}

	@Override
	public boolean reverseAccrualIt() {
		
		return false;
	}

	@Override
	public boolean reActivateIt() {
		
		return false;
	}

	@Override
	public String getSummary() {
		
		return null;
	}

	@Override
	public String getDocumentInfo() {
		
		return getDocumentNo();
	}

	@Override
	public File createPDF() {
		
		return null;
	}

	@Override
	public String getProcessMsg() {
		return m_processMsg;
	}

	@Override
	public int getDoc_User_ID() {
		
		return 0;
	}

	@Override
	public int getC_Currency_ID() {
		
		return 0;
	}

	@Override
	public BigDecimal getApprovalAmt() {
		
		return null;
	}
	

}
