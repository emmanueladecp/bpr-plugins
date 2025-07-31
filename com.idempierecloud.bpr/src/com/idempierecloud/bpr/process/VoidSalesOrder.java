package com.idempierecloud.bpr.process;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.compiere.model.MOrder;

import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;

public class VoidSalesOrder extends CustomProcess  {
	
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
            sql.append("SELECT C_Order_ID FROM C_Order WHERE DocumentNo IN (");
            sql.append("'123303','123577','123616','123624','123630','123840','123945','123964',");
            sql.append("'123580','123596','123611','123617','123618','123619','123620','123629','123631','123653',");
            sql.append("'123835','123836','124054','123623','123830','123831','123834','123924','123926','123927','123930','123933',");
            sql.append("'123940','123942','123949','123956','123961','124410','124415','123936','123938','123948','124048','124053',");
            sql.append("'124418','124419','124481','124361','124424','124425','124426','124428','124429','124431','124485','124486',");
            sql.append("'124360','124432','124433','124617','124618','124624','124626','124622','124802','124806','124807','124808',");
            sql.append("'124809','124815','124816','124821','124825','124860','124914','125117','125114','125115','125116','125118',");
            sql.append("'125119','125120','125135','125266','125268','125262','125264','125269','125413','125601','126243','126244',");
            sql.append("'126246','126247','126248','126249','126250','126252','126251','126253','126255','126256','126261','126262',");
            sql.append("'126548','126372','127473','127575'");
            sql.append(")");
            
            PreparedStatement pstmnt = null;
    		ResultSet rsl = null;
            		
    		try
    		{
    			pstmnt = DB.prepareStatement (sql.toString(), get_TrxName());
    			rsl = pstmnt.executeQuery ();
    			while (rsl.next ()){
    				MOrder order = new MOrder(getCtx(), rsl.getInt(1), get_TrxName());
    				
    				if (order.isProcessed() && !"VO".equals(order.getDocStatus())) {
                        if (order.voidIt()) {
                        	order.setDocStatus(MOrder.DOCSTATUS_Voided);
                        	order.setDocAction(MOrder.ACTION_None);
                            order.saveEx();
                            addLog("Voided SO: " + rsl.getInt(1));
                            successCount++;
                        } else {
                            addLog("❌ Failed to void SO : " + rsl.getInt(1));
                            skipCount++;
                        }
                    } else {
                        addLog("⏩ Skipped (already voided/unprocessed): " + rsl.getInt(1));
                        skipCount++;
                    }
    			}
    		}
    		catch (SQLException e){
   			 log.log(Level.SEVERE, " VOID SO- " + sql.toString(), e);
	   		}
	   		finally{
	   			DB.close(rsl, pstmnt);
	   			rsl = null;
	   			pstmnt = null;
	   		}
	   		return "✅ Voided: " + successCount + " | ⏭️ Skipped: " + skipCount;
    		
    }
}