package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MMovementLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRMaterialRequestLine;

public class MMovementLineEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(MMovementLineEvent.class);
	
	private MMovementLine line = null;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("movement line Event : "+event.getTopic());
		
		line = (MMovementLine) po;
		
		if(event.getTopic().equals(IEventTopics.PO_AFTER_DELETE)) {
			checkMovementRequest();
		}
	}

	private void checkMovementRequest() {
		MBPRMaterialRequestLine requestLine = new Query(line.getCtx(), MBPRMaterialRequestLine.Table_Name, "M_MovementLine_ID=?", line.get_TrxName())
				.setParameters(line.getM_MovementLine_ID())
				.first();
		if(requestLine==null)
			return;
		
		requestLine.setM_MovementLine_ID(0);
		requestLine.saveEx();
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub

	}

}
