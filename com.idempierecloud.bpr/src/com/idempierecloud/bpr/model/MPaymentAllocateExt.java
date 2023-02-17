package com.idempierecloud.bpr.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.MInvoice;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentAllocate;
import org.compiere.util.Msg;

public class MPaymentAllocateExt extends MPaymentAllocate {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8535136048333922173L;

	/**	The Invoice				*/
	private MInvoice	m_invoice = null;	
	
	public MPaymentAllocateExt(Properties ctx, int C_PaymentAllocate_ID, String trxName) {
		super(ctx, C_PaymentAllocate_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MPaymentAllocateExt(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		MPayment payment = new MPayment (getCtx(), getC_Payment_ID(), get_TrxName());
		if ((newRecord || is_ValueChanged("C_Invoice_ID"))
			&& (payment.getC_Charge_ID() != 0 
				|| payment.getC_Invoice_ID() != 0 
				|| payment.getC_Order_ID() != 0))
		{
			log.saveError("PaymentIsAllocated", "");
			return false;
		}
		
		BigDecimal check = getAmount()
			.add(getDiscountAmt())
			.add(getWriteOffAmt())
			.add(getOverUnderAmt());
		if ((check.compareTo(getInvoiceAmt()) != 0)  && getC_Invoice_ID() != 0)
		{
			log.saveError("Error", Msg.parseTranslation(getCtx(), 
				"@InvoiceAmt@(" + getInvoiceAmt()
				+ ") <> @Totals@(" + check + ")"));
			return false;
		}
		
		//	Org
		if ((newRecord || is_ValueChanged("C_Invoice_ID")) && (get_ValueAsInt("C_Charge_ID")==0))
		{
			getInvoice();
			if (m_invoice != null)
				setAD_Org_ID(m_invoice.getAD_Org_ID());
		}
		
		return true;
	}	//	beforeSave
		
}
