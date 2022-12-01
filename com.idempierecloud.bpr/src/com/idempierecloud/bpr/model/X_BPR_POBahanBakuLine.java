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
import org.compiere.util.KeyNamePair;

/** Generated Model for BPR_POBahanBakuLine
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_POBahanBakuLine")
public class X_BPR_POBahanBakuLine extends PO implements I_BPR_POBahanBakuLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221201L;

    /** Standard Constructor */
    public X_BPR_POBahanBakuLine (Properties ctx, int BPR_POBahanBakuLine_ID, String trxName)
    {
      super (ctx, BPR_POBahanBakuLine_ID, trxName);
      /** if (BPR_POBahanBakuLine_ID == 0)
        {
			setBPR_POBahanBakuLine_ID (0);
			setM_Cost_UU (null);
			setName (null);
        } */
    }

    /** Standard Constructor */
    public X_BPR_POBahanBakuLine (Properties ctx, int BPR_POBahanBakuLine_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_POBahanBakuLine_ID, trxName, virtualColumns);
      /** if (BPR_POBahanBakuLine_ID == 0)
        {
			setBPR_POBahanBakuLine_ID (0);
			setM_Cost_UU (null);
			setName (null);
        } */
    }

    /** Load Constructor */
    public X_BPR_POBahanBakuLine (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_POBahanBakuLine[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	public I_BPR_POBahanBaku getBPR_POBahanBaku() throws RuntimeException
	{
		return (I_BPR_POBahanBaku)MTable.get(getCtx(), I_BPR_POBahanBaku.Table_ID)
			.getPO(getBPR_POBahanBaku_ID(), get_TrxName());
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

	/** Set BPR_PO BahanBaku Line.
		@param BPR_POBahanBakuLine_ID BPR_PO BahanBaku Line
	*/
	public void setBPR_POBahanBakuLine_ID (int BPR_POBahanBakuLine_ID)
	{
		if (BPR_POBahanBakuLine_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_POBahanBakuLine_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_POBahanBakuLine_ID, Integer.valueOf(BPR_POBahanBakuLine_ID));
	}

	/** Get BPR_PO BahanBaku Line.
		@return BPR_PO BahanBaku Line	  */
	public int getBPR_POBahanBakuLine_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_POBahanBakuLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_POBahanBakuLine_UU.
		@param BPR_POBahanBakuLine_UU BPR_POBahanBakuLine_UU
	*/
	public void setBPR_POBahanBakuLine_UU (String BPR_POBahanBakuLine_UU)
	{
		set_Value (COLUMNNAME_BPR_POBahanBakuLine_UU, BPR_POBahanBakuLine_UU);
	}

	/** Get BPR_POBahanBakuLine_UU.
		@return BPR_POBahanBakuLine_UU	  */
	public String getBPR_POBahanBakuLine_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_POBahanBakuLine_UU);
	}

	/** Set Current Cost Price.
		@param CurrentCostPrice The currently used cost price
	*/
	public void setCurrentCostPrice (BigDecimal CurrentCostPrice)
	{
		set_Value (COLUMNNAME_CurrentCostPrice, CurrentCostPrice);
	}

	/** Get Current Cost Price.
		@return The currently used cost price
	  */
	public BigDecimal getCurrentCostPrice()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_CurrentCostPrice);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set M_Cost_UU.
		@param M_Cost_UU M_Cost_UU
	*/
	public void setM_Cost_UU (String M_Cost_UU)
	{
		set_Value (COLUMNNAME_M_Cost_UU, M_Cost_UU);
	}

	/** Get M_Cost_UU.
		@return M_Cost_UU	  */
	public String getM_Cost_UU()
	{
		return (String)get_Value(COLUMNNAME_M_Cost_UU);
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

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
    }

	/** Set New Cost Price.
		@param NewCostPrice New current cost price after processing of M_CostDetail
	*/
	public void setNewCostPrice (BigDecimal NewCostPrice)
	{
		set_ValueNoCheck (COLUMNNAME_NewCostPrice, NewCostPrice);
	}

	/** Get New Cost Price.
		@return New current cost price after processing of M_CostDetail
	  */
	public BigDecimal getNewCostPrice()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_NewCostPrice);
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
}