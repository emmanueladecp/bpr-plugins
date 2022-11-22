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

/** Generated Model for BPR_PicklistLine
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_PicklistLine")
public class X_BPR_PicklistLine extends PO implements I_BPR_PicklistLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221122L;

    /** Standard Constructor */
    public X_BPR_PicklistLine (Properties ctx, int BPR_PicklistLine_ID, String trxName)
    {
      super (ctx, BPR_PicklistLine_ID, trxName);
      /** if (BPR_PicklistLine_ID == 0)
        {
        } */
    }

    /** Standard Constructor */
    public X_BPR_PicklistLine (Properties ctx, int BPR_PicklistLine_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_PicklistLine_ID, trxName, virtualColumns);
      /** if (BPR_PicklistLine_ID == 0)
        {
        } */
    }

    /** Load Constructor */
    public X_BPR_PicklistLine (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_PicklistLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_BPR_Picklist getBPR_Picklist() throws RuntimeException
	{
		return (I_BPR_Picklist)MTable.get(getCtx(), I_BPR_Picklist.Table_ID)
			.getPO(getBPR_Picklist_ID(), get_TrxName());
	}

	/** Set BPR_Picklist_ID.
		@param BPR_Picklist_ID BPR_Picklist_ID
	*/
	public void setBPR_Picklist_ID (int BPR_Picklist_ID)
	{
		if (BPR_Picklist_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_Picklist_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_Picklist_ID, Integer.valueOf(BPR_Picklist_ID));
	}

	/** Get BPR_Picklist_ID.
		@return BPR_Picklist_ID	  */
	public int getBPR_Picklist_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_Picklist_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_PicklistLine_ID.
		@param BPR_PicklistLine_ID BPR_PicklistLine_ID
	*/
	public void setBPR_PicklistLine_ID (int BPR_PicklistLine_ID)
	{
		if (BPR_PicklistLine_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_PicklistLine_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_PicklistLine_ID, Integer.valueOf(BPR_PicklistLine_ID));
	}

	/** Get BPR_PicklistLine_ID.
		@return BPR_PicklistLine_ID	  */
	public int getBPR_PicklistLine_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_PicklistLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_PicklistLine_UU.
		@param BPR_PicklistLine_UU BPR_PicklistLine_UU
	*/
	public void setBPR_PicklistLine_UU (String BPR_PicklistLine_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BPR_PicklistLine_UU, BPR_PicklistLine_UU);
	}

	/** Get BPR_PicklistLine_UU.
		@return BPR_PicklistLine_UU	  */
	public String getBPR_PicklistLine_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_PicklistLine_UU);
	}

	public org.compiere.model.I_C_Activity getC_Activity() throws RuntimeException
	{
		return (org.compiere.model.I_C_Activity)MTable.get(getCtx(), org.compiere.model.I_C_Activity.Table_ID)
			.getPO(getC_Activity_ID(), get_TrxName());
	}

	/** Set Department.
		@param C_Activity_ID Business Activity
	*/
	public void setC_Activity_ID (int C_Activity_ID)
	{
		if (C_Activity_ID < 1)
			set_Value (COLUMNNAME_C_Activity_ID, null);
		else
			set_Value (COLUMNNAME_C_Activity_ID, Integer.valueOf(C_Activity_ID));
	}

	/** Get Department.
		@return Business Activity
	  */
	public int getC_Activity_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Activity_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Campaign getC_Campaign() throws RuntimeException
	{
		return (org.compiere.model.I_C_Campaign)MTable.get(getCtx(), org.compiere.model.I_C_Campaign.Table_ID)
			.getPO(getC_Campaign_ID(), get_TrxName());
	}

	/** Set Campaign.
		@param C_Campaign_ID Marketing Campaign
	*/
	public void setC_Campaign_ID (int C_Campaign_ID)
	{
		if (C_Campaign_ID < 1)
			set_Value (COLUMNNAME_C_Campaign_ID, null);
		else
			set_Value (COLUMNNAME_C_Campaign_ID, Integer.valueOf(C_Campaign_ID));
	}

	/** Get Campaign.
		@return Marketing Campaign
	  */
	public int getC_Campaign_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Campaign_ID);
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

	/** Set Line.
		@param LineNo Line No
	*/
	public void setLineNo (int LineNo)
	{
		set_Value (COLUMNNAME_LineNo, Integer.valueOf(LineNo));
	}

	/** Get Line.
		@return Line No
	  */
	public int getLineNo()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LineNo);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_InOut getM_InOut() throws RuntimeException
	{
		return (org.compiere.model.I_M_InOut)MTable.get(getCtx(), org.compiere.model.I_M_InOut.Table_ID)
			.getPO(getM_InOut_ID(), get_TrxName());
	}

	/** Set Shipment/Receipt.
		@param M_InOut_ID Material Shipment Document
	*/
	public void setM_InOut_ID (int M_InOut_ID)
	{
		if (M_InOut_ID < 1)
			set_ValueNoCheck (COLUMNNAME_M_InOut_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_M_InOut_ID, Integer.valueOf(M_InOut_ID));
	}

	/** Get Shipment/Receipt.
		@return Material Shipment Document
	  */
	public int getM_InOut_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_InOut_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Movement Quantity.
		@param MovementQty Quantity of a product moved.
	*/
	public void setMovementQty (BigDecimal MovementQty)
	{
		set_ValueNoCheck (COLUMNNAME_MovementQty, MovementQty);
	}

	/** Get Movement Quantity.
		@return Quantity of a product moved.
	  */
	public BigDecimal getMovementQty()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MovementQty);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public org.compiere.model.I_M_Product getM_Product() throws RuntimeException
	{
		return (org.compiere.model.I_M_Product)MTable.get(getCtx(), org.compiere.model.I_M_Product.Table_ID)
			.getPO(getM_Product_ID(), get_TrxName());
	}

	/** Set Product.
		@param M_Product_ID Product, Service, Item
	*/
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1)
			set_Value (COLUMNNAME_M_Product_ID, null);
		else
			set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Product.
		@return Product, Service, Item
	  */
	public int getM_Product_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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
}