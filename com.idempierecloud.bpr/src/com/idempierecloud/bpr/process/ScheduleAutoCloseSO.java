package com.idempierecloud.bpr.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.sound.sampled.Line;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MInOut;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;

/* BPR dan RMP
 * Perhitungan Credit limit dan Close SO jika suadh 45 hari masih ada outstanding
 */
public class ScheduleAutoCloseSO extends CustomProcess{

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String doIt() throws Exception {
		StringBuilder sql = new StringBuilder ("SELECT distinct co.c_order_id "
				+ " FROM c_order co "
				+ " JOIN c_orderline co2 ON co.c_order_id = co2.c_order_id "
				+ " LEFT JOIN m_inoutline mi ON co2.c_orderline_id = mi.c_orderline_id "
				+ " LEFT JOIN m_inout mi2 ON mi.m_inout_id = mi2.m_inout_id "
				+ " WHERE co.docstatus = 'CO' "
				+ " AND co.issotrx = 'Y' "
				+ " AND mi2.docstatus NOT IN ('VO', 'RE') "
				+ " AND co.datepromised + INTERVAL '40 DAY' <= current_date "
				+ " GROUP BY co.c_order_id,co2.qtyordered "
				+ " HAVING co2.qtyordered - COALESCE(SUM(mi.movementqty), 0) > 0"
				+ " order by co.c_order_id");
		PreparedStatement pstmnt = null;
		ResultSet rsl = null;
		try
		{
			pstmnt = DB.prepareStatement (sql.toString(), get_TrxName());
			rsl = pstmnt.executeQuery ();
			while (rsl.next ()){
				BigDecimal credit=BigDecimal.ZERO;
				MOrder order = new MOrder(getCtx(), rsl.getInt(1), get_TrxName());
				for(MOrderLine line : order.getLines()) {
					BigDecimal outstanding = DB.getSQLValueBD(get_TrxName(), 
							" SELECT co2.qtyordered - COALESCE(SUM(mi.movementqty), 0) AS outstanding "
							+ " FROM c_orderline co2 "
							+ " LEFT JOIN m_inoutline mi ON co2.c_orderline_id = mi.c_orderline_id "
							+ " LEFT JOIN m_inout mi2 ON mi.m_inout_id = mi2.m_inout_id "
							+ " WHERE mi2.issotrx ='Y' AND mi2.docstatus NOT IN ('VO', 'RE') and co2.c_orderline_id = ? "
							+ " Group By co2.qtyordered", line.getC_OrderLine_ID());
					if(outstanding.compareTo(BigDecimal.ZERO)>0) {
						credit= credit.add(line.getPriceActual().multiply(outstanding));
					}
				}
				order.setDocAction(MInOut.DOCACTION_Close);
				order.saveEx();
				if(!order.processIt(MInOut.DOCACTION_Close))
					log.warning("ScheduleCloseSO. SO GAGAL CLOSE : "+order.getProcessMsg());
				order.saveEx();
				
				if(credit.compareTo(BigDecimal.ZERO)>0) {
					MBPartner cb = (MBPartner)order.getC_BPartner();
	                BigDecimal creditUsed = cb.getSO_CreditUsed().subtract(credit);
	                cb.setSO_CreditUsed(creditUsed);
	                cb.saveEx();
				}
			}
		}
		catch (SQLException e){
			 log.log(Level.SEVERE, " ScheduleAutoCloseSO- " + sql.toString(), e);
		}
		finally{
			DB.close(rsl, pstmnt);
			rsl = null;
			pstmnt = null;
		}
		return null;
	}
	
}
