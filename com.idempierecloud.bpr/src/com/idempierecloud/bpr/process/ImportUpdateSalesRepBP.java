package com.idempierecloud.bpr.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.DBException;
import org.compiere.model.MBPartner;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.model.X_BPR_Historysales;
import com.idempierecloud.bpr.model.X_I_BPR_Updatesales;

public class ImportUpdateSalesRepBP extends CustomProcess {

	private int AD_User_ID = 0;
	private int m_AD_Client_ID = 0;
	private int SalesRep_ID = 0;
	private int C_BPartner_ID = 0;
	private int SalesRepOld_ID = 0;
	private String Value = "";
	

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			C_BPartner_ID = getRecord_ID();
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = para[i].getParameterAsInt();
			else if (name.equals("Value"))
				Value = para[i].getParameterAsString();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);	
		}

	}

	@Override
	protected String doIt() throws Exception {
		AD_User_ID = Env.getAD_User_ID(getCtx());
		StringBuilder sql = null;
		int no = 0;
		String clientCheck = getWhereClause();
		
		/*JIKA IMPORT dari Toolbar Button Window BP*/
		if(C_BPartner_ID>0) {
			if(Value.equals(null))
				throw new AdempiereException("Search Key SalesRep tidak boleh Kosong");
			SalesRep_ID = DB.getSQLValue(get_TrxName(),"select coalesce(ad_user_id,0) from ad_user au where au.value like ?", Value);
			if(SalesRep_ID==0 || SalesRep_ID<0) {
				throw new AdempiereException("Tidak ditemukan User dengan Value ="+Value);
			}else {
				MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, get_TrxName());
				SalesRepOld_ID = bp.getSalesRep_ID();
				bp.setSalesRep_ID(SalesRep_ID);
				bp.saveEx();
				
				if(bp.getSalesRep_ID()==SalesRep_ID) {
					X_BPR_Historysales hs = new X_BPR_Historysales(getCtx(), 0, get_TrxName());
					hs.setSalesRep_ID(SalesRepOld_ID);
					hs.setSalesRepNew_ID(SalesRep_ID);
					hs.setC_BPartner_ID(C_BPartner_ID);
					hs.saveEx();
				}
				
			}
		/*JIKA import menggunakan file loader*/		
		}else {
			//	****	Prepare	****
			sql = new StringBuilder ("UPDATE I_BPR_Updatesales ")
					.append("SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid BP Value, ' ")
					.append("WHERE value IS NULL")
					.append(" AND I_IsImported<>'Y'").append(clientCheck);
			no = DB.executeUpdateEx(sql.toString(), get_TrxName());
			if (log.isLoggable(Level.CONFIG)) log.config("Invalid Business Partner Value=" + no);
			
			sql = new StringBuilder ("UPDATE I_BPR_Updatesales ")
					.append("SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid SalesRep Value, ' ")
					.append("WHERE value2 IS NULL")
					.append(" AND I_IsImported<>'Y'").append(clientCheck);
			no = DB.executeUpdateEx(sql.toString(), get_TrxName());
			if (log.isLoggable(Level.CONFIG)) log.config("Invalid SalesRep Value=" + no);
			commitEx();
			
			sql = new StringBuilder ("SELECT * FROM I_BPR_Updatesales ")
					.append("WHERE I_IsImported='N'").append(clientCheck);
			sql.append(" ORDER BY Value, I_BPR_Updatesales_ID");
			PreparedStatement pstmt =  null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
				rs = pstmt.executeQuery();
				
				while (rs.next())
				{
					X_I_BPR_Updatesales us = new X_I_BPR_Updatesales(getCtx(), rs, get_TrxName());
					SalesRep_ID = DB.getSQLValue(get_TrxName(),"select coalesce(ad_user_id,0) from ad_user au where au.value like ?", us.getValue2());
					if(SalesRep_ID==0 || SalesRep_ID<0) {
						us.setI_ErrorMsg("Tidak ditemukan User dengan Value = "+us.getValue()+" || "+us.getI_ErrorMsg());
						us.setI_IsImported(false);
						us.saveEx();
						continue;
					}
					C_BPartner_ID = DB.getSQLValue(get_TrxName(),"select coalesce(c_bpartner_id,0) from c_bpartner cb where cb.value like ?", us.getValue());
					if(C_BPartner_ID==0 || C_BPartner_ID<0) {
						us.setI_ErrorMsg("Tidak ditemukan BP dengan Value = "+us.getValue()+" || "+us.getI_ErrorMsg());
						us.setI_IsImported(false);
						us.saveEx();
						continue;
					}
					MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, get_TrxName());
					SalesRepOld_ID = bp.getSalesRep_ID();
					bp.setSalesRep_ID(SalesRep_ID);
					bp.saveEx();
					
					if(bp.getSalesRep_ID()==SalesRep_ID) {
						us.setC_BPartner_ID(C_BPartner_ID);
						us.setAD_User_ID(SalesRep_ID);
						us.setI_IsImported(true);
						us.setProcessed(true);
						us.saveEx();
						
						X_BPR_Historysales hs = new X_BPR_Historysales(getCtx(), 0, get_TrxName());
						hs.setSalesRep_ID(SalesRepOld_ID);
						hs.setSalesRepNew_ID(SalesRep_ID);
						hs.setC_BPartner_ID(C_BPartner_ID);
						hs.saveEx();
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
				sql = new StringBuilder ("UPDATE I_BPR_Updatesales ")
						.append("SET I_IsImported='N', Updated=getDate() ")
						.append("WHERE I_IsImported<>'Y'").append(clientCheck);
				no = DB.executeUpdateEx(sql.toString(), get_TrxName());
				
				addLog (0, null, new BigDecimal (no), "@Errors@");
			}
		}
		return "";
	}
	
	
	
	public String getWhereClause() {
		StringBuilder msgreturn = new StringBuilder(" AND AD_Client_ID=").append(m_AD_Client_ID)
				.append(" AND CreatedBy = ").append(AD_User_ID);
		return msgreturn.toString();
	}


}
