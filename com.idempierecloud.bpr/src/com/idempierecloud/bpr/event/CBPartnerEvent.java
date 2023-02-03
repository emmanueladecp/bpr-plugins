package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.util.WebService;

public class CBPartnerEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CBPartnerEvent.class);
	
	private MBPartner bp = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("BPartner Event : "+event.getTopic());
		
		bp = (MBPartner) po;
		//if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW))
			//restrictRMP();
//		else if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW))
//			sendToRMP();
	}
	
	private void restrictRMP() {
		String client = Env.getContext(bp.getCtx(), "#AD_Client_Name");
		if(!client.equals("Belitang"))
			throw new AdempiereException("Not Allowed. Create on Belitang");
	}

	private void sendToRMP() {
		String client = Env.getContext(bp.getCtx(), "#AD_Client_Name");
		if(!client.equals("Belitang"))
			return;
		
		String[] columns = {"Value","Name","C_BP_Group_ID","TaxID","M_PriceList_ID",
				"C_PaymentTerm_ID","SO_CreditLimit","Name2","IsCustomer","AD_Org_ID",
				"IsVendor","C_Greeting_ID","KtpID","SalesRep_ID"};
		String[] values = new String[columns.length];
		for(int i=0;i<columns.length;i++) {
			values[i] = bp.get_ValueAsString(columns[i]);
		}
		WebService webService = new WebService();
		webService.createData("createBPartner", "C_BPartner", columns, values);
		webService.run();
		if(webService.isError())
			throw new AdempiereException(webService.getMessage());
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
