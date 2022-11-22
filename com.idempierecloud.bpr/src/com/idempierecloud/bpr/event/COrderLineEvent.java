package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
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
			setPricePOTurus();
			calculateOngkosAngkut();
			calculatePrice();
			setPricePONonTurus();
			calculateLinetNetAmt();
			setDiscount();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			setPricePOTurus();
			calculateOngkosAngkut();
			calculatePrice();
			setPricePONonTurus();
			calculateLinetNetAmt();
			setDiscount();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_DELETE)) {
			checkRequisitionLine();
		}
	}

	private void setPricePONonTurus() {
		MOrder order = (MOrder)orderLine.getC_Order();
		if(order.get_ValueAsBoolean("isSOTrx"))
			return;
		MDocType docType = (MDocType) orderLine.getC_Order().getC_DocTypeTarget();
		if(docType.get_ValueAsBoolean("isTurus"))
			return;
		if(orderLine.getPriceEntered().compareTo(BigDecimal.ZERO)>0) {
			orderLine.setPriceActual(orderLine.getPriceEntered());
			orderLine.setPriceList(orderLine.getPriceEntered());
		}
	}
	private void setPricePOTurus() {
		MDocType docType = (MDocType) orderLine.getC_Order().getC_DocTypeTarget();
		if(!docType.get_ValueAsBoolean("isTurus"))
			return;
		
		if(orderLine.get_ValueAsBoolean("isFOC")) {
			orderLine.setPriceEntered(Env.ZERO);
			orderLine.setPriceList(Env.ZERO);
			orderLine.setPriceActual(Env.ZERO);
			orderLine.setPriceLimit(Env.ZERO);
			return;
		}
		
		if(orderLine.get_ValueAsInt("relatedProduct_ID")==0)
			return;
		
		MProduct relatedProduct = new MProduct(orderLine.getCtx(), orderLine.get_ValueAsInt("relatedProduct_ID"), orderLine.get_TrxName());
		int M_PriceList_Version_ID = DB.getSQLValue(orderLine.get_TrxName(), "SELECT M_PriceList_Version_ID FROM M_PriceList_Version WHERE M_PriceList_ID=? AND ValidFrom<=? order By ValidFrom DESC Limit 1", orderLine.getC_Order().getM_PriceList_ID(), orderLine.getC_Order().getDateOrdered());
		if(M_PriceList_Version_ID<=0)
			throw new AdempiereException("No Product Price for "+relatedProduct.getName());
		
		MProductPrice price = MProductPrice.get(orderLine.getCtx(), M_PriceList_Version_ID, orderLine.get_ValueAsInt("relatedProduct_ID"), orderLine.get_TrxName());
		orderLine.setPriceEntered(price.getPriceStd());
		orderLine.setPriceList(price.getPriceList());
		orderLine.setPriceActual(price.getPriceList());
		orderLine.setPriceLimit(price.getPriceLimit());
	}

	private void checkRequisitionLine() {
		int no = DB.executeUpdate("UPDATE M_RequisitionLine SET C_OrderLine_ID=null WHERE C_orderLine_id=?", orderLine.getC_OrderLine_ID(), orderLine.get_TrxName());
		log.info("Updated RequisitionLine "+no);
	}
	private void calculatePrice() {
		MOrder order = (MOrder)orderLine.getC_Order();
		if(!order.get_ValueAsBoolean("isSOTrx"))
			return;
		if(orderLine.getM_Product_ID()==0)
			return;
		MDocType docType = (MDocType) orderLine.getC_Order().getC_DocTypeTarget();
		if(docType.get_ValueAsBoolean("isTurus"))
			return;
		BigDecimal ongkosAngkut = (BigDecimal) orderLine.get_Value("OngkosAngkut");
		BigDecimal price = ongkosAngkut.add(orderLine.getPriceList());
		orderLine.setPriceEntered(price);
	}
	private void calculateLinetNetAmt() {
		if(orderLine.getM_Product_ID()==0)
			return;
		BigDecimal LineNetAmt = orderLine.getPriceEntered().multiply(orderLine.getQtyOrdered());	
		orderLine.setLineNetAmt(LineNetAmt);
	}
	private void calculateOngkosAngkut() {
		MOrder order = (MOrder)orderLine.getC_Order();
		if(!order.get_ValueAsBoolean("isSOTrx"))
			return;
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
			BigDecimal ongkosAngkut = BPR_OngkosAngkut.multiply(orderLine.getQtyOrdered()).multiply(orderLine.getM_Product().getWeight());
			orderLine.set_ValueOfColumn("OngkosAngkut", ongkosAngkut);
		}
		else if (order.getDeliveryViaRule().equalsIgnoreCase("P")) {//Pickup
			BigDecimal ongkosAngkut = BigDecimal.ZERO;
			orderLine.set_ValueOfColumn("OngkosAngkut", ongkosAngkut);
		}
	}
	private void setDiscount() {
		if(orderLine.getM_Product_ID()==0)
			return;
		orderLine.setDiscount(BigDecimal.ZERO);
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
