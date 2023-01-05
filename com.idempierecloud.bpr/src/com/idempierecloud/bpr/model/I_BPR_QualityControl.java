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
package com.idempierecloud.bpr.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for BPR_QualityControl
 *  @author iDempiere (generated) 
 *  @version Release 9
 */
@SuppressWarnings("all")
public interface I_BPR_QualityControl 
{

    /** TableName=BPR_QualityControl */
    public static final String Table_Name = "BPR_QualityControl";

    /** AD_Table_ID=1000027 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name AsalGabah */
    public static final String COLUMNNAME_AsalGabah = "AsalGabah";

	/** Set AsalGabah	  */
	public void setAsalGabah (String AsalGabah);

	/** Get AsalGabah	  */
	public String getAsalGabah();

    /** Column name BerasKepala */
    public static final String COLUMNNAME_BerasKepala = "BerasKepala";

	/** Set BerasKepala	  */
	public void setBerasKepala (String BerasKepala);

	/** Get BerasKepala	  */
	public String getBerasKepala();

    /** Column name BerasPatahan */
    public static final String COLUMNNAME_BerasPatahan = "BerasPatahan";

	/** Set BerasPatahan	  */
	public void setBerasPatahan (String BerasPatahan);

	/** Get BerasPatahan	  */
	public String getBerasPatahan();

    /** Column name BerasSinar */
    public static final String COLUMNNAME_BerasSinar = "BerasSinar";

	/** Set BerasSinar	  */
	public void setBerasSinar (String BerasSinar);

	/** Get BerasSinar	  */
	public String getBerasSinar();

    /** Column name BPR_Mesin */
    public static final String COLUMNNAME_BPR_Mesin = "BPR_Mesin";

	/** Set BPR_Mesin	  */
	public void setBPR_Mesin (String BPR_Mesin);

	/** Get BPR_Mesin	  */
	public String getBPR_Mesin();

    /** Column name BPR_NoKendaraan */
    public static final String COLUMNNAME_BPR_NoKendaraan = "BPR_NoKendaraan";

	/** Set Nomor Kendaraan	  */
	public void setBPR_NoKendaraan (String BPR_NoKendaraan);

	/** Get Nomor Kendaraan	  */
	public String getBPR_NoKendaraan();

    /** Column name BPR_QualityControl_ID */
    public static final String COLUMNNAME_BPR_QualityControl_ID = "BPR_QualityControl_ID";

	/** Set BPR_QualityControl_ID	  */
	public void setBPR_QualityControl_ID (int BPR_QualityControl_ID);

	/** Get BPR_QualityControl_ID	  */
	public int getBPR_QualityControl_ID();

    /** Column name BPR_QualityControl_UU */
    public static final String COLUMNNAME_BPR_QualityControl_UU = "BPR_QualityControl_UU";

	/** Set BPR_QualityControl_UU	  */
	public void setBPR_QualityControl_UU (String BPR_QualityControl_UU);

	/** Get BPR_QualityControl_UU	  */
	public String getBPR_QualityControl_UU();

    /** Column name BPR_Timbangan_ID */
    public static final String COLUMNNAME_BPR_Timbangan_ID = "BPR_Timbangan_ID";

	/** Set BPR_Timbangan	  */
	public void setBPR_Timbangan_ID (int BPR_Timbangan_ID);

	/** Get BPR_Timbangan	  */
	public int getBPR_Timbangan_ID();

	public I_BPR_Timbangan getBPR_Timbangan() throws RuntimeException;

    /** Column name Broken */
    public static final String COLUMNNAME_Broken = "Broken";

	/** Set Broken	  */
	public void setBroken (String Broken);

	/** Get Broken	  */
	public String getBroken();

    /** Column name C_Activity_ID */
    public static final String COLUMNNAME_C_Activity_ID = "C_Activity_ID";

	/** Set Department.
	  * Business Activity
	  */
	public void setC_Activity_ID (int C_Activity_ID);

