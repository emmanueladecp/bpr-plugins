package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class MInOutEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	private MInOut inout = null;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Event : "+event.getTopic());
		inout = (MInOut) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_PREPARE)) {
			checkAvailableQtyProduct();
		}
	}
	private void checkAvailableQtyProduct() {
		if(inout.isSOTrx()) {
			MInOutLine[] lines = inout.getLines(true);
			for (MInOutLine line : lines) {
				BigDecimal qtyAvailable = DB.getSQLValueBD(inout.get_TrxName(), "SELECT COALESCE(SUM(QtyOnHand), 0)"
						+ "	FROM M_Storageonhand s"
						+ "	WHERE s.M_Product_ID=? AND s.m_locator_id=?", line.getM_Product_ID(),line.getM_Locator_ID());
				if(qtyAvailable.compareTo(line.getMovementQty())<0)
					throw new AdempiereException("Gagal Complete!! Quantity Avaibility = "+qtyAvailable+", Quantity Movement = "+line.getMovementQty()+", pada Shipment Line : "+line.getLine()+", Product : "+line.getM_Product().getValue()+"_"+line.getM_Product().getName()+" locator "+line.getM_Locator().getValue());
			}
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
