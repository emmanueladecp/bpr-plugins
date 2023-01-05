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
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for BPR_QualityControl
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="BPR_QualityControl")
public class X_BPR_QualityControl extends PO implements I_BPR_QualityControl, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20230105L;

    /** Standard Constructor */
    public X_BPR_QualityControl (Properties ctx, int BPR_QualityControl_ID, String trxName)
    {
      super (ctx, BPR_QualityControl_ID, trxName);
      /** if (BPR_QualityControl_ID == 0)
        {
        } */
    }

    /** Standard Constructor */
    public X_BPR_QualityControl (Properties ctx, int BPR_QualityControl_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, BPR_QualityControl_ID, trxName, virtualColumns);
      /** if (BPR_QualityControl_ID == 0)
        {
        } */
    }

    /** Load Constructor */
    public X_BPR_QualityControl (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BPR_QualityControl[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set AsalGabah.
		@param AsalGabah AsalGabah
	*/
	public void setAsalGabah (String AsalGabah)
	{
		set_Value (COLUMNNAME_AsalGabah, AsalGabah);
	}

	/** Get AsalGabah.
		@return AsalGabah	  */
	public String getAsalGabah()
	{
		return (String)get_Value(COLUMNNAME_AsalGabah);
	}

	/** Set BerasKepala.
		@param BerasKepala BerasKepala
	*/
	public void setBerasKepala (String BerasKepala)
	{
		set_Value (COLUMNNAME_BerasKepala, BerasKepala);
	}

	/** Get BerasKepala.
		@return BerasKepala	  */
	public String getBerasKepala()
	{
		return (String)get_Value(COLUMNNAME_BerasKepala);
	}

	/** Set BerasPatahan.
		@param BerasPatahan BerasPatahan
	*/
	public void setBerasPatahan (String BerasPatahan)
	{
		set_Value (COLUMNNAME_BerasPatahan, BerasPatahan);
	}

	/** Get BerasPatahan.
		@return BerasPatahan	  */
	public String getBerasPatahan()
	{
		return (String)get_Value(COLUMNNAME_BerasPatahan);
	}

	/** Set BerasSinar.
		@param BerasSinar BerasSinar
	*/
	public void setBerasSinar (String BerasSinar)
	{
		set_Value (COLUMNNAME_BerasSinar, BerasSinar);
	}

	/** Get BerasSinar.
		@return BerasSinar	  */
	public String getBerasSinar()
	{
		return (String)get_Value(COLUMNNAME_BerasSinar);
	}

	/** Set BPR_Mesin.
		@param BPR_Mesin BPR_Mesin
	*/
	public void setBPR_Mesin (String BPR_Mesin)
	{
		set_Value (COLUMNNAME_BPR_Mesin, BPR_Mesin);
	}

	/** Get BPR_Mesin.
		@return BPR_Mesin	  */
	public String getBPR_Mesin()
	{
		return (String)get_Value(COLUMNNAME_BPR_Mesin);
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

	/** Set BPR_QualityControl_ID.
		@param BPR_QualityControl_ID BPR_QualityControl_ID
	*/
	public void setBPR_QualityControl_ID (int BPR_QualityControl_ID)
	{
		if (BPR_QualityControl_ID < 1)
			set_ValueNoCheck (COLUMNNAME_BPR_QualityControl_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_BPR_QualityControl_ID, Integer.valueOf(BPR_QualityControl_ID));
	}

	/** Get BPR_QualityControl_ID.
		@return BPR_QualityControl_ID	  */
	public int getBPR_QualityControl_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BPR_QualityControl_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BPR_QualityControl_UU.
		@param BPR_QualityControl_UU BPR_QualityControl_UU
	*/
	public void setBPR_QualityControl_UU (String BPR_QualityControl_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BPR_QualityControl_UU, BPR_QualityControl_UU);
	}

	/** Get BPR_QualityControl_UU.
		@return BPR_QualityControl_UU	  */
	public String getBPR_QualityControl_UU()
	{
		return (String)get_Value(COLUMNNAME_BPR_QualityControl_UU);
	}

	public I_BPR_Timbangan getBPR_Timbangan() throws RuntimeException
	{
		return (I_BPR_Timbangan)MTable.get(getCtx(), I_BPR_Timbangan.Table_ID)
			.getPO(getBPR_Timbangan_ID(), get_TrxName());
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

	/** Set Broken.
		@param Broken Broken
	*/
	public void setBroken (String Broken)
	{
		set_Value (COLUMNNAME_Broken, Broken);
	}

	/** Get Broken.
		@return Broken	  */
	public String getBroken()
	{
		return (String)get_Value(COLUMNNAME_Broken);
	}

	public org.compiere.model.I_C_Activity getC_Activity() throws RuntimeException
	{
		return (org.compiere.model.I_C_Activity)MTable.get(getCtx(), org.compiere.model.I_C_Activity.Table_ID)
			.getPO(getC_Activity_ID(), get_TrxName());
	}

	/** Set Department.
		@param C_Activity_ID Business Activity
	*/
	public void setC_Activity_ID (int C_Activity_ID)
	{
		if (C_Activity_ID < 1)
			set_Value (COLUMNNAME_C_Activity_ID, null);
		else
			set_Value (COLUMNNAME_C_Activity_ID, Integer.valueOf(C_Activity_ID));
	}

	/** Get Department.
		@return Business Activity
	  */
	public int getC_Activity_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Activity_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	public org.compiere.model.I_C_Campaign getC_Campaign() throws RuntimeException
	{
		return (org.compiere.model.I_C_Campaign)MTable.get(getCtx(), org.compiere.model.I_C_Campaign.Table_ID)
			.getPO(getC_Campaign_ID(), get_TrxName());
	}

	/** Set Campaign.
		@param C_Campaign_ID Marketing Campaign
	*/
	public void setC_Campaign_ID (int C_Campaign_ID)
	{
		if (C_Campaign_ID < 1)
			set_Value (COLUMNNAME_C_Campaign_ID, null);
		else
			set_Value (COLUMNNAME_C_Campaign_ID, Integer.valueOf(C_Campaign_ID));
	}

	/** Get Campaign.
		@return Marketing Campaign
	  */
	public int getC_Campaign_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Campaign_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** I = I */
	public static final String CHECKINGINDEX_I = "I";
	/** II = II */
	public static final String CHECKINGINDEX_II = "II";
	/** III = III */
	public static final String CHECKINGINDEX_III = "III";
	/** IV = IV */
	public static final String CHECKINGINDEX_IV = "IV";
	/** Set CheckingINdex.
		@param CheckingINdex CheckingINdex
	*/
	public void setCheckingINdex (String CheckingINdex)
	{

		set_Value (COLUMNNAME_CheckingINdex, CheckingINdex);
	}

	/** Get CheckingINdex.
		@return CheckingINdex	  */
	public String getCheckingINdex()
	{
		return (String)get_Value(COLUMNNAME_CheckingINdex);
	}

	public org.compiere.model.I_C_Order getC_Order() throws RuntimeException
	{
		return (org.compiere.model.I_C_Order)MTable.get(getCtx(), org.compiere.model.I_C_Order.Table_ID)
			.getPO(getC_Order_ID(), get_TrxName());
	}

	/** Set Order.
		@param C_Order_ID Order
	*/
	public void setC_Order_ID (int C_Order_ID)
	{
		if (C_Order_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_Order_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_Order_ID, Integer.valueOf(C_Order_ID));
	}

	/** Get Order.
		@return Order
	  */
	public int getC_Order_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Order_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Document Date.
		@param DateDoc Date of the Document
	*/
	public void setDateDoc (Timestamp DateDoc)
	{
		set_Value (COLUMNNAME_DateDoc, DateDoc);
	}

	/** Get Document Date.
		@return Date of the Document
	  */
	public Timestamp getDateDoc()
	{
		return (Timestamp)get_Value(COLUMNNAME_DateDoc);
	}

	/** Set GabahAmpah.
		@param GabahAmpah GabahAmpah
	*/
	public void setGabahAmpah (String GabahAmpah)
	{
		set_Value (COLUMNNAME_GabahAmpah, GabahAmpah);
	}

	/** Get GabahAmpah.
		@return GabahAmpah	  */
	public String getGabahAmpah()
	{
		return (String)get_Value(COLUMNNAME_GabahAmpah);
	}

	/** Set GabahHijau.
		@param GabahHijau GabahHijau
	*/
	public void setGabahHijau (String GabahHijau)
	{
		set_Value (COLUMNNAME_GabahHijau, GabahHijau);
	}

	/** Get GabahHijau.
		@return GabahHijau	  */
	public String getGabahHijau()
	{
		return (String)get_Value(COLUMNNAME_GabahHijau);
	}

	/** Set GabahIsi.
		@param GabahIsi GabahIsi
	*/
	public void setGabahIsi (String GabahIsi)
	{
		set_Value (COLUMNNAME_GabahIsi, GabahIsi);
	}

	/** Get GabahIsi.
		@return GabahIsi	  */
	public String getGabahIsi()
	{
		return (String)get_Value(COLUMNNAME_GabahIsi);
	}

	/** Set isBeras.
		@param isBeras isBeras
	*/
	public void setisBeras (boolean isBeras)
	{
		set_Value (COLUMNNAME_isBeras, Boolean.valueOf(isBeras));
	}

	/** Get isBeras.
		@return isBeras	  */
	public boolean isBeras()
	{
		Object oo = get_Value(COLUMNNAME_isBeras);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set isGabah.
		@param isGabah isGabah
	*/
	public void setisGabah (boolean isGabah)
	{
		set_Value (COLUMNNAME_isGabah, Boolean.valueOf(isGabah));
	}

	/** Get isGabah.
		@return isGabah	  */
	public boolean isGabah()
	{
		Object oo = get_Value(COLUMNNAME_isGabah);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set isProduction.
		@param isProduction isProduction
	*/
	public void setisProduction (boolean isProduction)
	{
		set_Value (COLUMNNAME_isProduction, Boolean.valueOf(isProduction));
	}

	/** Get isProduction.
		@return isProduction	  */
	public boolean isProduction()
	{
		Object oo = get_Value(COLUMNNAME_isProduction);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Receipt.
		@param IsReceipt This is a sales transaction (receipt)
	*/
	public void setIsReceipt (boolean IsReceipt)
	{
		set_Value (COLUMNNAME_IsReceipt, Boolean.valueOf(IsReceipt));
	}

	/** Get Receipt.
		@return This is a sales transaction (receipt)
	  */
	public boolean isReceipt()
	{
		Object oo = get_Value(COLUMNNAME_IsReceipt);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set JenisBeras.
		@param JenisBeras JenisBeras
	*/
	public void setJenisBeras (String JenisBeras)
	{
		set_Value (COLUMNNAME_JenisBeras, JenisBeras);
	}

	/** Get JenisBeras.
		@return JenisBeras	  */
	public String getJenisBeras()
	{
		return (String)get_Value(COLUMNNAME_JenisBeras);
	}

	/** Set JenisGabah.
		@param JenisGabah JenisGabah
	*/
	public void setJenisGabah (String JenisGabah)
	{
		set_Value (COLUMNNAME_JenisGabah, JenisGabah);
	}

	/** Get JenisGabah.
		@return JenisGabah	  */
	public String getJenisGabah()
	{
		return (String)get_Value(COLUMNNAME_JenisGabah);
	}

	/** Set KadarAir.
		@param KadarAir KadarAir
	*/
	public void setKadarAir (String KadarAir)
	{
		set_Value (COLUMNNAME_KadarAir, KadarAir);
	}

	/** Get KadarAir.
		@return KadarAir	  */
	public String getKadarAir()
	{
		return (String)get_Value(COLUMNNAME_KadarAir);
	}

	/** Set Kutu.
		@param Kutu Kutu
	*/
	public void setKutu (String Kutu)
	{
		set_Value (COLUMNNAME_Kutu, Kutu);
	}

	/** Get Kutu.
		@return Kutu	  */
	public String getKutu()
	{
		return (String)get_Value(COLUMNNAME_Kutu);
	}

	/** Set Menir.
		@param Menir Menir
	*/
	public void setMenir (String Menir)
	{
		set_Value (COLUMNNAME_Menir, Menir);
	}

	/** Get Menir.
		@return Menir	  */
	public String getMenir()
	{
		return (String)get_Value(COLUMNNAME_Menir);
	}

	/** Set MillingDegree.
		@param MillingDegree MillingDegree
	*/
	public void setMillingDegree (String MillingDegree)
	{
		set_Value (COLUMNNAME_MillingDegree, MillingDegree);
	}

	/** Get MillingDegree.
		@return MillingDegree	  */
	public String getMillingDegree()
	{
		return (String)get_Value(COLUMNNAME_MillingDegree);
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

	public org.compiere.model.I_M_Production getM_Production() throws RuntimeException
	{
		return (org.compiere.model.I_M_Production)MTable.get(getCtx(), org.compiere.model.I_M_Production.Table_ID)
			.getPO(getM_Production_ID(), get_TrxName());
	}

	/** Set Production.
		@param M_Production_ID Plan for producing a product
	*/
	public void setM_Production_ID (int M_Production_ID)
	{
		if (M_Production_ID < 1)
			set_ValueNoCheck (COLUMNNAME_M_Production_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_M_Production_ID, Integer.valueOf(M_Production_ID));
	}

	/** Get Production.
		@return Plan for producing a product
	  */
	public int getM_Production_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Production_ID);
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

	/** Set rendemen.
		@param rendemen rendemen
	*/
	public void setrendemen (String rendemen)
	{
		set_Value (COLUMNNAME_rendemen, rendemen);
	}

	/** Get rendemen.
		@return rendemen	  */
	public String getrendemen()
	{
		return (String)get_Value(COLUMNNAME_rendemen);
	}

	/** I = I */
	public static final String SHIFT_I = "I";
	/** II = II */
	public static final String SHIFT_II = "II";
	/** III = III */
	public static final String SHIFT_III = "III";
	/** Set Shift.
		@param Shift Shift
	*/
	public void setShift (String Shift)
	{

		set_Value (COLUMNNAME_Shift, Shift);
	}

	/** Get Shift.
		@return Shift	  */
	public String getShift()
	{
		return (String)get_Value(COLUMNNAME_Shift);
	}

	/** Set SusutKA.
		@param SusutKA SusutKA
	*/
	public void setSusutKA (String SusutKA)
	{
		set_Value (COLUMNNAME_SusutKA, SusutKA);
	}

	/** Get SusutKA.
		@return SusutKA	  */
	public String getSusutKA()
	{
		return (String)get_Value(COLUMNNAME_SusutKA);
	}

	/** Set TimeCheck.
		@param TimeCheck TimeCheck
	*/
	public void setTimeCheck (Timestamp TimeCheck)
	{
		set_Value (COLUMNNAME_TimeCheck, TimeCheck);
	}

	/** Get TimeCheck.
		@return TimeCheck	  */
	public Timestamp getTimeCheck()
	{
		return (Timestamp)get_Value(COLUMNNAME_TimeCheck);
	}

	/** Set Tonase.
		@param Tonase Tonase
	*/
	public void setTonase (String Tonase)
	{
		set_Value (COLUMNNAME_Tonase, Tonase);
	}

	/** Get Tonase.
		@return Tonase	  */
	public String getTonase()
	{
		return (String)get_Value(COLUMNNAME_Tonase);
	}

	/** Set Transparancy.
		@param Transparancy Transparancy
	*/
	public void setTransparancy (String Transparancy)
	{
		set_Value (COLUMNNAME_Transparancy, Transparancy);
	}

	/** Get Transparancy.
		@return Transparancy	  */
	public String getTransparancy()
	{
		return (String)get_Value(COLUMNNAME_Transparancy);
	}

	/** Set Whitenes.
		@param Whitenes Whitenes
	*/
	public void setWhitenes (String Whitenes)
	{
		set_Value (COLUMNNAME_Whitenes, Whitenes);
	}

	/** Get Whitenes.
		@return Whitenes	  */
	public String getWhitenes()
	{
		return (String)get_Value(COLUMNNAME_Whitenes);
	}
}