package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRPicklistLine;

public class MBPRPicklistLineEvent extends CustomEvent{

	private static CLogger log = CLogger.getCLogger(MBPRPicklistLine.class);
	private MBPRPicklistLine line = null;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Event : "+event.getTopic());
		line = (MBPRPicklistLine) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			checkAvailableQtyProduct();
			checkQtySalesOrder();
		}
	}
	
	private void checkAvailableQtyProduct() {
		if(line.getM_InOut_ID()>0) {
			MInOut inout = (MInOut) line.getM_InOut();
			if(!inout.isSOTrx() || !inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerShipment))
				return;
			MInOutLine[] lines = inout.getLines(true);
			for (MInOutLine line : lines) {
				BigDecimal qtyonhand = DB.getSQLValueBD(inout.get_TrxName(), "SELECT COALESCE(SUM(QtyOnHand), 0) FROM M_Storageonhand s"
						+ "	WHERE s.M_Product_ID=? AND s.m_locator_id=?", line.getM_Product_ID(),line.getM_Locator_ID());
				
				BigDecimal qtyIntransit = DB.getSQLValueBD(inout.get_TrxName(), "SELECT COALESCE(SUM(s.confirmedqty), 0)"
						+ "	FROM M_InOutLineConfirm s"
						+ "	JOIN M_InOutConfirm c ON s.M_InOutConfirm_ID=c.M_InOutConfirm_ID"
						+ "	JOIN M_InOutLine iol ON s.M_InOutLine_ID=iol.M_InOutLine_ID"
						+ "	WHERE c.docstatus in ('DR','IP','IN') AND iol.M_Product_ID=? AND iol.m_locator_id=? "
						+ " and iol.m_inoutline_id not in (?)"
						, line.getM_Product_ID(),line.getM_Locator_ID(),line.getM_InOutLine_ID());
				
				BigDecimal qtyAvailable = qtyonhand.subtract(qtyIntransit).subtract(line.getMovementQty());
				
				if(qtyAvailable.signum()<0)
					throw new AdempiereException("Gagal Create Picklist, Please Check Quantity Shipment!!"
							+", Quantity Available : "+qtyAvailable.add(line.getMovementQty())
							+", Quantity OnHand : "+qtyonhand
							+", Quantity Intransit : "+qtyIntransit
							+", Quantity Movement : "+line.getMovementQty()
							+", pada Shipment Line : "+line.getLine()
							+", Product : "+line.getM_Product().toString()
							+" Locator "+line.getM_Locator().getValue());
			}
		}else {
			throw new AdempiereException("PicklistLine tidak memiliki Shipment ID!!");
		}
	}
	
	private void checkQtySalesOrder() {
		MInOut inout = (MInOut) line.getM_InOut();
		if(!inout.isSOTrx() || !inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerShipment))
			return;
		MInOutLine[] lines = inout.getLines(true);
		for (MInOutLine line : lines) {
			BigDecimal MovementQty = DB.getSQLValueBD(inout.get_TrxName(), "Select coalesce(sum(mi.movementqty),0) "
					+ "	from m_inoutline mi "
					+ "	join m_inout mi2 ON mi.m_inout_id = mi2.m_inout_id "
					+ "	left join m_inoutlineconfirm mi3 on mi3.m_inoutline_id = mi.m_inoutline_id"
					+ "	left join m_inoutconfirm mi4 on mi3.m_inoutconfirm_id = mi4.m_inoutconfirm_id"
					+ " where mi.c_orderline_id = ? and mi2.docstatus not in ('RE','VO')"
					+ " and mi4.docstatus not in ('RE','VO') and mi.m_inoutline_id not in (?)", line.getC_OrderLine_ID(),line.getM_InOutLine_ID());			
			
			MOrderLine oline = new MOrderLine(line.getCtx(), line.getC_OrderLine_ID(), line.get_TrxName());
			
			BigDecimal qtyAvailable = oline.getQtyOrdered().subtract(MovementQty);
			
			if(qtyAvailable.compareTo(line.getMovementQty())<0) {
				throw new AdempiereException("Gagal Create Picklist!! Quantity Shipment melebihi Quantity Ordered pada SO"
						+ ", Quantity Ordered SO : "+ oline.getQtyOrdered()
						+ ", Quantity Available : "+ qtyAvailable
						+ ", Quantity Movement : "+ line.getMovementQty()
						+ ", SUM Quantity Movement : "+ MovementQty
						+ ", pada Shipment Line : "+line.getLine()
						+ ", Product : "+line.getM_Product().toString()
						+ ", Locator "+line.getM_Locator().getValue());
			}
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
