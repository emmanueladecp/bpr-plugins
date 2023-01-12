package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class CInvoiceLineEvent extends CustomEvent {

private static CLogger log = CLogger.getCLogger(CInvoiceLineEvent.class);
	
	private MInvoiceLine invoiceLine = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("invoice line Event : "+event.getTopic());
		
		invoiceLine = (MInvoiceLine) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			setWitholdingType();
		}
			
	}
	
	private void setWitholdingType() {
		if(invoiceLine.getC_Invoice().isSOTrx() || invoiceLine.getC_OrderLine_ID()==0)
			return;
		
		MOrderLine orderLine = (MOrderLine) invoiceLine.getC_OrderLine();
		if(orderLine.get_ValueAsInt("LCO_WithholdingType_ID")>0)
			invoiceLine.set_ValueOfColumn("LCO_WithholdingType_ID", orderLine.get_ValueAsInt("LCO_WithholdingType_ID"));
	}

	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub

	}

}
