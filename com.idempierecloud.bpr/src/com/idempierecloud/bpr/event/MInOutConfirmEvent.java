package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutConfirm;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInOutLineConfirm;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.MOrder;
import org.compiere.model.PO;
import org.osgi.service.event.Event;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomEvent;

public class MInOutConfirmEvent extends CustomEvent {
	
	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	private MInOutConfirm confirm = null;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Confirm Event : "+event.getTopic());
		confirm = (MInOutConfirm) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			setProcessed();
			checkPicklist();	
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			completeShipment();	
			autoCloseSO();
		}
	
	}
	private void autoCloseSO() {
		/*Nomor CR ICT 210
		 * Title Auto close SO saat ship receipt confirm untuk semua item order ditolak
		 * BPR dan RMP
		 */
		 MInOut shipment = (MInOut) confirm.getM_InOut();
		 /*QUERY CHECK APAKAH SO DI TARIK FULL ATAU DI TARIK PARTIAL KE SHIPMENT*/
		 StringBuilder sql = new StringBuilder ("SELECT co.c_order_id,sum(co2.qtyordered)-sum(mi3.movementqty) "
		 		+ " from m_inoutconfirm mi  "
		 		+ " join m_inoutlineconfirm mi2 on mi2.m_inoutconfirm_id = mi.m_inoutconfirm_id "
		 		+ " join m_inoutline mi3 on mi2.m_inoutline_id = mi3.m_inoutline_id "
		 		+ " join c_orderline co2 on mi3.c_orderline_id = co2.c_orderline_id "
		 		+ " join c_order co on co2.c_order_id = co.c_order_id "
		 		+ " where co.issotrx = 'Y' and mi.m_inoutconfirm_id=? "
		 		+ " group by co.c_order_id ");
	 		PreparedStatement pstmnt = null;
	 		ResultSet rs = null;
	 		try
	 		{
	 			pstmnt = DB.prepareStatement (sql.toString(), confirm.get_TrxName());
	 			int index = 1; 
	 	        pstmnt.setInt(index++, confirm.getM_InOutConfirm_ID());
	 	        rs = pstmnt.executeQuery ();
	 			while (rs.next ()){
	 				MOrder so = new MOrder(confirm.getCtx(), rs.getInt(1), confirm.get_TrxName());
	 				/*JIKA PARSIAL*/
	 				if(rs.getBigDecimal(2).compareTo(BigDecimal.ZERO)>0) {
	 					/*CEK APAKAH SO LAINNYA SUDAH DITARIK SHIPMENT COMPLETE, maka SO boleh di Close*/
	 					int check = DB.getSQLValue(confirm.get_TrxName(), "select co.c_order_id from m_inout mi "
	 							+ " join m_inoutline mi2 on mi.m_inout_id = mi2.m_inout_id "
	 							+ " join c_orderline co on co.c_orderline_id = mi2.c_orderline_id "
	 							+ " where co.c_order_id = ? and mi.docstatus = 'CO'", rs.getInt(1));
	 					if(check>0) {
	 						if(so.getDocStatus().equals(MOrder.DOCSTATUS_Completed)) {
		 						 so.setDocAction(MOrder.DOCACTION_Close);
		 						 so.saveEx();
		 						 
		 						 if(!so.processIt(MOrder.DOCACTION_Close)) {
		 							 throw new AdempiereException("Sales Order gagal Close : "+so.getProcessMsg());
		 						 }
		 					 }
	 					}
	 					
	 					/*Jika ada Difference maka akan mengembalikan/mengurangi credit used*/
	 					BigDecimal creditUsedBack = DB.getSQLValueBD(confirm.get_TrxName(), "select sum(mi.differenceqty*co.priceactual)"
	 					 		+ " from m_inoutlineconfirm mi "
	 					 		+ " join m_inoutline mi2 on mi.m_inoutline_id = mi2.m_inoutline_id "
	 					 		+ " join c_orderline co on mi2.c_orderline_id = co.c_orderline_id "
	 					 		+ " join m_inout mi3 on mi3.m_inout_id = mi2.m_inout_id "
	 					 		+ " where mi3.docstatus = 'CO' and mi.m_inoutconfirm_id = ? "
	 					 		+ " and co.c_order_id = ?",confirm.getM_InOutConfirm_ID(), rs.getInt(1));
	 					 if(creditUsedBack.compareTo(BigDecimal.ZERO)>0) {
	 						 MBPartner cb = (MBPartner)so.getC_BPartner();
	 						 BigDecimal creditUsed = cb.getSO_CreditUsed().subtract(creditUsedBack);
	 						 cb.setSO_CreditUsed(creditUsed);
	 						 cb.saveEx();
	 					 }
	 					createMovementReject();
	 				}else {/*JIKA DITARIK FULL SO AUTO CLOSE*/
	 					 if(so.getDocStatus().equals(MOrder.DOCSTATUS_Completed)) {
	 						 so.setDocAction(MOrder.DOCACTION_Close);
	 						 so.saveEx();
	 						 
	 						 if(!so.processIt(MOrder.DOCACTION_Close)) {
	 							 throw new AdempiereException("Sales Order gagal Close : "+so.getProcessMsg());
	 						 }
	 					 }
	 					 
	 					/*Jika ada Difference maka akan mengembalikan/mengurangi credit used*/
	 					BigDecimal creditUsedBack = DB.getSQLValueBD(confirm.get_TrxName(), "select sum(mi.differenceqty*co.priceactual)"
	 					 		+ " from m_inoutlineconfirm mi "
	 					 		+ " join m_inoutline mi2 on mi.m_inoutline_id = mi2.m_inoutline_id "
	 					 		+ " join c_orderline co on mi2.c_orderline_id = co.c_orderline_id "
	 					 		+ " join m_inout mi3 on mi3.m_inout_id = mi2.m_inout_id "
	 					 		+ " where mi3.docstatus = 'CO' and mi.m_inoutconfirm_id = ? "
	 					 		+ " and co.c_order_id = ?",confirm.getM_InOutConfirm_ID(), rs.getInt(1));
	 					 if(creditUsedBack.compareTo(BigDecimal.ZERO)>0) {
	 						 MBPartner cb = (MBPartner)so.getC_BPartner();
	 						 BigDecimal creditUsed = cb.getSO_CreditUsed().subtract(creditUsedBack);
	 						 cb.setSO_CreditUsed(creditUsed);
	 						 cb.saveEx();
	 					 }
	 					createMovementReject();
	 				}
	 			}
	 		}
	 		catch (SQLException e){
	 			log.log(Level.SEVERE, " MInOutConfirmEvent - " + sql.toString(), e);
	 		}
	 		finally{
	 			DB.close(rs, pstmnt);
	 			rs = null;
	 			pstmnt = null;
	 		}	 
	}
			   
	private void setProcessed() {
		MInOut inout = (MInOut) confirm.getM_InOut();
		inout.setProcessed(false);
		inout.saveEx();
	}

	private void checkPicklist() {
		if(confirm.getM_InOut_ID()==0)
			return;
		
		int count = DB.getSQLValue(confirm.get_TrxName(), "select count(1) from BPR_PicklistLine pl"
				+ " join BPR_Picklist p on pl.bpr_picklist_id = p.bpr_picklist_id"
				+ " where pl.m_inout_id =? and p.docstatus in ('CO','CL')", confirm.getM_InOut_ID());
		if(count==0)
			throw new AdempiereException("Belum ada picklist complete untuk shipment "+confirm.getM_InOut().getDocumentNo());
	}
	
	private void completeShipment() {
		if(confirm.getM_InOut_ID()==0)
			return;
		confirm.setProcessed(true);
		confirm.setDocStatus(MInOutConfirm.STATUS_Completed);
		confirm.saveEx();
		
		MInOut shipment = (MInOut) confirm.getM_InOut();
		shipment.setDocAction(MInOut.DOCACTION_Complete);
		shipment.saveEx();
		if(!shipment.processIt(MInOut.DOCACTION_Complete))
			throw new AdempiereException("Shipment gagal Complete : "+shipment.getProcessMsg());
		shipment.saveEx();
		return;
	}
	
	private void createMovementReject() {
		int count = DB.getSQLValue(confirm.get_TrxName(), "select coalesce(sum(DifferenceQty), 0) from M_InOutLineConfirm where M_InOutConfirm_ID=?", confirm.getM_InOutConfirm_ID());
		final int DocType_MovementReject = 1000098;
		if(count>0) {
			MInOut shipment = (MInOut) confirm.getM_InOut();
			MMovement movement = new MMovement(confirm.getCtx(), 0, confirm.get_TrxName());
			movement.setAD_Org_ID(shipment.getAD_Org_ID());
			movement.setPOReference(shipment.getDocumentNo());
			movement.setC_DocType_ID(DocType_MovementReject);
			movement.setDescription("REJECT");
			movement.setM_Warehouse_ID(shipment.getM_Warehouse_ID());
			movement.setM_WarehouseTo_ID(shipment.getM_Warehouse_ID());
			movement.setMovementDate(shipment.getMovementDate());
			movement.setDocAction(MMovement.DOCSTATUS_Drafted);
			movement.setDocAction(MMovement.DOCACTION_Complete);
			movement.setIsApproved(true);
			movement.saveEx();
			for(MInOutLineConfirm line: confirm.getLines(false)) {
				if(line.getDifferenceQty().compareTo(BigDecimal.ZERO)>0) {
					MInOutLine shipLine = (MInOutLine) line.getM_InOutLine();
					MMovementLine mline = new MMovementLine(movement.getCtx(),0, movement.get_TrxName());
					mline.setAD_Org_ID(movement.getAD_Org_ID());
					mline.setM_Movement_ID(movement.getM_Movement_ID());
					mline.setM_Product_ID(shipLine.getM_Product_ID());
					mline.setM_Locator_ID(shipLine.getM_Locator_ID());
					if(line.getDifferenceQty().compareTo(line.getTargetQty())<0) {
						int MLocator_Retur = DB.getSQLValue(line.get_TrxName(), "Select * from M_Locator where  M_LocatorType_ID=1000004 and M_Warehouse_ID=?", shipment.getM_Warehouse_ID());
						if(MLocator_Retur>0)
							mline.setM_LocatorTo_ID(MLocator_Retur);
						else 
							throw new AdempiereException("Locator Retur tidak ditemukan");
					}
					mline.setMovementQty(line.getDifferenceQty());
					mline.set_ValueOfColumn("timbangannetamt", BigDecimal.ZERO);
					mline.saveEx();
				}
			}
			if(!movement.processIt(MMovement.DOCACTION_Complete))
				throw new AdempiereException("Inventory Move (Retur) gagal Complete : "+shipment.getProcessMsg());
			movement.saveEx();
			log.info("Create Movement Reject from ship/receipt confirm "+movement.getDocumentNo());
		}
	}
	
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}
	
}
