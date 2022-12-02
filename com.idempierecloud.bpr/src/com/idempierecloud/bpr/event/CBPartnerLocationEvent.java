package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MLocation;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class CBPartnerLocationEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CBPartnerLocationEvent.class);
	
	private MBPartnerLocation bpLocation = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("invoice Event : "+event.getTopic());
		
		bpLocation = (MBPartnerLocation) po;
		if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW))
			updateLocation();
		else if(event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE))
			updateLocation();
	}
	
	private void updateLocation() {
		if(bpLocation.getC_Location_ID()==0)
			return;
		
		MLocation location = (MLocation) bpLocation.getC_Location();
		location.setAddress1(bpLocation.getName());
		location.saveEx();
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
