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
		int Day = DB.getSQLValue(get_TrxName(), "Select Value from AD_SysConfig where AD_SysConfig_ID=1000009");
		if(Day<=0) {
			Day= 0;
		}
			
		StringBuilder sql = new StringBuilder ("select distinct co.C_Order_ID "
				+ "	from C_Order co "
				+ "	join C_Doctype dt on co.C_DoctypeTarget_ID = dt.C_Doctype_ID "
				+ "	join c_orderline col on co.C_Order_ID = col.C_Order_ID "
				+ "	where co.datepromised + INTERVAL '"+Day+" DAY'+ (select count(date1) from C_NonBusinessDay  where date1 between now()-"+Day+" and now())<= current_date "
				+ " and docstatus in ('CO','IP') and co.issotrx = 'Y' and dt.c_doctype_id not in (1000084) "
				+ " and (qtyordered <> qtydelivered or qtyordered <> qtyinvoiced)  and qtydelivered >= qtyinvoiced ");
		PreparedStatement pstmnt = null;
		ResultSet rsl = null;
		try
		{
			pstmnt = DB.prepareStatement (sql.toString(), get_TrxName());
			rsl = pstmnt.executeQuery ();
			while (rsl.next ()){
				BigDecimal credit		=BigDecimal.ZERO;
				BigDecimal outstanding  = BigDecimal.ZERO;
				BigDecimal sumqty 		= BigDecimal.ZERO;
				BigDecimal sumoutstanding 	 = BigDecimal.ZERO;
				int ExistsShipmentFullReject = 0;
				int ShipmentFullReject 		 = 0;
				int ExistsShipmentInProgress = 0;
				int ShipmentInProgress 		 = 0;
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
					
					/*
					 * Check Apakah ada Shipment line atas SO Line ini
					 */
					ShipmentFullReject = DB.getSQLValue(get_TrxName(), "select 1 from m_inoutline mi join m_inout mi2 on mi2.m_inout_id = mi.m_inout_id where mi2.docstatus in ('CO') and mi.c_orderline_id = ? ", line.getC_OrderLine_ID());
					if(ShipmentFullReject<0) {
						ShipmentFullReject=0;
					}
					ExistsShipmentFullReject +=ShipmentFullReject;
					
					ShipmentInProgress = DB.getSQLValue(get_TrxName(), "select 1 from m_inoutline mi join m_inout mi2 on mi2.m_inout_id = mi.m_inout_id where mi2.docstatus in ('IP') and mi.c_orderline_id = ? ", line.getC_OrderLine_ID());
					if(ShipmentInProgress<0) {
						ShipmentInProgress=0;
					}
					ExistsShipmentInProgress +=ShipmentInProgress;
				}
				
				// JIKA TOTAL OUTSTANDING QTY SAMA DENGAN TOTAL QTYORDERED MAKA DAPAT DI SIMPULKAN TIDAK ADA PENGIRIMAN PADA SO TERSEBUT SEHINGGA DI VOID
				if(sumoutstanding.compareTo(sumqty)==0 
						&& ExistsShipmentFullReject==0&&ExistsShipmentInProgress==0) {//jika menemukan shipment yang full reject maka tidak di void namun di close
					order.setDocAction(MOrder.DOCACTION_Void);
					order.saveEx();
					if(!order.processIt(MOrder.DOCACTION_Void)) {
						log.warning("ScheduleCloseSO. SO GAGAL VOID : "+order.getProcessMsg());
						addLog("ScheduleCloseSO. SO GAGAL VOID : "+order.getProcessMsg());
						continue;
					}
						
				}else if (ExistsShipmentInProgress<=0 //Jika ada shipment inprogress maka tidak boleh di close 
						&& (sumoutstanding.compareTo(sumqty)<1 || ExistsShipmentFullReject>0) ) {
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
