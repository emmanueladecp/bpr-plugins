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

/** Generated Model for BPR_Timbangan
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_Timbangan")
public class X_BPR_Timbangan extends PO implements I_BPR_Timbangan, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221115L;

    /** Standard Constructor */
    public X_BPR_Timbangan (Properties ctx, int BPR_Timbangan_ID, String trxName)
    {
      super (ctx, BPR_Timbangan_ID, trxName);
      /** if (BPR_Timbangan_ID == 0)
        {
			setBPR_Timbangan_ID (0);
			setValue (null);
        } */
    }

    /** Standard Constructor */
    public X_BPR_Timbangan (Properties ctx, int BPR_Timbangan_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_Timbangan_ID, trxName, virtualColumns);
      /** if (BPR_Timbangan_ID == 0)
        {
			setBPR_Timbangan_ID (0);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_BPR_Timbangan (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_Timbangan[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Nomor Kendaraan.
		@param BPR_NoKendaraan Nomor Kendaraan
	*/
	public void setBPR_NoKendaraan (String BPR_NoKendaraan)
	{
		set_Value (COLUMNNAME_BPR_NoKendaraan, BPR_NoKendaraan);
	}

	/** Get Nomor Kendaraan.
		@return Nomor Kendaraan	  */
	public String getBPR_NoKendaraan()
	{
		return (String)get_Value(COLUMNNAME_BPR_NoKendaraan);
	}

	/** Set BPR_Timbangan.
		@param BPR_Timbangan_ID BPR_Timbangan
	*/
	public void setBPR_Timbangan_ID (int BPR_Timbangan_ID)
	{
		if (BPR_Timbangan_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_Timbangan_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_Timbangan_ID, Integer.valueOf(BPR_Timbangan_ID));
	}

	/** Get BPR_Timbangan.
		@return BPR_Timbangan	  */
	public int getBPR_Timbangan_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_Timbangan_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_Timbangan_UU.
		@param BPR_Timbangan_UU BPR_Timbangan_UU
	*/
	public void setBPR_Timbangan_UU (String BPR_Timbangan_UU)
	{
		set_Value (COLUMNNAME_BPR_Timbangan_UU, BPR_Timbangan_UU);
	}

	/** Get BPR_Timbangan_UU.
		@return BPR_Timbangan_UU	  */
	public String getBPR_Timbangan_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_Timbangan_UU);
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

	public org.compiere.model.I_M_Warehouse getM_Warehouse() throws RuntimeException
	{
		return (org.compiere.model.I_M_Warehouse)MTable.get(getCtx(), org.compiere.model.I_M_Warehouse.Table_ID)
			.getPO(getM_Warehouse_ID(), get_TrxName());
	}

	/** Set Warehouse.
		@param M_Warehouse_ID Storage Warehouse and Service Point
	*/
	public void setM_Warehouse_ID (int M_Warehouse_ID)
	{
		if (M_Warehouse_ID < 1)
			set_ValueNoCheck (COLUMNNAME_M_Warehouse_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_M_Warehouse_ID, Integer.valueOf(M_Warehouse_ID));
	}

	/** Get Warehouse.
		@return Storage Warehouse and Service Point
	  */
	public int getM_Warehouse_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Warehouse_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Timbangan Net Amt.
		@param TimbanganNetAmt Timbangan Net Amt
	*/
	public void setTimbanganNetAmt (BigDecimal TimbanganNetAmt)
	{
		set_Value (COLUMNNAME_TimbanganNetAmt, TimbanganNetAmt);
	}

	/** Get Timbangan Net Amt.
		@return Timbangan Net Amt	  */
	public BigDecimal getTimbanganNetAmt()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TimbanganNetAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Timbang Isi.
		@param TimbangIsi Timbang Isi
	*/
	public void setTimbangIsi (BigDecimal TimbangIsi)
	{
		set_Value (COLUMNNAME_TimbangIsi, TimbangIsi);
	}

	/** Get Timbang Isi.
		@return Timbang Isi	  */
	public BigDecimal getTimbangIsi()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TimbangIsi);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Timbang Kosong.
		@param TimbangKosong Timbang Kosong
	*/
	public void setTimbangKosong (BigDecimal TimbangKosong)
	{
		set_Value (COLUMNNAME_TimbangKosong, TimbangKosong);
	}

	/** Get Timbang Kosong.
		@return Timbang Kosong	  */
	public BigDecimal getTimbangKosong()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TimbangKosong);
		if (bd == null)
			 return Env.ZERO;
		return bd;
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

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getValue());
    }
}