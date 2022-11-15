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

/** Generated Interface for BPR_Timbangan
 *  @author iDempiere (generated) 
 *  @version Release 9
 */
@SuppressWarnings("all")
public interface I_BPR_Timbangan 
{

    /** TableName=BPR_Timbangan */
    public static final String Table_Name = "BPR_Timbangan";

    /** AD_Table_ID=1000011 */
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

    /** Column name BPR_NoKendaraan */
    public static final String COLUMNNAME_BPR_NoKendaraan = "BPR_NoKendaraan";

	/** Set Nomor Kendaraan	  */
	public void setBPR_NoKendaraan (String BPR_NoKendaraan);

	/** Get Nomor Kendaraan	  */
	public String getBPR_NoKendaraan();

    /** Column name BPR_Timbangan_ID */
    public static final String COLUMNNAME_BPR_Timbangan_ID = "BPR_Timbangan_ID";

	/** Set BPR_Timbangan	  */
	public void setBPR_Timbangan_ID (int BPR_Timbangan_ID);

	/** Get BPR_Timbangan	  */
	public int getBPR_Timbangan_ID();

    /** Column name BPR_Timbangan_UU */
    public static final String COLUMNNAME_BPR_Timbangan_UU = "BPR_Timbangan_UU";

	/** Set BPR_Timbangan_UU	  */
	public void setBPR_Timbangan_UU (String BPR_Timbangan_UU);

	/** Get BPR_Timbangan_UU	  */
	public String getBPR_Timbangan_UU();

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

    /** Column name M_Warehouse_ID */
    public static final String COLUMNNAME_M_Warehouse_ID = "M_Warehouse_ID";

	/** Set Warehouse.
	  * Storage Warehouse and Service Point
	  */
	public void setM_Warehouse_ID (int M_Warehouse_ID);

	/** Get Warehouse.
	  * Storage Warehouse and Service Point
	  */
	public int getM_Warehouse_ID();

	public org.compiere.model.I_M_Warehouse getM_Warehouse() throws RuntimeException;

    /** Column name TimbanganNetAmt */
    public static final String COLUMNNAME_TimbanganNetAmt = "TimbanganNetAmt";

	/** Set Timbangan Net Amt	  */
	public void setTimbanganNetAmt (BigDecimal TimbanganNetAmt);

	/** Get Timbangan Net Amt	  */
	public BigDecimal getTimbanganNetAmt();

    /** Column name TimbangIsi */
    public static final String COLUMNNAME_TimbangIsi = "TimbangIsi";

	/** Set Timbang Isi	  */
	public void setTimbangIsi (BigDecimal TimbangIsi);

	/** Get Timbang Isi	  */
	public BigDecimal getTimbangIsi();

    /** Column name TimbangKosong */
    public static final String COLUMNNAME_TimbangKosong = "TimbangKosong";

	/** Set Timbang Kosong	  */
	public void setTimbangKosong (BigDecimal TimbangKosong);

	/** Get Timbang Kosong	  */
	public BigDecimal getTimbangKosong();

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

    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";

	/** Set Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value);

	/** Get Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public String getValue();
}
