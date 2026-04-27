/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package com.idempierecloud.bpr.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for I_BPR_ChargeAR
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="I_BPR_ChargeAR")
public class X_I_BPR_ChargeAR extends PO implements I_I_BPR_ChargeAR, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20260421L;

    /** Standard Constructor */
    public X_I_BPR_ChargeAR (Properties ctx, int I_BPR_ChargeAR_ID, String trxName)
    {
      super (ctx, I_BPR_ChargeAR_ID, trxName);
      /** if (I_BPR_ChargeAR_ID == 0)
        {
        } */
    }

    /** Standard Constructor */
    public X_I_BPR_ChargeAR (Properties ctx, int I_BPR_ChargeAR_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, I_BPR_ChargeAR_ID, trxName, virtualColumns);
      /** if (I_BPR_ChargeAR_ID == 0)
        {
        } */
    }

    /** Load Constructor */
    public X_I_BPR_ChargeAR (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder ("X_I_BPR_ChargeAR[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException
	{
		return (org.compiere.model.I_C_BPartner)MTable.get(getCtx(), org.compiere.model.I_C_BPartner.Table_ID)
			.getPO(getC_BPartner_ID(), get_TrxName());
	}

	/** Set Business Partner .
		@param C_BPartner_ID Identifies a Business Partner
	*/
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1)
			set_Value (COLUMNNAME_C_BPartner_ID, null);
		else
			set_Value (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Business Partner .
		@return Identifies a Business Partner
	  */
	public int getC_BPartner_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Invoice getC_Invoice() throws RuntimeException
	{
		return (org.compiere.model.I_C_Invoice)MTable.get(getCtx(), org.compiere.model.I_C_Invoice.Table_ID)
			.getPO(getC_Invoice_ID(), get_TrxName());
	}

	/** Set Invoice.
		@param C_Invoice_ID Invoice Identifier
	*/
	public void setC_Invoice_ID (int C_Invoice_ID)
	{
		if (C_Invoice_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_Invoice_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_Invoice_ID, Integer.valueOf(C_Invoice_ID));
	}

	/** Get Invoice.
		@return Invoice Identifier
	  */
	public int getC_Invoice_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Invoice_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Document No.
		@param DocumentNo Document sequence number of the document
	*/
	public void setDocumentNo (String DocumentNo)
	{
		set_ValueNoCheck (COLUMNNAME_DocumentNo, DocumentNo);
	}

	/** Get Document No.
		@return Document sequence number of the document
	  */
	public String getDocumentNo()
	{
		return (String)get_Value(COLUMNNAME_DocumentNo);
	}

	/** Set I_BPR_ChargeAR.
		@param I_BPR_ChargeAR_ID I_BPR_ChargeAR
	*/
	public void setI_BPR_ChargeAR_ID (int I_BPR_ChargeAR_ID)
	{
		if (I_BPR_ChargeAR_ID < 1)
			set_ValueNoCheck (COLUMNNAME_I_BPR_ChargeAR_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_I_BPR_ChargeAR_ID, Integer.valueOf(I_BPR_ChargeAR_ID));
	}

	/** Get I_BPR_ChargeAR.
		@return I_BPR_ChargeAR	  */
	public int getI_BPR_ChargeAR_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_I_BPR_ChargeAR_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set I_BPR_ChargeAR_UU.
		@param I_BPR_ChargeAR_UU I_BPR_ChargeAR_UU
	*/
	public void setI_BPR_ChargeAR_UU (String I_BPR_ChargeAR_UU)
	{
		set_ValueNoCheck (COLUMNNAME_I_BPR_ChargeAR_UU, I_BPR_ChargeAR_UU);
	}

	/** Get I_BPR_ChargeAR_UU.
		@return I_BPR_ChargeAR_UU	  */
	public String getI_BPR_ChargeAR_UU()
	{
		return (String)get_Value(COLUMNNAME_I_BPR_ChargeAR_UU);
	}

	/** Set Import Error Message.
		@param I_ErrorMsg Messages generated from import process
	*/
	public void setI_ErrorMsg (String I_ErrorMsg)
	{
		set_Value (COLUMNNAME_I_ErrorMsg, I_ErrorMsg);
	}

	/** Get Import Error Message.
		@return Messages generated from import process
	  */
	public String getI_ErrorMsg()
	{
		return (String)get_Value(COLUMNNAME_I_ErrorMsg);
	}

	/** Set Imported.
		@param I_IsImported Has this import been processed
	*/
	public void setI_IsImported (boolean I_IsImported)
	{
		set_Value (COLUMNNAME_I_IsImported, Boolean.valueOf(I_IsImported));
	}

	/** Get Imported.
		@return Has this import been processed
	  */
	public boolean isI_IsImported()
	{
		Object oo = get_Value(COLUMNNAME_I_IsImported);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Paid.
		@param IsPaid The document is paid
	*/
	public void setIsPaid (boolean IsPaid)
	{
		set_ValueNoCheck (COLUMNNAME_IsPaid, Boolean.valueOf(IsPaid));
	}

	/** Get Paid.
		@return The document is paid
	  */
	public boolean isPaid()
	{
		Object oo = get_Value(COLUMNNAME_IsPaid);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Open Amount.
		@param OpenAmt Open item amount
	*/
	public void setOpenAmt (BigDecimal OpenAmt)
	{
		set_ValueNoCheck (COLUMNNAME_OpenAmt, OpenAmt);
	}

	/** Get Open Amount.
		@return Open item amount
	  */
	public BigDecimal getOpenAmt()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_OpenAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Processed.
		@param Processed The document has been processed
	*/
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Processed.
		@return The document has been processed
	  */
	public boolean isProcessed()
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Process Now.
		@param Processing Process Now
	*/
	public void setProcessing (boolean Processing)
	{
		set_Value (COLUMNNAME_Processing, Boolean.valueOf(Processing));
	}

	/** Get Process Now.
		@return Process Now	  */
	public boolean isProcessing()
	{
		Object oo = get_Value(COLUMNNAME_Processing);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
}