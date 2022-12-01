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

/** Generated Model for BPR_POBahanBaku
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_POBahanBaku")
public class X_BPR_POBahanBaku extends PO implements I_BPR_POBahanBaku, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221201L;

    /** Standard Constructor */
    public X_BPR_POBahanBaku (Properties ctx, int BPR_POBahanBaku_ID, String trxName)
    {
      super (ctx, BPR_POBahanBaku_ID, trxName);
      /** if (BPR_POBahanBaku_ID == 0)
        {
			setBPR_POBahanBaku_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_BPR_POBahanBaku (Properties ctx, int BPR_POBahanBaku_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_POBahanBaku_ID, trxName, virtualColumns);
      /** if (BPR_POBahanBaku_ID == 0)
        {
			setBPR_POBahanBaku_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BPR_POBahanBaku (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_POBahanBaku[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	/** Set Amount.
		@param Amount Amount in a defined currency
	*/
	public void setAmount (BigDecimal Amount)
	{
		set_Value (COLUMNNAME_Amount, Amount);
	}

	/** Get Amount.
		@return Amount in a defined currency
	  */
	public BigDecimal getAmount()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Amount);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set BPR PO BahanBaku.
		@param BPR_POBahanBaku_ID BPR PO BahanBaku
	*/
	public void setBPR_POBahanBaku_ID (int BPR_POBahanBaku_ID)
	{
		if (BPR_POBahanBaku_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_POBahanBaku_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_POBahanBaku_ID, Integer.valueOf(BPR_POBahanBaku_ID));
	}

	/** Get BPR PO BahanBaku.
		@return BPR PO BahanBaku	  */
	public int getBPR_POBahanBaku_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_POBahanBaku_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_POBahanBaku_UU.
		@param BPR_POBahanBaku_UU BPR_POBahanBaku_UU
	*/
	public void setBPR_POBahanBaku_UU (String BPR_POBahanBaku_UU)
	{
		set_Value (COLUMNNAME_BPR_POBahanBaku_UU, BPR_POBahanBaku_UU);
	}

	/** Get BPR_POBahanBaku_UU.
		@return BPR_POBahanBaku_UU	  */
	public String getBPR_POBahanBaku_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_POBahanBaku_UU);
	}

	/** CostingMethod AD_Reference_ID=122 */
	public static final int COSTINGMETHOD_AD_Reference_ID=122;
	/** Average PO = A */
	public static final String COSTINGMETHOD_AveragePO = "A";
	/** Fifo = F */
	public static final String COSTINGMETHOD_Fifo = "F";
	/** Last Invoice = i */
	public static final String COSTINGMETHOD_LastInvoice = "i";
	/** Average Invoice = I */
	public static final String COSTINGMETHOD_AverageInvoice = "I";
	/** Lifo = L */
	public static final String COSTINGMETHOD_Lifo = "L";
	/** Last PO Price = p */
	public static final String COSTINGMETHOD_LastPOPrice = "p";
	/** Standard Costing = S */
	public static final String COSTINGMETHOD_StandardCosting = "S";
	/** User Defined = U */
	public static final String COSTINGMETHOD_UserDefined = "U";
	/** _ = x */
	public static final String COSTINGMETHOD__ = "x";
	/** Set Costing Method.
		@param CostingMethod Indicates how Costs will be calculated
	*/
	public void setCostingMethod (String CostingMethod)
	{

		set_ValueNoCheck (COLUMNNAME_CostingMethod, CostingMethod);
	}

	/** Get Costing Method.
		@return Indicates how Costs will be calculated
	  */
	public String getCostingMethod()
	{
		return (String)get_Value(COLUMNNAME_CostingMethod);
	}

	public org.compiere.model.I_C_Period getC_Period() throws RuntimeException
	{
		return (org.compiere.model.I_C_Period)MTable.get(getCtx(), org.compiere.model.I_C_Period.Table_ID)
			.getPO(getC_Period_ID(), get_TrxName());
	}

	/** Set Period.
		@param C_Period_ID Period of the Calendar
	*/
	public void setC_Period_ID (int C_Period_ID)
	{
		if (C_Period_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_Period_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_Period_ID, Integer.valueOf(C_Period_ID));
	}

	/** Get Period.
		@return Period of the Calendar
	  */
	public int getC_Period_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Period_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Description.
		@param Description Optional short description of the record
	*/
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription()
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Name.
		@param Name Alphanumeric identifier of the entity
	*/
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName()
	{
		return (String)get_Value(COLUMNNAME_Name);
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