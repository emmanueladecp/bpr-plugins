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

/** Generated Model for BPR_Village
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_Village")
public class X_BPR_Village extends PO implements I_BPR_Village, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20240418L;

    /** Standard Constructor */
    public X_BPR_Village (Properties ctx, int BPR_Village_ID, String trxName)
    {
      super (ctx, BPR_Village_ID, trxName);
      /** if (BPR_Village_ID == 0)
        {
			setBPR_District_ID (0);
			setBPR_Village_ID (0);
			setName (null);
        } */
    }

    /** Standard Constructor */
    public X_BPR_Village (Properties ctx, int BPR_Village_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_Village_ID, trxName, virtualColumns);
      /** if (BPR_Village_ID == 0)
        {
			setBPR_District_ID (0);
			setBPR_Village_ID (0);
			setName (null);
        } */
    }

    /** Load Constructor */
    public X_BPR_Village (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 6 - System - Client 
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
      StringBuilder sb = new StringBuilder ("X_BPR_Village[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	public I_BPR_District getBPR_District() throws RuntimeException
	{
		return (I_BPR_District)MTable.get(getCtx(), I_BPR_District.Table_ID)
			.getPO(getBPR_District_ID(), get_TrxName());
	}

	/** Set District.
		@param BPR_District_ID District
	*/
	public void setBPR_District_ID (int BPR_District_ID)
	{
		if (BPR_District_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_District_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_District_ID, Integer.valueOf(BPR_District_ID));
	}

	/** Get District.
		@return District	  */
	public int getBPR_District_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_District_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Village.
		@param BPR_Village_ID Village
	*/
	public void setBPR_Village_ID (int BPR_Village_ID)
	{
		if (BPR_Village_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_Village_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_Village_ID, Integer.valueOf(BPR_Village_ID));
	}

	/** Get Village.
		@return Village	  */
	public int getBPR_Village_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_Village_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_Village_UU.
		@param BPR_Village_UU BPR_Village_UU
	*/
	public void setBPR_Village_UU (String BPR_Village_UU)
	{
		set_Value (COLUMNNAME_BPR_Village_UU, BPR_Village_UU);
	}

	/** Get BPR_Village_UU.
		@return BPR_Village_UU	  */
	public String getBPR_Village_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_Village_UU);
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