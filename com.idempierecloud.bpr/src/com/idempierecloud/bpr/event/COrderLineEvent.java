package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MOrder;
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
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			calculateOngkosAngkut();
			calculatePrice();
			calculateLinetNetAmt();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			calculateOngkosAngkut();
			calculatePrice();
			calculateLinetNetAmt();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_DELETE)) {
			checkRequisitionLine();
		}
	}

	private void checkRequisitionLine() {
		int no = DB.executeUpdate("UPDATE M_RequisitionLine SET C_OrderLine_ID=null WHERE C_orderLine_id=?", orderLine.getC_OrderLine_ID(), orderLine.get_TrxName());
		log.info("Updated RequisitionLine "+no);
	}
	private void calculatePrice() {
		if(orderLine.getM_Product_ID()==0)
			return;
		BigDecimal ongkosAngkut = (BigDecimal) orderLine.get_Value("OngkosAngkut");
		BigDecimal price = ongkosAngkut.add(orderLine.getPriceList());
		orderLine.setPriceEntered(price);
	}
	private void calculateLinetNetAmt() {
		if(orderLine.getM_Product_ID()==0)
			return;
		BigDecimal ongkosAngkut = (BigDecimal) orderLine.get_Value("OngkosAngkut");
		if(ongkosAngkut==null)
			ongkosAngkut = Env.ZERO;
		orderLine.setLineNetAmt(orderLine.getLineNetAmt().add(ongkosAngkut));
	}
	private void calculateOngkosAngkut() {
		
		MOrder order = (MOrder)orderLine.getC_Order();
		if(order.getDeliveryViaRule().equalsIgnoreCase("")) {
			return;
		}		
		if(order.getDeliveryViaRule().equalsIgnoreCase("D")) {//Delivery
			if(orderLine.getM_Product_ID()==0)
				return;
			if(orderLine.getC_BPartner_Location_ID()==0)
				return;
			MBPartnerLocation BPLoc = new MBPartnerLocation(orderLine.getCtx(), orderLine.getC_BPartner_Location_ID(), orderLine.get_TrxName());
			BigDecimal BPR_OngkosAngkut = DB.getSQLValueBD(BPLoc.get_TrxName(), "Select OngkosAngkut from BPR_OngkosAngkutDetail where C_City_ID = ?", BPLoc.get_ValueAsInt("C_City_ID"));
			BigDecimal ongkosAngkut = BPR_OngkosAngkut.multiply(orderLine.getQtyEntered()).multiply(orderLine.getM_Product().getWeight());
			orderLine.set_ValueOfColumn("OngkosAngkut", ongkosAngkut);
		}
		else if (order.getDeliveryViaRule().equalsIgnoreCase("P")) {//Pickup
			BigDecimal ongkosAngkut = BigDecimal.ZERO;
			orderLine.set_ValueOfColumn("OngkosAngkut", ongkosAngkut);
		}
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
