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
import org.compiere.util.KeyNamePair;

/** Generated Model for BPR_ListFakturPajak
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_ListFakturPajak")
public class X_BPR_ListFakturPajak extends PO implements I_BPR_ListFakturPajak, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221212L;

    /** Standard Constructor */
    public X_BPR_ListFakturPajak (Properties ctx, int BPR_ListFakturPajak_ID, String trxName)
    {
      super (ctx, BPR_ListFakturPajak_ID, trxName);
      /** if (BPR_ListFakturPajak_ID == 0)
        {
			setBPR_ListFakturPajak_ID (0);
			setName (null);
			setValue (null);
        } */
    }

    /** Standard Constructor */
    public X_BPR_ListFakturPajak (Properties ctx, int BPR_ListFakturPajak_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_ListFakturPajak_ID, trxName, virtualColumns);
      /** if (BPR_ListFakturPajak_ID == 0)
        {
			setBPR_ListFakturPajak_ID (0);
			setName (null);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_BPR_ListFakturPajak (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_ListFakturPajak[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
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

	/** Set BPR_ListFakturPajak_UU.
		@param BPR_ListFakturPajak_UU BPR_ListFakturPajak_UU
	*/
	public void setBPR_ListFakturPajak_UU (String BPR_ListFakturPajak_UU)
	{
		set_Value (COLUMNNAME_BPR_ListFakturPajak_UU, BPR_ListFakturPajak_UU);
	}

	/** Get BPR_ListFakturPajak_UU.
		@return BPR_ListFakturPajak_UU	  */
	public String getBPR_ListFakturPajak_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_ListFakturPajak_UU);
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

	/** Set Year.
		@param FiscalYear The Fiscal Year
	*/
	public void setFiscalYear (String FiscalYear)
	{
		set_Value (COLUMNNAME_FiscalYear, FiscalYear);
	}

	/** Get Year.
		@return The Fiscal Year
	  */
	public String getFiscalYear()
	{
		return (String)get_Value(COLUMNNAME_FiscalYear);
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

	/** 010 = 010 */
	public static final String TYPEPAJAK_010 = "010";
	/** 040 = 040 */
	public static final String TYPEPAJAK_040 = "040";
	/** 080 = 080 */
	public static final String TYPEPAJAK_080 = "080";
	/** Set Type Pajak.
		@param TypePajak Type Pajak
	*/
	public void setTypePajak (String TypePajak)
	{

		set_Value (COLUMNNAME_TypePajak, TypePajak);
	}

	/** Get Type Pajak.
		@return Type Pajak	  */
	public String getTypePajak()
	{
		return (String)get_Value(COLUMNNAME_TypePajak);
	}

	/** Set Search Key.
		@param Value Search key for the record in the format required - must be unique
	*/
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue()
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}