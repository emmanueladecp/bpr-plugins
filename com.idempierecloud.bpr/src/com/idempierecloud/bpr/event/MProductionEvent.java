package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MProductionExt;
import com.idempierecloud.bpr.model.MProductionLineExt;

public class MProductionEvent extends CustomEvent{

	private static CLogger log = CLogger.getCLogger(MProductionEvent.class);
	private final static int C_Doctype_ID_BPR_RiceToRice = 1000066;
	
	private MProductionExt production = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Production Event : "+event.getTopic());
		production = (MProductionExt) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			//if Window Production Plan (Rice to Rice)
			if(!production.get_ValueAsBoolean("IsUseProductionPlan") && production.get_ValueAsInt("C_DocType_ID")==C_Doctype_ID_BPR_RiceToRice) {
				//if bahan baku
				String sqlStmt = "select M_Productionline_ID, qtyused from M_ProductionLine where M_Production_ID = ? and isActive = 'Y' and jenisproduk = 'B'";
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				
				try {
					pstmt = DB.prepareStatement(sqlStmt, null);
					pstmt.setInt(1, production.get_ID());
					rs = pstmt.executeQuery();
					
					while (rs.next()) {
						int M_Productionline_ID = rs.getInt(1);
						BigDecimal qtyused = rs.getBigDecimal(2);
						MProductionLineExt line = new MProductionLineExt(production.getCtx(), M_Productionline_ID, production.get_TrxName());
						
						//validation if qtyused = 0, return error messege
						if(qtyused.compareTo(BigDecimal.ZERO)==0)
							throw new AdempiereException("QtyUsed Bahan Baku tidak boleh 0 pada Bahan Baku dengan produk : "+line.getM_Product().getValue()+"_"+line.getM_Product().getName());							
					}
				} catch (SQLException e) {
					log.log(Level.SEVERE, sqlStmt.toString(), e);
				} finally{
					DB.close(rs, pstmt);
					rs = null;
					pstmt = null;
				}
			}
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
