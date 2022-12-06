package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutConfirm;
import org.compiere.model.PO;
import org.osgi.service.event.Event;
import org.compiere.util.CLogger;

import com.idempierecloud.bpr.base.CustomEvent;

public class MInOutConfirmEvent extends CustomEvent {
	
	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	private MInOutConfirm confirm = null;
	private static final int C_DocType_ID_MM_Shipment_with_Confirmation =1000058;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Confirm Event : "+event.getTopic());
		confirm = (MInOutConfirm) po;
		if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			completeShipment();	
		}
	
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
	
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}
	
}
