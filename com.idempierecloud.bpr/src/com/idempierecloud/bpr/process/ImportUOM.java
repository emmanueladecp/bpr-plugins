package com.idempierecloud.bpr.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MUOM;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.idempierecloud.bpr.base.CustomProcess;

public class ImportUOM extends CustomProcess{

	int p_AD_Org_ID 	= 0;
	int AD_User_ID 		= 0;
	int run= 0;
	
	@Override
	protected void prepare() {
		
	}

	@Override
	protected String doIt() throws Exception {
		StringBuilder sql = null;
		AD_User_ID = Env.getAD_User_ID(getCtx());
		String clientCheck = getWhereClause();
		int no = 0;
		
		sql = new StringBuilder ("Select Name, X12DE355, UOMSymbol, StdPrecision, CostingPrecision, I_UOM_ID FROM I_UOM where I_IsImported <> 'Y' ").append (clientCheck);
		PreparedStatement pstmnt = null;
		ResultSet rs = null;
		try
		{
			pstmnt = DB.prepareStatement (sql.toString(), get_TrxName());
			rs = pstmnt.executeQuery ();
			while (rs.next ()){
				String name = rs.getString(1);
				String code = rs.getString(2);
				String symbol = rs.getString(3);
				
				if(name==null || code == null || symbol == null) {
					throw new AdempiereException("Name : "+name+", UOMCode : "+code+", UOMSymbol : "+symbol+", tidak boleh null");
				}
				int StdPrecision = rs.getInt(4);
				int CostingPrecision = rs.getInt(5);
				if(StdPrecision<0)
					StdPrecision=0;
				if(CostingPrecision<0)
					CostingPrecision=0;
				
				int c_uom_id = DB.getSQLValue(get_TrxName(), "select c_uom_id from C_UOM where X12DE355 = ?", symbol);
				if(c_uom_id>0) {
					MUOM uom = new MUOM(getCtx(), c_uom_id, get_TrxName());
					throw new AdempiereException("UOM : "+uom.getX12DE355()+" already exists");
				}
				MUOM uom = new MUOM(getCtx(), c_uom_id, get_TrxName());
				uom.setName(name);
				uom.setX12DE355(code);
				uom.setUOMSymbol(symbol);
				if(StdPrecision>0) {
					uom.setStdPrecision(StdPrecision);
				}
				if(StdPrecision>0) {
					uom.setCostingPrecision(CostingPrecision);
				}
				uom.saveEx();
				
				int I_UOM_ID = rs.getInt(6);
				
				sql = new StringBuilder ("update I_UOM set i_isimported='Y' where i_uom_id = "+I_UOM_ID);
				no = DB.executeUpdate(sql.toString(), get_TrxName());
				if (log.isLoggable(Level.INFO)) 
					log.info ("uom imported =" + no);
				
				sql = new StringBuilder ("update I_UOM set C_UOM_ID="+uom.getC_UOM_ID()+" where i_uom_id = "+I_UOM_ID);
				no = DB.executeUpdate(sql.toString(), get_TrxName());
				if (log.isLoggable(Level.INFO)) 
					log.info ("set uom on i_uom=" + no);
				
				commitEx();
			}
		}
		catch (SQLException e){
			log.log(Level.SEVERE, " i_uom - " + sql.toString(), e);
		}
			finally
			{
				DB.close(rs, pstmnt);
				rs = null;
				pstmnt = null;
			}
		return clientCheck;	
		}

		public String getWhereClause() {
			StringBuilder msgreturn = new StringBuilder(" AND CreatedBy = ").append(AD_User_ID);
			return msgreturn.toString();
		}

}
