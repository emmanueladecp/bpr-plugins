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

/** Generated Interface for BPR_HistoryFakturPajak
 *  @author iDempiere (generated) 
 *  @version Release 9
 */
@SuppressWarnings("all")
public interface I_BPR_HistoryFakturPajak 
{

    /** TableName=BPR_HistoryFakturPajak */
    public static final String Table_Name = "BPR_HistoryFakturPajak";

    /** AD_Table_ID=1000043 */
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

    /** Column name BPR_HistoryFakturPajak_ID */
    public static final String COLUMNNAME_BPR_HistoryFakturPajak_ID = "BPR_HistoryFakturPajak_ID";

	/** Set History Faktur Pajak	  */
	public void setBPR_HistoryFakturPajak_ID (int BPR_HistoryFakturPajak_ID);

	/** Get History Faktur Pajak	  */
	public int getBPR_HistoryFakturPajak_ID();

    /** Column name BPR_HistoryFakturPajak_UU */
    public static final String COLUMNNAME_BPR_HistoryFakturPajak_UU = "BPR_HistoryFakturPajak_UU";

	/** Set BPR_HistoryFakturPajak_UU	  */
	public void setBPR_HistoryFakturPajak_UU (String BPR_HistoryFakturPajak_UU);

	/** Get BPR_HistoryFakturPajak_UU	  */
	public String getBPR_HistoryFakturPajak_UU();

    /** Column name BPR_ListFakturPajak_ID */
    public static final String COLUMNNAME_BPR_ListFakturPajak_ID = "BPR_ListFakturPajak_ID";

	/** Set BPR_ListFakturPajak	  */
	public void setBPR_ListFakturPajak_ID (int BPR_ListFakturPajak_ID);

	/** Get BPR_ListFakturPajak	  */
	public int getBPR_ListFakturPajak_ID();

	public I_BPR_ListFakturPajak getBPR_ListFakturPajak() throws RuntimeException;

    /** Column name C_Invoice_ID */
    public static final String COLUMNNAME_C_Invoice_ID = "C_Invoice_ID";

	/** Set Invoice.
	  * Invoice Identifier
	  */
	public void setC_Invoice_ID (int C_Invoice_ID);

	/** Get Invoice.
	  * Invoice Identifier
	  */
	public int getC_Invoice_ID();

	public org.compiere.model.I_C_Invoice getC_Invoice() throws RuntimeException;

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

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();

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

    /** Column name IsUploaded */
    public static final String COLUMNNAME_IsUploaded = "IsUploaded";

	/** Set Is Uploaded	  */
	public void setIsUploaded (boolean IsUploaded);

	/** Get Is Uploaded	  */
	public boolean isUploaded();

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
}
