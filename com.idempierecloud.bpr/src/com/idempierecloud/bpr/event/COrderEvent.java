package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MOrder;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class COrderEvent extends CustomEvent{

	private static CLogger log = CLogger.getCLogger(COrderLineEvent.class);
	
	private MOrder order = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Order Event : "+event.getTopic());
		
		order = (MOrder) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			checkSalesRep();
			setCreditAvailable();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			checkSalesRep();
			setCreditAvailable();
		}
	}
	private void setCreditAvailable() {
		if(order.isSOTrx()) {//if sales order
			if(order.getC_BPartner_ID()>0) {
				if(order.getC_BPartner_ID()!=order.get_ValueAsInt("C_BPartnerSR_ID")) {//if document new or c_bpartner_id is change
					BigDecimal SO_CreditAvailable = order.getC_BPartner().getSO_CreditLimit().subtract(order.getC_BPartner().getSO_CreditUsed());
					order.set_ValueOfColumn("SO_CreditAvailable", SO_CreditAvailable);
					order.set_ValueOfColumn("C_BPartnerSR_ID", order.getC_BPartner_ID());
				}
			}
		}
	}
	
	private void checkSalesRep() {
		if(order.get_ValueAsInt("SalesRep_ID2")>0)
			order.setSalesRep_ID(order.get_ValueAsInt("SalesRep_ID2"));
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
