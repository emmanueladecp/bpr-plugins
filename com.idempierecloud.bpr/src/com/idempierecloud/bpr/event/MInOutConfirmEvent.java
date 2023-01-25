package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutConfirm;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInOutLineConfirm;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
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
			checkPicklist();	
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			completeShipment();	
			createMovementReject();
		}
	
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
					int MLocator_Retur = DB.getSQLValue(line.get_TrxName(), "Select * from M_Locator where  M_LocatorType_ID=1000004 and M_Warehouse_ID=?", shipment.getM_Warehouse_ID());
					if(MLocator_Retur>0)
						mline.setM_LocatorTo_ID(MLocator_Retur);
					else 
						throw new AdempiereException("Locator Retur tidak ditemukan");
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
