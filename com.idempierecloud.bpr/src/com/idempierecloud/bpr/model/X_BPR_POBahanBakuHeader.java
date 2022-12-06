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

/** Generated Model for BPR_POBahanBakuHeader
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_POBahanBakuHeader")
public class X_BPR_POBahanBakuHeader extends PO implements I_BPR_POBahanBakuHeader, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221206L;

    /** Standard Constructor */
    public X_BPR_POBahanBakuHeader (Properties ctx, int BPR_POBahanBakuHeader_ID, String trxName)
    {
      super (ctx, BPR_POBahanBakuHeader_ID, trxName);
      /** if (BPR_POBahanBakuHeader_ID == 0)
        {
			setBPR_POBahanBakuHeader_ID (0);
// @#AD_Org_ID@
        } */
    }

    /** Standard Constructor */
    public X_BPR_POBahanBakuHeader (Properties ctx, int BPR_POBahanBakuHeader_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_POBahanBakuHeader_ID, trxName, virtualColumns);
      /** if (BPR_POBahanBakuHeader_ID == 0)
        {
			setBPR_POBahanBakuHeader_ID (0);
// @#AD_Org_ID@
        } */
    }

    /** Load Constructor */
    public X_BPR_POBahanBakuHeader (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_POBahanBakuHeader[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Amount.
		@param Amount Amount in a defined currency
	*/
	public void setAmount (BigDecimal Amount)
	{
		set_ValueNoCheck (COLUMNNAME_Amount, Amount);
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

	/** Set BPR PO BahanBaku Header.
		@param BPR_POBahanBakuHeader_ID BPR PO BahanBaku Header
	*/
	public void setBPR_POBahanBakuHeader_ID (int BPR_POBahanBakuHeader_ID)
	{
		if (BPR_POBahanBakuHeader_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_POBahanBakuHeader_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_POBahanBakuHeader_ID, Integer.valueOf(BPR_POBahanBakuHeader_ID));
	}

	/** Get BPR PO BahanBaku Header.
		@return BPR PO BahanBaku Header	  */
	public int getBPR_POBahanBakuHeader_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_POBahanBakuHeader_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_POBahanBakuHeader_UU.
		@param BPR_POBahanBakuHeader_UU BPR_POBahanBakuHeader_UU
	*/
	public void setBPR_POBahanBakuHeader_UU (String BPR_POBahanBakuHeader_UU)
	{
		set_Value (COLUMNNAME_BPR_POBahanBakuHeader_UU, BPR_POBahanBakuHeader_UU);
	}

	/** Get BPR_POBahanBakuHeader_UU.
		@return BPR_POBahanBakuHeader_UU	  */
	public String getBPR_POBahanBakuHeader_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_POBahanBakuHeader_UU);
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
}