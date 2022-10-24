package com.idempierecloud.bpr.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.exceptions.DBException;
import org.adempiere.model.ImportValidator;
import org.adempiere.process.ImportProcess;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.Query;
import org.compiere.model.X_M_RelatedProduct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.model.X_I_RelatedProduct;

public class ImportRelatedProduct extends CustomProcess implements ImportProcess {

	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 0;
	/**	Delete old Imported				*/
	private boolean			m_deleteOldImported = false;
	/**	Only validate, don't import		*/
	private boolean			p_IsValidateOnly = false;
	
	@Override
	public String getImportTableName() {
		return X_I_RelatedProduct.Table_Name;
	}

	@Override
	public String getWhereClause() {
		StringBuilder msgreturn = new StringBuilder(" AND AD_Client_ID=").append(m_AD_Client_ID);
		return msgreturn.toString();
	}

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = para[i].getParameterAsInt();
			else if (name.equals("DeleteOldImported"))
				m_deleteOldImported = "Y".equals(para[i].getParameter());
			else if (name.equals("IsValidateOnly"))
				p_IsValidateOnly = para[i].getParameterAsBoolean();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {
		StringBuilder sql = null;
		int no = 0;
		String clientCheck = getWhereClause();

		//	****	Prepare	****

		//	Delete Old Imported
		if (m_deleteOldImported)
		{
			sql = new StringBuilder ("DELETE FROM I_RelatedProduct ")
					.append("WHERE I_IsImported='Y'").append(clientCheck);
			no = DB.executeUpdateEx(sql.toString(), get_TrxName());
			if (log.isLoggable(Level.FINE)) log.fine("Delete Old Impored =" + no);
		}

		//	Set Client, Org, IsActive, Created/Updated
		sql = new StringBuilder ("UPDATE I_RelatedProduct ")
				.append("SET AD_Client_ID = COALESCE (AD_Client_ID, ").append(m_AD_Client_ID).append("),")
						.append(" AD_Org_ID = COALESCE (AD_Org_ID, 0),")
						.append(" IsActive = COALESCE (IsActive, 'Y'),")
						.append(" Created = COALESCE (Created, getDate()),")
						.append(" CreatedBy = COALESCE (CreatedBy, 0),")
						.append(" Updated = COALESCE (Updated, getDate()),")
						.append(" UpdatedBy = COALESCE (UpdatedBy, 0),")
						.append(" I_ErrorMsg = ' ',")
						.append(" I_IsImported = 'N' ")
						.append("WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdateEx(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) log.fine("Reset=" + no);
		
		ModelValidationEngine.get().fireImportValidate(this, null, null, ImportValidator.TIMING_BEFORE_VALIDATE);
		
		// Set Product
		sql = new StringBuilder ("UPDATE I_RelatedProduct i ")
				.append("SET M_Product_ID=(SELECT M_Product_ID FROM M_Product g")
				.append(" WHERE i.productvalue=g.value AND g.AD_Client_ID=i.AD_Client_ID) ")
				.append("WHERE M_Product_ID IS NULL")
				.append(" AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdateEx(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) log.fine("Set Product=" + no);
		//
		sql = new StringBuilder ("UPDATE I_RelatedProduct ")
				.append("SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid Product Value, ' ")
				.append("WHERE M_Product_ID IS NULL")
				.append(" AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdateEx(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.CONFIG)) log.config("Invalid Product=" + no);
		
		// Set Related Product
		sql = new StringBuilder ("UPDATE I_RelatedProduct i ")
				.append("SET RelatedProduct_ID=(SELECT M_Product_ID FROM M_Product g")
				.append(" WHERE i.value=g.value AND g.AD_Client_ID=i.AD_Client_ID) ")
				.append("WHERE RelatedProduct_ID IS NULL")
				.append(" AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdateEx(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) log.fine("Set Product=" + no);
		//
		sql = new StringBuilder ("UPDATE I_RelatedProduct ")
				.append("SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid Related Product Value, ' ")
				.append("WHERE RelatedProduct_ID IS NULL")
				.append(" AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdateEx(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.CONFIG)) log.config("Invalid Related Product=" + no);
		
		// Validate RelatedProductType
		sql = new StringBuilder ("UPDATE I_RelatedProduct ")
				.append("SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid Related Product Type, ' ")
				.append("WHERE NOT EXISTS(SELECT 1 FROM AD_Ref_List WHERE I_RelatedProduct.RelatedProductType=AD_Ref_List.value AND AD_Reference_ID=313)")
				.append(" AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdateEx(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.CONFIG)) log.config("Invalid Related Product Type=" + no);
		
		// Set Related Product
		sql = new StringBuilder ("UPDATE I_RelatedProduct i ")
				.append("SET M_RelatedProduct_UU=(SELECT M_RelatedProduct_UU FROM M_RelatedProduct g")
				.append(" WHERE i.M_Product_ID=g.M_Product_ID AND i.RelatedProduct_ID=g.RelatedProduct_ID AND g.AD_Client_ID=i.AD_Client_ID) ")
				.append("WHERE M_Product_ID IS NOT NULL AND RelatedProduct_ID IS NOT NULL")
				.append(" AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdateEx(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) log.fine("Set Existing Related Product=" + no);
		//
		
		ModelValidationEngine.get().fireImportValidate(this, null, null, ImportValidator.TIMING_AFTER_VALIDATE);

		commitEx();
		if (p_IsValidateOnly)
		{
			return "Validated";
		}
		
		int noInsert = 0;
		int noUpdate = 0;

		//	Go through Records
		sql = new StringBuilder ("SELECT * FROM I_RelatedProduct ")
				.append("WHERE I_IsImported='N'").append(clientCheck);
		sql.append(" ORDER BY Value, I_RelatedProduct_ID");
		PreparedStatement pstmt =  null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			rs = pstmt.executeQuery();
			
			while (rs.next())
			{
				X_I_RelatedProduct impRelatedProduct = new X_I_RelatedProduct (getCtx(), rs, get_TrxName());
				StringBuilder msglog = new StringBuilder("I_RelatedProduct)_ID=") .append(impRelatedProduct.getI_RelatedProduct_ID())
						.append(", M_Product_ID=").append(impRelatedProduct.getM_Product_ID())
						.append(", RelatedProduct)_ID=").append(impRelatedProduct.getRelatedProduct_ID());
				if (log.isLoggable(Level.FINE)) log.fine(msglog.toString());
				
				if(impRelatedProduct.getM_RelatedProduct_UU()==null) {
					X_M_RelatedProduct relatedProduct = new X_M_RelatedProduct(getCtx(), 0, get_TrxName());
					
					relatedProduct.setM_Product_ID(impRelatedProduct.getM_Product_ID());
					relatedProduct.setRelatedProduct_ID(impRelatedProduct.getRelatedProduct_ID());
					relatedProduct.setName(impRelatedProduct.getName());
					relatedProduct.setDescription(impRelatedProduct.getDescription());
					relatedProduct.setRelatedProductType(impRelatedProduct.getRelatedProductType());
					relatedProduct.saveEx();
					
					impRelatedProduct.setM_RelatedProduct_UU(relatedProduct.getM_RelatedProduct_UU());
					impRelatedProduct.saveEx();
					noInsert++;
				}else {
					X_M_RelatedProduct relatedProduct = new Query(getCtx(), X_I_RelatedProduct.Table_Name, "M_RelatedProduct_UU=? "+clientCheck, get_TrxName())
						.setParameters(new Object[] {impRelatedProduct.getM_RelatedProduct_UU(), m_AD_Client_ID})
						.firstOnly();
					
					relatedProduct.setName(impRelatedProduct.getName());
					relatedProduct.setDescription(impRelatedProduct.getDescription());
					relatedProduct.setRelatedProductType(impRelatedProduct.getRelatedProductType());
					noUpdate++;
				}
			}
			
		}
		catch (SQLException e)
		{
			rollback();
			throw new DBException(e, sql.toString());
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
			//	Set Error to indicator to not imported
			sql = new StringBuilder ("UPDATE I_RelatedProduct ")
					.append("SET I_IsImported='N', Updated=getDate() ")
					.append("WHERE I_IsImported<>'Y'").append(clientCheck);
			no = DB.executeUpdateEx(sql.toString(), get_TrxName());
			addLog (0, null, new BigDecimal (no), "@Errors@");
			addLog (0, null, new BigDecimal (noInsert), "@M_RelatedProduct_ID@: @Inserted@");
			addLog (0, null, new BigDecimal (noUpdate), "@M_RelatedProduct_ID@: @Updated@");
		}
		return "";
	}

}
