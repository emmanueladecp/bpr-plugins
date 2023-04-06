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

/** Generated Model for BPR_Historysales
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_Historysales")
public class X_BPR_Historysales extends PO implements I_BPR_Historysales, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20230328L;

    /** Standard Constructor */
    public X_BPR_Historysales (Properties ctx, int BPR_Historysales_ID, String trxName)
    {
      super (ctx, BPR_Historysales_ID, trxName);
      /** if (BPR_Historysales_ID == 0)
        {
        } */
    }

    /** Standard Constructor */
    public X_BPR_Historysales (Properties ctx, int BPR_Historysales_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_Historysales_ID, trxName, virtualColumns);
      /** if (BPR_Historysales_ID == 0)
        {
        } */
    }

    /** Load Constructor */
    public X_BPR_Historysales (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_Historysales[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set BPR_Historysales_ID.
		@param BPR_Historysales_ID BPR_Historysales_ID
	*/
	public void setBPR_Historysales_ID (int BPR_Historysales_ID)
	{
		if (BPR_Historysales_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_Historysales_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_Historysales_ID, Integer.valueOf(BPR_Historysales_ID));
	}

	/** Get BPR_Historysales_ID.
		@return BPR_Historysales_ID	  */
	public int getBPR_Historysales_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_Historysales_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_Historysales_UU.
		@param BPR_Historysales_UU BPR_Historysales_UU
	*/
	public void setBPR_Historysales_UU (String BPR_Historysales_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BPR_Historysales_UU, BPR_Historysales_UU);
	}

	/** Get BPR_Historysales_UU.
		@return BPR_Historysales_UU	  */
	public String getBPR_Historysales_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_Historysales_UU);
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

	public org.compiere.model.I_AD_User getSalesRep() throws RuntimeException
	{
		return (org.compiere.model.I_AD_User)MTable.get(getCtx(), org.compiere.model.I_AD_User.Table_ID)
			.getPO(getSalesRep_ID(), get_TrxName());
	}

	/** Set Sales Representative.
		@param SalesRep_ID Sales Representative or Company Agent
	*/
	public void setSalesRep_ID (int SalesRep_ID)
	{
		if (SalesRep_ID < 1)
			set_ValueNoCheck (COLUMNNAME_SalesRep_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_SalesRep_ID, Integer.valueOf(SalesRep_ID));
	}

	/** Get Sales Representative.
		@return Sales Representative or Company Agent
	  */
	public int getSalesRep_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SalesRep_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_User getSalesRepNew() throws RuntimeException
	{
		return (org.compiere.model.I_AD_User)MTable.get(getCtx(), org.compiere.model.I_AD_User.Table_ID)
			.getPO(getSalesRepNew_ID(), get_TrxName());
	}

	/** Set Sales Representative new.
		@param SalesRepNew_ID Sales Representative or Company Agent (New)
	*/
	public void setSalesRepNew_ID (int SalesRepNew_ID)
	{
		if (SalesRepNew_ID < 1)
			set_Value (COLUMNNAME_SalesRepNew_ID, null);
		else
			set_Value (COLUMNNAME_SalesRepNew_ID, Integer.valueOf(SalesRepNew_ID));
	}

	/** Get Sales Representative new.
		@return Sales Representative or Company Agent (New)
	  */
	public int getSalesRepNew_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SalesRepNew_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}