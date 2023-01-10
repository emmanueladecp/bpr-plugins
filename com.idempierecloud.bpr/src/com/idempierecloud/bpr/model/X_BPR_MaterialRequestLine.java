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

/** Generated Model for BPR_MaterialRequestLine
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_MaterialRequestLine")
public class X_BPR_MaterialRequestLine extends PO implements I_BPR_MaterialRequestLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20230110L;

    /** Standard Constructor */
    public X_BPR_MaterialRequestLine (Properties ctx, int BPR_MaterialRequestLine_ID, String trxName)
    {
      super (ctx, BPR_MaterialRequestLine_ID, trxName);
      /** if (BPR_MaterialRequestLine_ID == 0)
        {
			setBPR_MaterialRequestLine_ID (0);
// @#AD_Org_ID@
        } */
    }

    /** Standard Constructor */
    public X_BPR_MaterialRequestLine (Properties ctx, int BPR_MaterialRequestLine_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_MaterialRequestLine_ID, trxName, virtualColumns);
      /** if (BPR_MaterialRequestLine_ID == 0)
        {
			setBPR_MaterialRequestLine_ID (0);
// @#AD_Org_ID@
        } */
    }

    /** Load Constructor */
    public X_BPR_MaterialRequestLine (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_MaterialRequestLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_BPR_MaterialRequest getBPR_MaterialRequest() throws RuntimeException
	{
		return (I_BPR_MaterialRequest)MTable.get(getCtx(), I_BPR_MaterialRequest.Table_ID)
			.getPO(getBPR_MaterialRequest_ID(), get_TrxName());
	}

	/** Set Material Request.
		@param BPR_MaterialRequest_ID Material Request
	*/
	public void setBPR_MaterialRequest_ID (int BPR_MaterialRequest_ID)
	{
		if (BPR_MaterialRequest_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_MaterialRequest_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_MaterialRequest_ID, Integer.valueOf(BPR_MaterialRequest_ID));
	}

	/** Get Material Request.
		@return Material Request	  */
	public int getBPR_MaterialRequest_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_MaterialRequest_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Material Request Line.
		@param BPR_MaterialRequestLine_ID Material Request Line
	*/
	public void setBPR_MaterialRequestLine_ID (int BPR_MaterialRequestLine_ID)
	{
		if (BPR_MaterialRequestLine_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_MaterialRequestLine_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_MaterialRequestLine_ID, Integer.valueOf(BPR_MaterialRequestLine_ID));
	}

	/** Get Material Request Line.
		@return Material Request Line	  */
	public int getBPR_MaterialRequestLine_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_MaterialRequestLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_MaterialRequestLine_UU.
		@param BPR_MaterialRequestLine_UU BPR_MaterialRequestLine_UU
	*/
	public void setBPR_MaterialRequestLine_UU (String BPR_MaterialRequestLine_UU)
	{
		set_Value (COLUMNNAME_BPR_MaterialRequestLine_UU, BPR_MaterialRequestLine_UU);
	}

	/** Get BPR_MaterialRequestLine_UU.
		@return BPR_MaterialRequestLine_UU	  */
	public String getBPR_MaterialRequestLine_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_MaterialRequestLine_UU);
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

	/** Set Line No.
		@param Line Unique line for this document
	*/
	public void setLine (int Line)
	{
		set_ValueNoCheck (COLUMNNAME_Line, Integer.valueOf(Line));
	}

	/** Get Line No.
		@return Unique line for this document
	  */
	public int getLine()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Line);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_Locator getM_LocatorToAlias() throws RuntimeException
	{
		return (org.compiere.model.I_M_Locator)MTable.get(getCtx(), org.compiere.model.I_M_Locator.Table_ID)
			.getPO(getM_LocatorToAlias_ID(), get_TrxName());
	}

	/** Set Locator To  Alias.
		@param M_LocatorToAlias_ID Locator To  Alias
	*/
	public void setM_LocatorToAlias_ID (int M_LocatorToAlias_ID)
	{
		if (M_LocatorToAlias_ID < 1)
			set_Value (COLUMNNAME_M_LocatorToAlias_ID, null);
		else
			set_Value (COLUMNNAME_M_LocatorToAlias_ID, Integer.valueOf(M_LocatorToAlias_ID));
	}

	/** Get Locator To  Alias.
		@return Locator To  Alias	  */
	public int getM_LocatorToAlias_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_LocatorToAlias_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_MovementLine getM_MovementLine() throws RuntimeException
	{
		return (org.compiere.model.I_M_MovementLine)MTable.get(getCtx(), org.compiere.model.I_M_MovementLine.Table_ID)
			.getPO(getM_MovementLine_ID(), get_TrxName());
	}

	/** Set Move Line.
		@param M_MovementLine_ID Inventory Move document Line
	*/
	public void setM_MovementLine_ID (int M_MovementLine_ID)
	{
		if (M_MovementLine_ID < 1)
			set_Value (COLUMNNAME_M_MovementLine_ID, null);
		else
			set_Value (COLUMNNAME_M_MovementLine_ID, Integer.valueOf(M_MovementLine_ID));
	}

	/** Get Move Line.
		@return Inventory Move document Line
	  */
	public int getM_MovementLine_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_MovementLine_ID);
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