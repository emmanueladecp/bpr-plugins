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

/** Generated Model for I_RelatedProduct
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="I_RelatedProduct")
public class X_I_RelatedProduct extends PO implements I_I_RelatedProduct, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20221024L;

    /** Standard Constructor */
    public X_I_RelatedProduct (Properties ctx, int I_RelatedProduct_ID, String trxName)
    {
      super (ctx, I_RelatedProduct_ID, trxName);
      /** if (I_RelatedProduct_ID == 0)
        {
			setI_RelatedProduct_ID (0);
			setName (null);
			setRelatedProductType (null);
        } */
    }

    /** Standard Constructor */
    public X_I_RelatedProduct (Properties ctx, int I_RelatedProduct_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, I_RelatedProduct_ID, trxName, virtualColumns);
      /** if (I_RelatedProduct_ID == 0)
        {
			setI_RelatedProduct_ID (0);
			setName (null);
			setRelatedProductType (null);
        } */
    }

    /** Load Constructor */
    public X_I_RelatedProduct (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_I_RelatedProduct[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
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

	/** Set Import Error Message.
		@param I_ErrorMsg Messages generated from import process
	*/
	public void setI_ErrorMsg (String I_ErrorMsg)
	{
		set_Value (COLUMNNAME_I_ErrorMsg, I_ErrorMsg);
	}

	/** Get Import Error Message.
		@return Messages generated from import process
	  */
	public String getI_ErrorMsg()
	{
		return (String)get_Value(COLUMNNAME_I_ErrorMsg);
	}

	/** Set Imported.
		@param I_IsImported Has this import been processed
	*/
	public void setI_IsImported (boolean I_IsImported)
	{
		set_Value (COLUMNNAME_I_IsImported, Boolean.valueOf(I_IsImported));
	}

	/** Get Imported.
		@return Has this import been processed
	  */
	public boolean isI_IsImported()
	{
		Object oo = get_Value(COLUMNNAME_I_IsImported);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Import Related Product.
		@param I_RelatedProduct_ID Import Related Product
	*/
	public void setI_RelatedProduct_ID (int I_RelatedProduct_ID)
	{
		if (I_RelatedProduct_ID < 1)
			set_ValueNoCheck (COLUMNNAME_I_RelatedProduct_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_I_RelatedProduct_ID, Integer.valueOf(I_RelatedProduct_ID));
	}

	/** Get Import Related Product.
		@return Import Related Product	  */
	public int getI_RelatedProduct_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_I_RelatedProduct_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set I_RelatedProduct_UU.
		@param I_RelatedProduct_UU I_RelatedProduct_UU
	*/
	public void setI_RelatedProduct_UU (String I_RelatedProduct_UU)
	{
		set_Value (COLUMNNAME_I_RelatedProduct_UU, I_RelatedProduct_UU);
	}

	/** Get I_RelatedProduct_UU.
		@return I_RelatedProduct_UU	  */
	public String getI_RelatedProduct_UU()
	{
		return (String)get_Value(COLUMNNAME_I_RelatedProduct_UU);
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

	/** Set M_RelatedProduct_UU.
		@param M_RelatedProduct_UU M_RelatedProduct_UU
	*/
	public void setM_RelatedProduct_UU (String M_RelatedProduct_UU)
	{
		set_Value (COLUMNNAME_M_RelatedProduct_UU, M_RelatedProduct_UU);
	}

	/** Get M_RelatedProduct_UU.
		@return M_RelatedProduct_UU	  */
	public String getM_RelatedProduct_UU()
	{
		return (String)get_Value(COLUMNNAME_M_RelatedProduct_UU);
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

	/** Set Process Now.
		@param Processing Process Now
	*/
	public void setProcessing (boolean Processing)
	{
		set_Value (COLUMNNAME_Processing, Boolean.valueOf(Processing));
	}

	/** Get Process Now.
		@return Process Now	  */
	public boolean isProcessing()
	{
		Object oo = get_Value(COLUMNNAME_Processing);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Product Key.
		@param ProductValue Key of the Product
	*/
	public void setProductValue (String ProductValue)
	{
		set_ValueNoCheck (COLUMNNAME_ProductValue, ProductValue);
	}

	/** Get Product Key.
		@return Key of the Product
	  */
	public String getProductValue()
	{
		return (String)get_Value(COLUMNNAME_ProductValue);
	}

	/** RelatedProductType AD_Reference_ID=313 */
	public static final int RELATEDPRODUCTTYPE_AD_Reference_ID=313;
	/** Alternative = A */
	public static final String RELATEDPRODUCTTYPE_Alternative = "A";
	/** Web Promotion = P */
	public static final String RELATEDPRODUCTTYPE_WebPromotion = "P";
	/** Supplemental = S */
	public static final String RELATEDPRODUCTTYPE_Supplemental = "S";
	/** Set Related Product Type.
		@param RelatedProductType Related Product Type
	*/
	public void setRelatedProductType (String RelatedProductType)
	{

		set_ValueNoCheck (COLUMNNAME_RelatedProductType, RelatedProductType);
	}

	/** Get Related Product Type.
		@return Related Product Type	  */
	public String getRelatedProductType()
	{
		return (String)get_Value(COLUMNNAME_RelatedProductType);
	}

	public org.compiere.model.I_M_Product getRelatedProduct() throws RuntimeException
	{
		return (org.compiere.model.I_M_Product)MTable.get(getCtx(), org.compiere.model.I_M_Product.Table_ID)
			.getPO(getRelatedProduct_ID(), get_TrxName());
	}

	/** Set Related Product.
		@param RelatedProduct_ID Related Product
	*/
	public void setRelatedProduct_ID (int RelatedProduct_ID)
	{
		if (RelatedProduct_ID < 1)
			set_ValueNoCheck (COLUMNNAME_RelatedProduct_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_RelatedProduct_ID, Integer.valueOf(RelatedProduct_ID));
	}

	/** Get Related Product.
		@return Related Product
	  */
	public int getRelatedProduct_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_RelatedProduct_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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