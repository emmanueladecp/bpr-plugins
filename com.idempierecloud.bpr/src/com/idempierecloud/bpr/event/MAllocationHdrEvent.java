package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class MAllocationHdrEvent extends CustomEvent{

	private static CLogger log = CLogger.getCLogger(MAllocationHdrEvent.class);
	String m_processMsg = null;
	private MAllocationHdr alloc = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Allocation Event : "+event.getTopic());
		
		alloc = (MAllocationHdr) po;
		if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			setCreditUsed();
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_REVERSECORRECT)) {
			setCreditUsed();
		}
	}

	private void setCreditUsed() {
		for(MAllocationLine line : alloc.getLines(false)) {
			MBPartner bpartner = new MBPartner(line.getCtx(), line.getC_BPartner_ID(), line.get_TrxName());
			BigDecimal creditUsed = DB.getSQLValueBD(alloc.get_TrxName(), "SELECT calculate_credituse(?)+?", bpartner.getC_BPartner_ID(),line.getAmount());            
			bpartner.setSO_CreditUsed(creditUsed);
			bpartner.saveEx();
		}
	}

	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}
}
