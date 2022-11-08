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

/** Generated Model for BPR_OngkosAngkutDetail
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_OngkosAngkutDetail")
public class X_BPR_OngkosAngkutDetail extends PO implements I_BPR_OngkosAngkutDetail, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221108L;

    /** Standard Constructor */
    public X_BPR_OngkosAngkutDetail (Properties ctx, int BPR_OngkosAngkutDetail_ID, String trxName)
    {
      super (ctx, BPR_OngkosAngkutDetail_ID, trxName);
      /** if (BPR_OngkosAngkutDetail_ID == 0)
        {
			setBPR_OngkosAngkutDetail_ID (0);
// @#AD_Org_ID@
        } */
    }

    /** Standard Constructor */
    public X_BPR_OngkosAngkutDetail (Properties ctx, int BPR_OngkosAngkutDetail_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_OngkosAngkutDetail_ID, trxName, virtualColumns);
      /** if (BPR_OngkosAngkutDetail_ID == 0)
        {
			setBPR_OngkosAngkutDetail_ID (0);
// @#AD_Org_ID@
        } */
    }

    /** Load Constructor */
    public X_BPR_OngkosAngkutDetail (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_OngkosAngkutDetail[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	/** Set Ongkos Angkut Detail.
		@param BPR_OngkosAngkutDetail_ID Ongkos Angkut Detail
	*/
	public void setBPR_OngkosAngkutDetail_ID (int BPR_OngkosAngkutDetail_ID)
	{
		if (BPR_OngkosAngkutDetail_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_OngkosAngkutDetail_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_OngkosAngkutDetail_ID, Integer.valueOf(BPR_OngkosAngkutDetail_ID));
	}

	/** Get Ongkos Angkut Detail.
		@return Ongkos Angkut Detail	  */
	public int getBPR_OngkosAngkutDetail_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_OngkosAngkutDetail_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_OngkosAngkutDetail_UU.
		@param BPR_OngkosAngkutDetail_UU BPR_OngkosAngkutDetail_UU
	*/
	public void setBPR_OngkosAngkutDetail_UU (String BPR_OngkosAngkutDetail_UU)
	{
		set_Value (COLUMNNAME_BPR_OngkosAngkutDetail_UU, BPR_OngkosAngkutDetail_UU);
	}

	/** Get BPR_OngkosAngkutDetail_UU.
		@return BPR_OngkosAngkutDetail_UU	  */
	public String getBPR_OngkosAngkutDetail_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_OngkosAngkutDetail_UU);
	}

	public org.compiere.model.I_C_City getC_City() throws RuntimeException
	{
		return (org.compiere.model.I_C_City)MTable.get(getCtx(), org.compiere.model.I_C_City.Table_ID)
			.getPO(getC_City_ID(), get_TrxName());
	}

	/** Set City.
		@param C_City_ID City
	*/
	public void setC_City_ID (int C_City_ID)
	{
		if (C_City_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_City_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_City_ID, Integer.valueOf(C_City_ID));
	}

	/** Get City.
		@return City
	  */
	public int getC_City_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_City_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Country getC_Country() throws RuntimeException
	{
		return (org.compiere.model.I_C_Country)MTable.get(getCtx(), org.compiere.model.I_C_Country.Table_ID)
			.getPO(getC_Country_ID(), get_TrxName());
	}

	/** Set Country.
		@param C_Country_ID Country 
	*/
	public void setC_Country_ID (int C_Country_ID)
	{
		if (C_Country_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_Country_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_Country_ID, Integer.valueOf(C_Country_ID));
	}

	/** Get Country.
		@return Country 
	  */
	public int getC_Country_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Country_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Region getC_Region() throws RuntimeException
	{
		return (org.compiere.model.I_C_Region)MTable.get(getCtx(), org.compiere.model.I_C_Region.Table_ID)
			.getPO(getC_Region_ID(), get_TrxName());
	}

	/** Set Region.
		@param C_Region_ID Identifies a geographical Region
	*/
	public void setC_Region_ID (int C_Region_ID)
	{
		if (C_Region_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_Region_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_Region_ID, Integer.valueOf(C_Region_ID));
	}

	/** Get Region.
		@return Identifies a geographical Region
	  */
	public int getC_Region_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Region_ID);
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

	/** Set Ongkos Angkut.
		@param OngkosAngkut Ongkos Angkut
	*/
	public void setOngkosAngkut (BigDecimal OngkosAngkut)
	{
		set_Value (COLUMNNAME_OngkosAngkut, OngkosAngkut);
	}

	/** Get Ongkos Angkut.
		@return Ongkos Angkut	  */
	public BigDecimal getOngkosAngkut()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_OngkosAngkut);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}