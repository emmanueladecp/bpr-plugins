package com.idempierecloud.bpr.process;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.compiere.model.MRequisition;
import org.compiere.process.DocAction;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;

public class CloseRequisition extends CustomProcess  {
	
	@Override
    protected void prepare() {
        // No parameters needed now, since we use attachment
    }

    @Override
    protected String doIt() throws Exception {
    	

        int successCount = 0;
        int skipCount = 0;

        
            //String line;
            //boolean isFirstLine = true;

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT m_requisition_id FROM M_Requisition WHERE ad_client_id = 1000003 AND docstatus = 'CO' AND EXTRACT(YEAR FROM created) IN (2023, 2024) order by CREATED desc ");
            
            PreparedStatement pstmnt = null;
    		ResultSet rsl = null;
            		
    		try
    		{
    			pstmnt = DB.prepareStatement (sql.toString(), get_TrxName());
    			rsl = pstmnt.executeQuery ();
    			while (rsl.next ()){
    				MRequisition requisition = new MRequisition(getCtx(), rsl.getInt(1), get_TrxName());
    				
    				if (requisition.isProcessed() && "CO".equals(requisition.getDocStatus())) {
                        if (requisition.closeIt()) {
                        	requisition.setDocStatus(MRequisition.DOCSTATUS_Closed);
                        	requisition.setDocAction(DocAction.ACTION_None);
                        	requisition.saveEx();
                            addLog("Closed Requisition: " + rsl.getInt(1));
                            successCount++;
                        } else {
                            addLog("❌ Failed to Closed Requisition : " + rsl.getInt(1));
                            skipCount++;
                        }
                    } else {
                        addLog("⏩ Skipped (already closed/unprocessed): " + rsl.getInt(1));
                        skipCount++;
                    }
    			}
    		}
    		catch (SQLException e){
   			 log.log(Level.SEVERE, " Closed Requisition- " + sql.toString(), e);
	   		}
	   		finally{
	   			DB.close(rsl, pstmnt);
	   			rsl = null;
	   			pstmnt = null;
	   		}
	   		return "✅ Closed: " + successCount + " | ⏭️ Skipped: " + skipCount;
    		
    }
}