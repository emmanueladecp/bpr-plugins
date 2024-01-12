package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class MAllocationHdrEvent extends CustomEvent {
private static CLogger log = CLogger.getCLogger(MAllocationHdrEvent.class);
	
	private MAllocationHdr allocation = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("OrderLine Event : "+event.getTopic());
		
		allocation = (MAllocationHdr) po;
		if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			setCreditUsed();
		}
	}

	private void setCreditUsed() {
		boolean isCustomer = false;
		MInvoice invoice = null;
		for(MAllocationLine line : allocation.getLines(true)) {
			if(line.getC_Invoice_ID()>0) {
				if(line.getC_Invoice().isSOTrx()) {
					isCustomer = true;
					invoice = (MInvoice) line.getC_Invoice();
				}
			}
		}	
		if(isCustomer) {
			MBPartner bp = (MBPartner) invoice.getC_BPartner();
			BigDecimal CreditUsed = DB.getSQLValueBD(invoice.get_TrxName(), "SELECT calculate_credituse(?)", bp.getC_BPartner_ID());
			bp.setSO_CreditUsed(CreditUsed);
			bp.save();
		}
	}

	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}
}
