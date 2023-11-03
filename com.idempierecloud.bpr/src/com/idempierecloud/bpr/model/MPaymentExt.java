package com.idempierecloud.bpr.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MCash;
import org.compiere.model.MCashLine;
import org.compiere.model.MClient;
import org.compiere.model.MConversionRate;
import org.compiere.model.MConversionRateUtil;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentAllocate;
import org.compiere.model.MPeriod;
import org.compiere.model.MSysConfig;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.X_C_CashLine;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

public class MPaymentExt extends MPayment {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8699346227246789924L;

	public MPaymentExt(Properties ctx, int C_Payment_ID, String trxName) {
		super(ctx, C_Payment_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MPaymentExt(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 	Allocate It.
	 * 	Only call when there is NO allocation as it will create duplicates.
	 * 	If an invoice exists, it allocates that 
	 * 	otherwise it allocates Payment Selection.
	 *	@return true if allocated
	 */
	@Override
	public boolean allocateIt()
	{
		//	Create invoice Allocation -	See also MCash.completeIt
		if (getC_Invoice_ID() != 0)
		{	
				return allocateInvoice();
		}	
		//	Invoices of a AP Payment Selection
		if (allocatePaySelection())
			return true;
		
		if (getC_Order_ID() != 0)
			return false;
			
		//	Allocate to multiple Payments based on entry
		MPaymentAllocate[] pAllocs = MPaymentAllocate.get(this);
		if (pAllocs.length == 0)
			return false;
		
		MAllocationHdr alloca = new MAllocationHdr(getCtx(), false, 
			getDateTrx(), getC_Currency_ID(), 
				Msg.translate(getCtx(), "C_Payment_ID")	+ ": " + getDocumentNo(), 
				get_TrxName());
		MAllocationHdrExt alloc = new MAllocationHdrExt(alloca.getCtx(), alloca.getC_AllocationHdr_ID(), alloca.get_TrxName());
		alloc.setAD_Org_ID(getAD_Org_ID());
		alloc.setDateAcct(getDateAcct()); // in case date acct is different from datetrx in payment; IDEMPIERE-1532 tbayen
		
		if (!alloc.save())
		{
			log.severe("P.Allocations not created");
			return false;
		}
		//	Lines
		BigDecimal allocationAmtToCharge = Env.ZERO;
		for (int i = 0; i < pAllocs.length; i++)
		{
			MPaymentAllocate pa = pAllocs[i];

			BigDecimal allocationAmt = pa.getAmount();			//	underpayment
			if (pa.getOverUnderAmt().signum() < 0 && pa.getAmount().signum() > 0)
				allocationAmt = allocationAmt.add(pa.getOverUnderAmt());	//	overpayment (negative)

			MAllocationLine aLine = null;
			if (isReceipt())
				aLine = new MAllocationLine (alloc, allocationAmt,
					pa.getDiscountAmt(), pa.getWriteOffAmt(), pa.getOverUnderAmt());
			else
				aLine = new MAllocationLine (alloc, allocationAmt.negate(),
					pa.getDiscountAmt().negate(), pa.getWriteOffAmt().negate(), pa.getOverUnderAmt().negate());
			//aLine.setDocInfo(pa.getC_BPartner_ID(), 0, pa.getC_Invoice_ID());
			if (pa.getC_Invoice_ID() != 0) {
				aLine.setDocInfo(pa.getC_BPartner_ID(), 0, pa.getC_Invoice_ID());
				aLine.setPaymentInfo(getC_Payment_ID(), 0);
			}
			else {
				MPayment payment = new MPayment(pa.getCtx(), pa.getC_Payment_ID(), pa.get_TrxName());
				aLine.setAmount(isReceipt() ? allocationAmt.negate() : allocationAmt);
				aLine.setC_BPartner_ID(payment.getC_BPartner_ID());
				aLine.setC_Charge_ID(pa.get_ValueAsInt("C_Charge_ID"));
				
				aLine.set_ValueOfColumn("AD_Org_ID", pa.getAD_Org_ID());
				aLine.set_ValueOfColumn("Description", pa.get_Value("Description"));
				
				allocationAmtToCharge = allocationAmtToCharge.add(pa.getAmount());
			}
			if (!aLine.save(get_TrxName()))
				log.warning("P.Allocations - line not saved");
			else
			{
				pa.setC_AllocationLine_ID(aLine.getC_AllocationLine_ID());
				pa.saveEx();
			}
		}
		
		if (allocationAmtToCharge.signum()!=0) {
			MAllocationLine aLine = new MAllocationLine(alloc, isReceipt() ? allocationAmtToCharge : allocationAmtToCharge.negate(), null, null, null);
			aLine.setC_BPartner_ID(getC_BPartner_ID());
			aLine.setPaymentInfo(getC_Payment_ID(), 0);
			if (!aLine.save(get_TrxName())) {
				log.warning("P.Allocations Charge - line not saved");
			}
		}
		
		//do not post immediate alloc, alloc should post after payment
		alloc.set_Attribute(DocumentEngine.DOCUMENT_POST_IMMEDIATE_AFTER_COMPLETE, Boolean.FALSE);
		// added AdempiereException by zuhri
		if (!alloc.processIt(DocAction.ACTION_Complete))
			throw new AdempiereException(Msg.getMsg(getCtx(), "FailedProcessingDocument") + " - " + alloc.getProcessMsg());
		addDocsPostProcess(alloc);
		// end added
		m_processMsg = "@C_AllocationHdr_ID@: " + alloc.getDocumentNo();
		return alloc.save(get_TrxName());
	}	//	allocateIt	
	
	
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

		// Set the definite document number after completed (if needed)
		setDefiniteDocumentNo();

		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;

		//	Implicit Approval
		if (!isApproved())
			approveIt();
		if (log.isLoggable(Level.INFO)) log.info(toString());

		//	Charge Handling
		boolean createdAllocationRecords = false;
		if (getC_Charge_ID() != 0)
		{
			setIsAllocated(true);
		}
		else
		{
			createdAllocationRecords = allocateIt();	//	Create Allocation Records
			testAllocation();
		}

		//	Update BP for Prepayments
		if (getC_BPartner_ID() != 0 && getC_Invoice_ID() == 0 && getC_Charge_ID() == 0 && MPaymentAllocate.get(this).length == 0 && !createdAllocationRecords)
		{
			MBPartner bp = new MBPartner (getCtx(), getC_BPartner_ID(), get_TrxName());
			DB.getDatabase().forUpdate(bp, 0);
			//	Update total balance to include this payment
			BigDecimal payAmt = null;
			int baseCurrencyId = Env.getContextAsInt(getCtx(), Env.C_CURRENCY_ID);
			if (getC_Currency_ID() != baseCurrencyId && isOverrideCurrencyRate()) 
			{
				payAmt = getConvertedAmt();
			}
			else
			{
				payAmt = MConversionRate.convertBase(getCtx(), getPayAmt(), 
					getC_Currency_ID(), getDateAcct(), getC_ConversionType_ID(), getAD_Client_ID(), getAD_Org_ID());
				if (payAmt == null)
				{
					m_processMsg = MConversionRateUtil.getErrorMessage(getCtx(), "ErrorConvertingCurrencyToBaseCurrency",
							getC_Currency_ID(), MClient.get(getCtx()).getC_Currency_ID(), getC_ConversionType_ID(), getDateAcct(), get_TrxName());
					return DocAction.STATUS_Invalid;
				}
			}
			//	Total Balance
			BigDecimal newBalance = bp.getTotalOpenBalance();
			if (newBalance == null)
				newBalance = Env.ZERO;
			if (isReceipt())
				newBalance = newBalance.subtract(payAmt);
			else
				newBalance = newBalance.add(payAmt);
				
//			bp.setTotalOpenBalance(newBalance);
			bp.setSOCreditStatus();
			bp.saveEx();
		}		

		//	Counter Doc
		MPayment counter = createCounterDoc();
		if (counter != null)
			m_processMsg += " @CounterDoc@: @C_Payment_ID@=" + counter.getDocumentNo();

		// @Trifon - CashPayments
		if ( isCashbookTrx()) {
			// Create Cash Book entry
			if ( getC_CashBook_ID() <= 0 ) {
				log.saveError("Error", Msg.parseTranslation(getCtx(), "@Mandatory@: @C_CashBook_ID@"));
				m_processMsg = "@NoCashBook@";
				return DocAction.STATUS_Invalid;
			}
			MCash cash = MCash.get (getCtx(), getAD_Org_ID(), getDateAcct(), getC_Currency_ID(), get_TrxName());
			if (cash == null || cash.get_ID() == 0)
			{
				m_processMsg = "@NoCashBook@";
				return DocAction.STATUS_Invalid;
			}
			MCashLine cl = new MCashLine( cash );
			cl.setCashType( X_C_CashLine.CASHTYPE_GeneralReceipts );
			cl.setDescription("Generated From Payment #" + getDocumentNo());
			cl.setC_Currency_ID( this.getC_Currency_ID() );
			cl.setC_Payment_ID( getC_Payment_ID() ); // Set Reference to payment.
			StringBuilder info=new StringBuilder();
			info.append("Cash journal ( ")
				.append(cash.getDocumentNo()).append(" )");				
			m_processMsg = info.toString();
			//	Amount
			BigDecimal amt = this.getPayAmt();
			cl.setAmount( amt );
			//
			cl.setDiscountAmt( Env.ZERO );
			cl.setWriteOffAmt( Env.ZERO );
			cl.setIsGenerated( true );
			
			if (!cl.save(get_TrxName()))
			{
				m_processMsg = "Could not save Cash Journal Line";
				return DocAction.STATUS_Invalid;
			}
		}
		// End Trifon - CashPayments
		
		//	update C_Invoice.C_Payment_ID and C_Order.C_Payment_ID reference
		if (getC_Invoice_ID() != 0)
		{
			MInvoice inv = new MInvoice(getCtx(), getC_Invoice_ID(), get_TrxName());
			if (inv.getC_Payment_ID() != getC_Payment_ID())
			{
				inv.setC_Payment_ID(getC_Payment_ID());
				inv.saveEx();
			}
		}		
		if (getC_Order_ID() != 0)
		{
			MOrder ord = new MOrder(getCtx(), getC_Order_ID(), get_TrxName());
			if (ord.getC_Payment_ID() != getC_Payment_ID())
			{
				ord.setC_Payment_ID(getC_Payment_ID());
				ord.saveEx();
			}
		}
		
		//	User Validation
		String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocAction.STATUS_Invalid;
		}

		//
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	}	//	completeIt
	
	@Override
	protected StringBuilder reverse(boolean accrual) {
		if (!voidOnlinePayment())
			return null;
		
		//	Std Period open?
		Timestamp dateAcct = accrual ? Env.getContextAsDate(getCtx(), Env.DATE) : getDateAcct();
		if (dateAcct == null) {
			dateAcct = new Timestamp(System.currentTimeMillis());
		}
		MPeriod.testPeriodOpen(getCtx(), dateAcct, getC_DocType_ID(), getAD_Org_ID());
		
		if (getC_BankStatementLine_ID() > 0 && isReconciled()) {
			boolean allow = MSysConfig.getBooleanValue(MSysConfig.ALLOW_REVERSAL_OF_RECONCILED_PAYMENT, true, Env.getAD_Client_ID(getCtx()));
			if (!allow) {
				m_processMsg = Msg.getMsg(getCtx(), "NotAllowReversalOfReconciledPayment");
				return null;
			}
		}
		
		//	Create Reversal
		MPaymentExt reversal = new MPaymentExt (getCtx(), 0, get_TrxName());
		copyValues(this, reversal);
		reversal.setClientOrg(this);
		// reversal.setC_Order_ID(0); // IDEMPIERE-1764
		reversal.setC_Invoice_ID(0);
		reversal.setDateAcct(dateAcct);
		//
		reversal.setDocumentNo(getDocumentNo() + REVERSE_INDICATOR);	//	indicate reversals
		reversal.setDocStatus(DOCSTATUS_Drafted);
		reversal.setDocAction(DOCACTION_Complete);
		//
		reversal.setPayAmt(getPayAmt().negate());
		reversal.setDiscountAmt(getDiscountAmt().negate());
		reversal.setWriteOffAmt(getWriteOffAmt().negate());
		reversal.setOverUnderAmt(getOverUnderAmt().negate());
		//
		reversal.setIsAllocated(true);
		reversal.setIsReconciled(false);
		reversal.setIsOnline(false);
		reversal.setIsApproved(true); 
		reversal.setR_PnRef(null);
		reversal.setR_Result(null);
		reversal.setR_RespMsg(null);
		reversal.setR_AuthCode(null);
		reversal.setR_Info(null);
		reversal.setProcessing(false);
		reversal.setOProcessing("N");
		reversal.setProcessed(false);
		reversal.setPosted(false);
		reversal.setDescription(getDescription());
		reversal.addDescription("{->" + getDocumentNo() + ")");
		//FR [ 1948157  ] 
		reversal.setReversal_ID(getC_Payment_ID());
		reversal.saveEx(get_TrxName());
		//	Post Reversal
		if (!reversal.processIt(DocAction.ACTION_Complete))
		{
			m_processMsg = "Reversal ERROR: " + reversal.getProcessMsg();
			return null;
		}
		reversal.closeIt();
		reversal.setDocStatus(DOCSTATUS_Reversed);
		reversal.setDocAction(DOCACTION_None);
		reversal.saveEx(get_TrxName());

		//	Unlink & De-Allocate
		deAllocate(accrual);
		setIsAllocated (true);	//	the allocation below is overwritten
		//	Set Status 
		addDescription("(" + reversal.getDocumentNo() + "<-)");
		setDocStatus(DOCSTATUS_Reversed);
		setDocAction(DOCACTION_None);
		setProcessed(true);
		//FR [ 1948157  ] 
		setReversal_ID(reversal.getC_Payment_ID());
		
		StringBuilder info = new StringBuilder(reversal.getDocumentNo());

		//	Create automatic Allocation
		MAllocationHdr alloc = new MAllocationHdr (getCtx(), false, 
			getDateTrx(), 
			getC_Currency_ID(),
			Msg.translate(getCtx(), "C_Payment_ID")	+ ": " + reversal.getDocumentNo(), get_TrxName());
		alloc.setAD_Org_ID(getAD_Org_ID());
		alloc.setDateAcct(dateAcct); // dateAcct variable already take into account the accrual parameter
		alloc.saveEx(get_TrxName());

		//	Original Allocation
		MAllocationLine aLine = new MAllocationLine (alloc, getPayAmt(true), 
			Env.ZERO, Env.ZERO, Env.ZERO);
		aLine.setDocInfo(getC_BPartner_ID(), 0, 0);
		aLine.setPaymentInfo(getC_Payment_ID(), 0);
		if (!aLine.save(get_TrxName()))
			log.warning("Automatic allocation - line not saved");
		//	Reversal Allocation
		aLine = new MAllocationLine (alloc, reversal.getPayAmt(true), 
			Env.ZERO, Env.ZERO, Env.ZERO);
		aLine.setDocInfo(reversal.getC_BPartner_ID(), 0, 0);
		aLine.setPaymentInfo(reversal.getC_Payment_ID(), 0);
		if (!aLine.save(get_TrxName()))
			log.warning("Automatic allocation - reversal line not saved");
		
		//do not post immediate alloc
		alloc.set_Attribute(DocumentEngine.DOCUMENT_POST_IMMEDIATE_AFTER_COMPLETE, Boolean.FALSE);
		// added AdempiereException by zuhri
		if (!alloc.processIt(DocAction.ACTION_Complete))
			throw new AdempiereException(Msg.getMsg(getCtx(), "FailedProcessingDocument") + " - " + alloc.getProcessMsg());
		addDocsPostProcess(alloc);
		// end added
		alloc.saveEx(get_TrxName());
		//			
		info.append(" - @C_AllocationHdr_ID@: ").append(alloc.getDocumentNo());
		
		//	Update BPartner
		if (getC_BPartner_ID() != 0)
		{
			MBPartner bp = new MBPartner (getCtx(), getC_BPartner_ID(), get_TrxName());
//			bp.setTotalOpenBalance();
			bp.saveEx(get_TrxName());
		}
		
		return info;
	}
	
	@Override
	protected boolean allocateInvoice()
	{
		//	calculate actual allocation
		BigDecimal allocationAmt = getPayAmt();			//	underpayment
		if (getOverUnderAmt().signum() < 0 && getPayAmt().signum() > 0)
			allocationAmt = allocationAmt.add(getOverUnderAmt());	//	overpayment (negative)

		MAllocationHdr alloca = new MAllocationHdr(getCtx(), false, 
			getDateTrx(), getC_Currency_ID(),
			Msg.translate(getCtx(), "C_Payment_ID") + ": " + getDocumentNo() + " [1]", get_TrxName());
		
		MAllocationHdrExt alloc = new MAllocationHdrExt(alloca.getCtx(), alloca.getC_AllocationHdr_ID(), alloca.get_TrxName());
		alloc.setAD_Org_ID(getAD_Org_ID());
		alloc.setDateAcct(getDateAcct()); // in case date acct is different from datetrx in payment
		alloc.setC_Currency_ID(alloca.getC_Currency_ID());
		MInvoice invoice = new MInvoice(getCtx(), getC_Invoice_ID(), get_TrxName());
		if (invoice.getDateAcct().after(alloc.getDateAcct())) {
			alloc.setDateAcct(invoice.getDateAcct());
		}
		alloc.saveEx();
		MAllocationLine aLine = null;
		if (isReceipt())
			aLine = new MAllocationLine (alloc, allocationAmt, 
				getDiscountAmt(), getWriteOffAmt(), getOverUnderAmt());
		else
			aLine = new MAllocationLine (alloc, allocationAmt.negate(), 
				getDiscountAmt().negate(), getWriteOffAmt().negate(), getOverUnderAmt().negate());
		aLine.setDocInfo(getC_BPartner_ID(), 0, getC_Invoice_ID());
		aLine.setC_Payment_ID(getC_Payment_ID());
		aLine.saveEx(get_TrxName());
		//do not post immediate alloc
		alloc.set_Attribute(DocumentEngine.DOCUMENT_POST_IMMEDIATE_AFTER_COMPLETE, Boolean.FALSE);
		// added AdempiereException by zuhri
		if (!alloc.processIt(DocAction.ACTION_Complete))
			throw new AdempiereException(Msg.getMsg(getCtx(), "FailedProcessingDocument") + " - " + alloc.getProcessMsg());
		addDocsPostProcess(alloc);
		// end added
		alloc.saveEx(get_TrxName());
		m_justCreatedAllocInv = alloc;
		m_processMsg = "@C_AllocationHdr_ID@: " + alloc.getDocumentNo();
			
		//	Get Project from Invoice
		int C_Project_ID = DB.getSQLValue(get_TrxName(), 
			"SELECT MAX(C_Project_ID) FROM C_Invoice WHERE C_Invoice_ID=?", getC_Invoice_ID());
		if (C_Project_ID > 0 && getC_Project_ID() == 0)
			setC_Project_ID(C_Project_ID);
		else if (C_Project_ID > 0 && getC_Project_ID() > 0 && C_Project_ID != getC_Project_ID())
			log.warning("Invoice C_Project_ID=" + C_Project_ID 
				+ " <> Payment C_Project_ID=" + getC_Project_ID());
		return true;
	}	//	allocateInvoice
}
