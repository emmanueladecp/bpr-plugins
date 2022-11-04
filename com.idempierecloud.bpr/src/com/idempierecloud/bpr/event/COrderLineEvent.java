package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class COrderLineEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(COrderLineEvent.class);
	
	private MOrderLine orderLine = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("OrderLine Event : "+event.getTopic());
		
		orderLine = (MOrderLine) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW))
			calculateLinetNetAmt();
		else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE))
			calculateLinetNetAmt();
		else if(event.getTopic().equals(IEventTopics.PO_BEFORE_DELETE))
			checkRequisitionLine();
	}

	private void checkRequisitionLine() {
		int no = DB.executeUpdate("UPDATE M_RequisitionLine SET C_OrderLine_ID=null WHERE C_orderLine_id=?", orderLine.getC_OrderLine_ID(), orderLine.get_TrxName());
		log.info("Updated RequisitionLine "+no);
	}

	private void calculateLinetNetAmt() {
		if(orderLine.getM_Product_ID()==0)
			return;
		
		BigDecimal ongkosAngkut = (BigDecimal) orderLine.get_Value("OngkosAngkut");
		if(ongkosAngkut==null)
			ongkosAngkut = Env.ZERO;
		
		orderLine.setLineNetAmt(orderLine.getLineNetAmt().add(ongkosAngkut));
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
