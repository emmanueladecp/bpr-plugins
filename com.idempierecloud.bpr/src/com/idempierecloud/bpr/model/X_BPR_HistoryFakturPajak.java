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

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for BPR_HistoryFakturPajak
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_HistoryFakturPajak")
public class X_BPR_HistoryFakturPajak extends PO implements I_BPR_HistoryFakturPajak, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221212L;

    /** Standard Constructor */
    public X_BPR_HistoryFakturPajak (Properties ctx, int BPR_HistoryFakturPajak_ID, String trxName)
    {
      super (ctx, BPR_HistoryFakturPajak_ID, trxName);
      /** if (BPR_HistoryFakturPajak_ID == 0)
        {
			setBPR_HistoryFakturPajak_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_BPR_HistoryFakturPajak (Properties ctx, int BPR_HistoryFakturPajak_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_HistoryFakturPajak_ID, trxName, virtualColumns);
      /** if (BPR_HistoryFakturPajak_ID == 0)
        {
			setBPR_HistoryFakturPajak_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BPR_HistoryFakturPajak (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_HistoryFakturPajak[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set History Faktur Pajak.
		@param BPR_HistoryFakturPajak_ID History Faktur Pajak
	*/
	public void setBPR_HistoryFakturPajak_ID (int BPR_HistoryFakturPajak_ID)
	{
		if (BPR_HistoryFakturPajak_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_HistoryFakturPajak_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_HistoryFakturPajak_ID, Integer.valueOf(BPR_HistoryFakturPajak_ID));
	}

	/** Get History Faktur Pajak.
		@return History Faktur Pajak	  */
	public int getBPR_HistoryFakturPajak_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_HistoryFakturPajak_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_HistoryFakturPajak_UU.
		@param BPR_HistoryFakturPajak_UU BPR_HistoryFakturPajak_UU
	*/
	public void setBPR_HistoryFakturPajak_UU (String BPR_HistoryFakturPajak_UU)
	{
		set_Value (COLUMNNAME_BPR_HistoryFakturPajak_UU, BPR_HistoryFakturPajak_UU);
	}

	/** Get BPR_HistoryFakturPajak_UU.
		@return BPR_HistoryFakturPajak_UU	  */
	public String getBPR_HistoryFakturPajak_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_HistoryFakturPajak_UU);
	}

	public I_BPR_ListFakturPajak getBPR_ListFakturPajak() throws RuntimeException
	{
		return (I_BPR_ListFakturPajak)MTable.get(getCtx(), I_BPR_ListFakturPajak.Table_ID)
			.getPO(getBPR_ListFakturPajak_ID(), get_TrxName());
	}

	/** Set BPR_ListFakturPajak.
		@param BPR_ListFakturPajak_ID BPR_ListFakturPajak
	*/
	public void setBPR_ListFakturPajak_ID (int BPR_ListFakturPajak_ID)
	{
		if (BPR_ListFakturPajak_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_ListFakturPajak_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_ListFakturPajak_ID, Integer.valueOf(BPR_ListFakturPajak_ID));
	}

	/** Get BPR_ListFakturPajak.
		@return BPR_ListFakturPajak	  */
	public int getBPR_ListFakturPajak_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_ListFakturPajak_ID);
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

	/** Set Is Uploaded.
		@param IsUploaded Is Uploaded
	*/
	public void setIsUploaded (boolean IsUploaded)
	{
		set_Value (COLUMNNAME_IsUploaded, Boolean.valueOf(IsUploaded));
	}

	/** Get Is Uploaded.
		@return Is Uploaded	  */
	public boolean isUploaded()
	{
		Object oo = get_Value(COLUMNNAME_IsUploaded);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
}