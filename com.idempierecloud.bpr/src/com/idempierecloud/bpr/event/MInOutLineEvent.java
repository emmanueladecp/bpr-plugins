package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MInOutLine;
import org.compiere.model.MUOMConversion;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class MInOutLineEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(MInOutLineEvent.class);
	private MInOutLine line = null;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Line Event : "+event.getTopic());
		line = (MInOutLine) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			checkQtyEntered();
		}
	}
	private void checkQtyEntered() {
		BigDecimal qtyEntered = line.getQtyEntered();
		
		if(line.getM_Product().getC_UOM_ID()!=line.getC_UOM_ID()) {
			qtyEntered = MUOMConversion.convertProductTo(line.getCtx(), line.getM_Product_ID(), line.getC_UOM_ID(), line.getMovementQty());
			if(line.getQtyEntered().compareTo(qtyEntered)!=0)
				line.setQtyEntered(qtyEntered);
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
