package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MBPartner;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class CBPartnerEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CBPartnerLocationEvent.class);
	
	private MBPartner bpartner = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("bp location Event : "+event.getTopic());
		
		bpartner = (MBPartner) po;
		if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			setCreditLimit();
		}
	}

	private void setCreditLimit() {
		BigDecimal creditUsed = DB.getSQLValueBD(bpartner.get_TrxName(), "SELECT calculate_credituse(?)", bpartner.getC_BPartner_ID());			
		bpartner.setSO_CreditUsed(creditUsed);
		bpartner.saveEx();
	}

	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}
	
}
