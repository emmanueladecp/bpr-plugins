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
		//subquery shipment untuk cek so yang di close hanya yang memiliki outstanding qty
		StringBuilder sql = new StringBuilder ("with shipment as ( select so.c_order_id as so_id from c_orderline col"
				+ "  	join c_order so on so.c_order_id = col.c_order_id"
				+ "		join m_inoutline mi on mi.c_orderline_id = col.c_orderline_id "
				+ "  	join m_inout mi2 on mi2.m_inout_id= mi.m_inout_id"
				+ "  	where mi2.docstatus not in ('VO','RE') "
				+ "  	and mi2.issotrx ='Y' and mi2.movementtype = 'C-'"
				+ "  	group by so.c_order_id"
				+ "  	having sum(col.qtyordered)-sum(mi.movementqty)>0)"
				+ "  ,inv as (select so2.c_order_id as s_id from c_orderline col2"
				+ "  	join c_order so2 on so2.c_order_id = col2.c_order_id"
				+ "		join c_invoiceline invl on col2.c_orderline_id = invl.c_orderline_id"
				+ "		join c_invoice inv on invl.c_invoice_id = inv.c_invoice_id and inv.docstatus = 'CO'"
				+ "  	where inv.docstatus not in ('VO','RE') and inv.issotrx ='Y'"
				+ "  	group by so2.c_order_id"
				+ "  	having (select mino.m_inout_id from c_orderline col3"
				+ "			join m_inoutline minol on minol.c_orderline_id = col3.c_orderline_id "
				+ "		  	join m_inout mino on mino.m_inout_id= minol.m_inout_id"
				+ "  		where mino.docstatus='CO' limit 1) is null) "
				+ " SELECT distinct co.c_order_id FROM c_order co "
				+ "  JOIN c_orderline co2 ON co.c_order_id = co2.c_order_id  "
				+ "  JOIN C_Doctype cd ON cd.C_Doctype_ID = co.C_Doctype_ID "
				+ "  WHERE co.docstatus = 'CO'  AND co.issotrx = 'Y' "
				+ "  AND co.datepromised + INTERVAL '45 DAY'+ (select count(date1) from C_NonBusinessDay "
				+ "  		where date1 between now()-45 and now())<= current_date"
				+ "  and co.IsSOTrx='Y' And co.C_DocTypeTarget_ID IN (select c_doctype_id from c_doctype "
				+ "  		where isactive='Y' and issotrx='Y' and docbasetype='SOO' and docsubtypeso='SO' and isretur='N') "
				+ "  and exists (select so_id from shipment sp where sp.so_id = co.c_order_id) "
				+ "  and not exists (select inv.s_id from inv where inv.s_id = co.c_order_id)"
				+ "  GROUP BY co.c_order_id order by co.c_order_id desc limit 500");
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
