package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MInOutLineConfirm;
import org.compiere.model.PO;
import org.osgi.service.event.Event;
import org.compiere.util.CLogger;

import com.idempierecloud.bpr.base.CustomEvent;

public class MInOutLineConfirmEvent extends CustomEvent {
	
	private static CLogger log = CLogger.getCLogger(MInOutLineConfirmEvent.class);
	private MInOutLineConfirm confirmLine = null;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Line Confirm Event : "+event.getTopic());
		confirmLine = (MInOutLineConfirm) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			setQty();	
		}
	
	}
	
	private void setQty() {
		confirmLine.set_ValueOfColumn("QtyEntered", confirmLine.getM_InOutLine().getQtyEntered());
		confirmLine.set_ValueOfColumn("C_UOM_ID", confirmLine.getM_InOutLine().getC_UOM_ID());
		return;
	}
	
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}
	
}