	/** Get Department.
	  * Business Activity
	  */
	public int getC_Activity_ID();

	public org.compiere.model.I_C_Activity getC_Activity() throws RuntimeException;

    /** Column name C_BPartner_ID */
    public static final String COLUMNNAME_C_BPartner_ID = "C_BPartner_ID";

	/** Set Business Partner.
	  * Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID);

	/** Get Business Partner.
	  * Identifies a Business Partner
	  */
	public int getC_BPartner_ID();

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException;

    /** Column name C_Campaign_ID */
    public static final String COLUMNNAME_C_Campaign_ID = "C_Campaign_ID";

	/** Set Campaign.
	  * Marketing Campaign
	  */
	public void setC_Campaign_ID (int C_Campaign_ID);

	/** Get Campaign.
	  * Marketing Campaign
	  */
	public int getC_Campaign_ID();

	public org.compiere.model.I_C_Campaign getC_Campaign() throws RuntimeException;

    /** Column name CheckingINdex */
    public static final String COLUMNNAME_CheckingINdex = "CheckingINdex";

	/** Set CheckingINdex	  */
	public void setCheckingINdex (String CheckingINdex);

	/** Get CheckingINdex	  */
	public String getCheckingINdex();

    /** Column name C_Order_ID */
    public static final String COLUMNNAME_C_Order_ID = "C_Order_ID";

	/** Set Order.
	  * Order
	  */
	public void setC_Order_ID (int C_Order_ID);

	/** Get Order.
	  * Order
	  */
	public int getC_Order_ID();

	public org.compiere.model.I_C_Order getC_Order() throws RuntimeException;

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name DateDoc */
    public static final String COLUMNNAME_DateDoc = "DateDoc";

	/** Set Document Date.
	  * Date of the Document
	  */
	public void setDateDoc (Timestamp DateDoc);

	/** Get Document Date.
	  * Date of the Document
	  */
	public Timestamp getDateDoc();

    /** Column name GabahAmpah */
    public static final String COLUMNNAME_GabahAmpah = "GabahAmpah";

	/** Set GabahAmpah	  */
	public void setGabahAmpah (String GabahAmpah);

	/** Get GabahAmpah	  */
	public String getGabahAmpah();

    /** Column name GabahHijau */
    public static final String COLUMNNAME_GabahHijau = "GabahHijau";

	/** Set GabahHijau	  */
	public void setGabahHijau (String GabahHijau);

	/** Get GabahHijau	  */
	public String getGabahHijau();

    /** Column name GabahIsi */
    public static final String COLUMNNAME_GabahIsi = "GabahIsi";

	/** Set GabahIsi	  */
	public void setGabahIsi (String GabahIsi);

	/** Get GabahIsi	  */
	public String getGabahIsi();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name isBeras */
    public static final String COLUMNNAME_isBeras = "isBeras";

	/** Set isBeras	  */
	public void setisBeras (boolean isBeras);

	/** Get isBeras	  */
	public boolean isBeras();

    /** Column name isGabah */
    public static final String COLUMNNAME_isGabah = "isGabah";

	/** Set isGabah	  */
	public void setisGabah (boolean isGabah);

	/** Get isGabah	  */
	public boolean isGabah();

    /** Column name isProduction */
    public static final String COLUMNNAME_isProduction = "isProduction";

	/** Set isProduction	  */
	public void setisProduction (boolean isProduction);

	/** Get isProduction	  */
	public boolean isProduction();

    /** Column name IsReceipt */
    public static final String COLUMNNAME_IsReceipt = "IsReceipt";

	/** Set Receipt.
	  * This is a sales transaction (receipt)
	  */
	public void setIsReceipt (boolean IsReceipt);

	/** Get Receipt.
	  * This is a sales transaction (receipt)
	  */
	public boolean isReceipt();

    /** Column name JenisBeras */
    public static final String COLUMNNAME_JenisBeras = "JenisBeras";

