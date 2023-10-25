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
		StringBuilder sql = new StringBuilder ("select distinct co.C_Order_ID "
				+ "	from C_Order co "
				+ "	join C_Doctype dt on co.C_DoctypeTarget_ID = dt.C_Doctype_ID "
				+ "	join c_orderline col on co.C_Order_ID = col.C_Order_ID "
				+ "	where co.datepromised + INTERVAL '45 DAY'+ (select count(date1) from C_NonBusinessDay  where date1 between now()-45 and now())<= current_date "
				+ " and docstatus in ('CO','IP') and co.issotrx = 'Y' and dt.c_doctype_id = 1000084 "
				+ " and (qtyordered <> qtydelivered or qtyordered <> qtyinvoiced) ");
		PreparedStatement pstmnt = null;
		ResultSet rsl = null;
		try
		{
			pstmnt = DB.prepareStatement (sql.toString(), get_TrxName());
			rsl = pstmnt.executeQuery ();
			while (rsl.next ()){
				BigDecimal credit=BigDecimal.ZERO;
				BigDecimal outstanding = BigDecimal.ZERO;
				BigDecimal sumqty = BigDecimal.ZERO;
				BigDecimal sumoutstanding = BigDecimal.ZERO;
				MOrder order = new MOrder(getCtx(), rsl.getInt(1), get_TrxName());
				for(MOrderLine line : order.getLines()) {
					outstanding = DB.getSQLValueBD(get_TrxName(), 
							"  SELECT co2.qtyordered - COALESCE(SUM(mi.movementqty), 0) AS outstanding "
							+ " FROM c_orderline co2 "
							+ " LEFT JOIN m_inoutline mi ON co2.c_orderline_id = mi.c_orderline_id "
							+ " LEFT JOIN m_inout mi2 ON mi.m_inout_id = mi2.m_inout_id AND mi2.docstatus NOT IN ('VO', 'RE') "
							+ " and mi2.issotrx ='Y' and mi2.movementtype = 'C-' "
							+ " WHERE co2.c_orderline_id = ?"
							+ " Group By co2.qtyordered ", line.getC_OrderLine_ID());
					if(outstanding!=null) {
						if(outstanding.compareTo(BigDecimal.ZERO)>0) {
							credit= credit.add(line.getPriceActual().multiply(outstanding));
						}
						sumoutstanding = sumoutstanding.add(outstanding);
						sumqty = line.getQtyOrdered().add(sumqty);
					}
				}
				//Handling full reject
				String ShipCO = DB.getSQLValueString(order.get_TrxName(), "select distinct mi.docstatus"
						+ "  from c_order co"
						+ "  join c_orderline col on co.c_order_id= col.c_order_id "
						+ "  join m_inoutline mil on mil.c_orderline_id = col.c_orderline_id "
						+ "  join m_inout mi on mi.m_inout_id = mil.m_inout_id "
						+ "  where mi.docstatus = 'CO' and col.c_order_id= ?", order.getC_Order_ID());
				// JIKA TOTAL OUTSTANDING QTY SAMA DENGAN TOTAL QTYORDERED MAKA DAPAT DI SIMPULKAN TIDAK ADA PENGIRIMAN PADA SO TERSEBUT SEHINGGA DI VOID
				if(sumoutstanding.compareTo(sumqty)==0 && ShipCO==null) {
					order.setDocAction(MOrder.DOCACTION_Void);
					order.saveEx();
					if(!order.processIt(MOrder.DOCACTION_Void)) {
						log.warning("ScheduleCloseSO. SO GAGAL VOID : "+order.getProcessMsg());
						addLog("ScheduleCloseSO. SO GAGAL VOID : "+order.getProcessMsg());
						continue;
					}
						
				}else if (sumoutstanding.compareTo(sumqty)<1 ||ShipCO!=null) {
					order.setDocAction(MOrder.DOCACTION_Close);
					order.saveEx();
					if(!order.processIt(MOrder.DOCACTION_Close)) {
						log.warning("ScheduleCloseSO. SO GAGAL CLOSE : "+order.getProcessMsg());
						addLog("ScheduleCloseSO. SO GAGAL CLOSE : "+order.getProcessMsg());
						continue;
					}
				}
				order.saveEx();
				if(order.getDocStatus().equals(MOrder.DOCSTATUS_Closed)||order.getDocStatus().equals(MOrder.DOCSTATUS_Voided)) {
					if(credit.compareTo(BigDecimal.ZERO)>0&&!order.isSelfService()) {
						MBPartner cb = (MBPartner)order.getC_BPartner();
		                BigDecimal creditUsed = cb.getSO_CreditUsed().subtract(credit);
		                cb.setSO_CreditUsed(creditUsed);
		                cb.saveEx();
					}
					addLog(order.getDocumentNo()+" : Already Processed");
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
