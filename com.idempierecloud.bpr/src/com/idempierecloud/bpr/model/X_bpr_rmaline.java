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

/** Generated Model for bpr_rmaline
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="bpr_rmaline")
public class X_bpr_rmaline extends PO implements I_bpr_rmaline, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221114L;

    /** Standard Constructor */
    public X_bpr_rmaline (Properties ctx, int bpr_rmaline_ID, String trxName)
    {
      super (ctx, bpr_rmaline_ID, trxName);
      /** if (bpr_rmaline_ID == 0)
        {
			setbpr_rmaline_ID (0);
// @#AD_Org_ID@
        } */
    }

    /** Standard Constructor */
    public X_bpr_rmaline (Properties ctx, int bpr_rmaline_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, bpr_rmaline_ID, trxName, virtualColumns);
      /** if (bpr_rmaline_ID == 0)
        {
			setbpr_rmaline_ID (0);
// @#AD_Org_ID@
        } */
    }

    /** Load Constructor */
    public X_bpr_rmaline (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_bpr_rmaline[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_BPR_RMA getBPR_RMA() throws RuntimeException
	{
		return (I_BPR_RMA)MTable.get(getCtx(), I_BPR_RMA.Table_ID)
			.getPO(getBPR_RMA_ID(), get_TrxName());
	}

	/** Set BPR RMA.
		@param BPR_RMA_ID BPR RMA
	*/
	public void setBPR_RMA_ID (int BPR_RMA_ID)
	{
		if (BPR_RMA_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_RMA_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_RMA_ID, Integer.valueOf(BPR_RMA_ID));
	}

	/** Get BPR RMA.
		@return BPR RMA	  */
	public int getBPR_RMA_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_RMA_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set bpr rma line.
		@param bpr_rmaline_ID bpr rma line
	*/
	public void setbpr_rmaline_ID (int bpr_rmaline_ID)
	{
		if (bpr_rmaline_ID < 1)
			set_ValueNoCheck (COLUMNNAME_bpr_rmaline_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_bpr_rmaline_ID, Integer.valueOf(bpr_rmaline_ID));
	}

	/** Get bpr rma line.
		@return bpr rma line	  */
	public int getbpr_rmaline_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_bpr_rmaline_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set bpr_rmaline_UU.
		@param bpr_rmaline_UU bpr_rmaline_UU
	*/
	public void setbpr_rmaline_UU (String bpr_rmaline_UU)
	{
		set_Value (COLUMNNAME_bpr_rmaline_UU, bpr_rmaline_UU);
	}

	/** Get bpr_rmaline_UU.
		@return bpr_rmaline_UU	  */
	public String getbpr_rmaline_UU()
	{
		return (String)get_Value(COLUMNNAME_bpr_rmaline_UU);
	}

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException
	{
		return (org.compiere.model.I_C_BPartner)MTable.get(getCtx(), org.compiere.model.I_C_BPartner.Table_ID)
			.getPO(getC_BPartner_ID(), get_TrxName());
	}

	/** Set Business Partner.
		@param C_BPartner_ID Identifies a Business Partner
	*/
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_BPartner_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Business Partner.
		@return Identifies a Business Partner
	  */
	public int getC_BPartner_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
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

	public org.compiere.model.I_M_InOutLine getM_InOutLine() throws RuntimeException
	{
		return (org.compiere.model.I_M_InOutLine)MTable.get(getCtx(), org.compiere.model.I_M_InOutLine.Table_ID)
			.getPO(getM_InOutLine_ID(), get_TrxName());
	}

	/** Set Shipment/Receipt Line.
		@param M_InOutLine_ID Line on Shipment or Receipt document
	*/
	public void setM_InOutLine_ID (int M_InOutLine_ID)
	{
		if (M_InOutLine_ID < 1)
			set_ValueNoCheck (COLUMNNAME_M_InOutLine_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_M_InOutLine_ID, Integer.valueOf(M_InOutLine_ID));
	}

	/** Get Shipment/Receipt Line.
		@return Line on Shipment or Receipt document
	  */
	public int getM_InOutLine_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_InOutLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	public org.compiere.model.I_M_RMA getM_RMA() throws RuntimeException
	{
		return (org.compiere.model.I_M_RMA)MTable.get(getCtx(), org.compiere.model.I_M_RMA.Table_ID)
			.getPO(getM_RMA_ID(), get_TrxName());
	}

	/** Set RMA.
		@param M_RMA_ID Return Material Authorization
	*/
	public void setM_RMA_ID (int M_RMA_ID)
	{
		if (M_RMA_ID < 1)
			set_ValueNoCheck (COLUMNNAME_M_RMA_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_M_RMA_ID, Integer.valueOf(M_RMA_ID));
	}

	/** Get RMA.
		@return Return Material Authorization
	  */
	public int getM_RMA_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_RMA_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_RMALine getM_RMALine() throws RuntimeException
	{
		return (org.compiere.model.I_M_RMALine)MTable.get(getCtx(), org.compiere.model.I_M_RMALine.Table_ID)
			.getPO(getM_RMALine_ID(), get_TrxName());
	}

	/** Set RMA Line.
		@param M_RMALine_ID Return Material Authorization Line
	*/
	public void setM_RMALine_ID (int M_RMALine_ID)
	{
		if (M_RMALine_ID < 1)
			set_ValueNoCheck (COLUMNNAME_M_RMALine_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_M_RMALine_ID, Integer.valueOf(M_RMALine_ID));
	}

	/** Get RMA Line.
		@return Return Material Authorization Line
	  */
	public int getM_RMALine_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_RMALine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Quantity.
		@param Qty Quantity
	*/
	public void setQty (BigDecimal Qty)
	{
		set_Value (COLUMNNAME_Qty, Qty);
	}

	/** Get Quantity.
		@return Quantity
	  */
	public BigDecimal getQty()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Qty);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}