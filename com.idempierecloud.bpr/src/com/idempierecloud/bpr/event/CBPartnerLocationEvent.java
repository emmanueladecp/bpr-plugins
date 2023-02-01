package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.util.WebService;

public class CBPartnerLocationEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CBPartnerLocationEvent.class);
	
	private MBPartnerLocation bpLocation = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("bp location Event : "+event.getTopic());
		
		bpLocation = (MBPartnerLocation) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW))
			restrictRMP();
		else if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			updateLocation();
			//sendToRMP();
		}else if(event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)) {
			updateLocation();
		}
	}
	
	private void restrictRMP() {
		String client = Env.getContext(bpLocation.getCtx(), "#AD_Client_Name");
		if(!client.equals("Belitang"))
			throw new AdempiereException("Not Allowed. Create on Belitang");
	}
	
	private void sendToRMP() {
		String client = Env.getContext(bpLocation.getCtx(), "#AD_Client_Name");
		if(!client.equals("Belitang"))
			return;
		
		String[] columns = {"C_BPartner_ID","C_Country_ID","C_Region_ID","C_City_ID",
				"BPR_District_ID","BPR_Village_ID","C_Location_ID","Name","C_SalesRegion_ID",
				"newcust_latitude","newcust_longitude"};
		String[] values = new String[columns.length];
		for(int i=0;i<columns.length;i++) {
			values[i] = bpLocation.get_ValueAsString(columns[i]);
		}
		WebService webService = new WebService();
		webService.createData("addBPLocation", "C_BPartner_Location", columns, values);
		webService.run();
		if(webService.isError())
			throw new AdempiereException(webService.getMessage());
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
