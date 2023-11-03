package com.idempierecloud.bpr.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.process.DocAction;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MAllocationHdrExt extends MAllocationHdr{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5467484624583309835L;
	public MAllocationHdrExt(Properties ctx, int C_AllocationHdr_ID, String trxName) {
		super(ctx, C_AllocationHdr_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	public MAllocationHdrExt(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	private boolean m_reversal = false;
	private String		m_processMsg = null;
	/**	Just Prepared Flag			*/
	private boolean		m_justPrepared = false;
	
	
	
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

		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		//	Implicit Approval
		if (!isApproved())
			approveIt();
		if (log.isLoggable(Level.INFO)) log.info(toString());

		//	Link
		getLines(false);
		if(!updateBP())
			return DocAction.STATUS_Invalid;
		
		for (int i = 0; i < getLines(false).length; i++)
		{
			MAllocationLine lined = getLines(false)[i];
			MAllocationLineExt line = new MAllocationLineExt(lined.getCtx(), lined.getC_AllocationLine_ID(), lined.get_TrxName());
			line.processIt(isReversal());
		}		

		//	User Validation
		String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocAction.STATUS_Invalid;
		}

		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	}	//	completeIt
	
	private boolean updateBP()
	{
		List<Integer> bps = new ArrayList<Integer>();
		getLines(false);
		for (MAllocationLine line : getLines(false)) {
			int C_BPartner_ID = line.getC_BPartner_ID();
			if (! bps.contains(C_BPartner_ID)) {
				bps.add(C_BPartner_ID);
				MBPartner bpartner = new MBPartner(Env.getCtx(), C_BPartner_ID, get_TrxName());
//				bpartner.setTotalOpenBalance();
				bpartner.saveEx();
			}
		} // for all lines
		return true;
	}	//	updateBP
	
	private boolean isReversal()
	{
		return m_reversal;
	}	//	isReversal
	
}