	/** Set JenisBeras	  */
	public void setJenisBeras (String JenisBeras);

	/** Get JenisBeras	  */
	public String getJenisBeras();

    /** Column name JenisGabah */
    public static final String COLUMNNAME_JenisGabah = "JenisGabah";

	/** Set JenisGabah	  */
	public void setJenisGabah (String JenisGabah);

	/** Get JenisGabah	  */
	public String getJenisGabah();

    /** Column name KadarAir */
    public static final String COLUMNNAME_KadarAir = "KadarAir";

	/** Set KadarAir	  */
	public void setKadarAir (String KadarAir);

	/** Get KadarAir	  */
	public String getKadarAir();

    /** Column name Kutu */
    public static final String COLUMNNAME_Kutu = "Kutu";

	/** Set Kutu	  */
	public void setKutu (String Kutu);

	/** Get Kutu	  */
	public String getKutu();

    /** Column name Menir */
    public static final String COLUMNNAME_Menir = "Menir";

	/** Set Menir	  */
	public void setMenir (String Menir);

	/** Get Menir	  */
	public String getMenir();

    /** Column name MillingDegree */
    public static final String COLUMNNAME_MillingDegree = "MillingDegree";

	/** Set MillingDegree	  */
	public void setMillingDegree (String MillingDegree);

	/** Get MillingDegree	  */
	public String getMillingDegree();

    /** Column name M_Product_ID */
    public static final String COLUMNNAME_M_Product_ID = "M_Product_ID";

	/** Set Product.
	  * Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID);

	/** Get Product.
	  * Product, Service, Item
	  */
	public int getM_Product_ID();

	public org.compiere.model.I_M_Product getM_Product() throws RuntimeException;

    /** Column name M_Production_ID */
    public static final String COLUMNNAME_M_Production_ID = "M_Production_ID";

	/** Set Production.
	  * Plan for producing a product
	  */
	public void setM_Production_ID (int M_Production_ID);

	/** Get Production.
	  * Plan for producing a product
	  */
	public int getM_Production_ID();

	public org.compiere.model.I_M_Production getM_Production() throws RuntimeException;

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Processed.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Processed.
	  * The document has been processed
	  */
	public boolean isProcessed();

    /** Column name rendemen */
    public static final String COLUMNNAME_rendemen = "rendemen";

	/** Set rendemen	  */
	public void setrendemen (String rendemen);

	/** Get rendemen	  */
	public String getrendemen();

    /** Column name Shift */
    public static final String COLUMNNAME_Shift = "Shift";

	/** Set Shift	  */
	public void setShift (String Shift);

	/** Get Shift	  */
	public String getShift();

    /** Column name SusutKA */
    public static final String COLUMNNAME_SusutKA = "SusutKA";

	/** Set SusutKA	  */
	public void setSusutKA (String SusutKA);

	/** Get SusutKA	  */
	public String getSusutKA();

    /** Column name TimeCheck */
    public static final String COLUMNNAME_TimeCheck = "TimeCheck";

	/** Set TimeCheck	  */
	public void setTimeCheck (Timestamp TimeCheck);

	/** Get TimeCheck	  */
	public Timestamp getTimeCheck();

    /** Column name Tonase */
    public static final String COLUMNNAME_Tonase = "Tonase";

	/** Set Tonase	  */
	public void setTonase (String Tonase);

	/** Get Tonase	  */
	public String getTonase();

    /** Column name Transparancy */
    public static final String COLUMNNAME_Transparancy = "Transparancy";

	/** Set Transparancy	  */
	public void setTransparancy (String Transparancy);

	/** Get Transparancy	  */
	public String getTransparancy();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();

    /** Column name Whitenes */
    public static final String COLUMNNAME_Whitenes = "Whitenes";

	/** Set Whitenes	  */
	public void setWhitenes (String Whitenes);

	/** Get Whitenes	  */
	public String getWhitenes();
}
