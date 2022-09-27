package com.idempierecloud.bpr.event;

import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class LoginEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(LoginEvent.class);

	@Override
	protected void doHandleEvent(Event event) {
		log.info("BPR Plugin Activated");
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
