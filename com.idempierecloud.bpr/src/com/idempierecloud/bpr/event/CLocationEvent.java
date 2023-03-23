package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MLocation;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.util.WebService;

public class CLocationEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CLocationEvent.class);
	
	private MLocation location = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("location Event : "+event.getTopic());
		
		location = (MLocation) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW))
			restrictRMP();
		else if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			sendToRMP();
		}
	}
	
	private void restrictRMP() {
		String client = Env.getContext(location.getCtx(), "#AD_Client_Name");
		if(!client.equals("Belitang"))
			throw new AdempiereException("Not Allowed. Create on Belitang");
	}
	
	private void sendToRMP() {
		String client = Env.getContext(location.getCtx(), "#AD_Client_Name");
		if(!client.equals("Belitang"))
			return;
		
		String[] columns = {"Address1","City","Postal","C_Country_ID"};
		String[] values = new String[columns.length];
		for(int i=0;i<columns.length;i++) {
			values[i] = location.get_ValueAsString(columns[i]);
		}
		WebService webService = new WebService();
		webService.createData("addLocation", "C_Location", columns, values);
		webService.run();
		if(webService.isError())
			throw new AdempiereException(webService.getMessage());
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
