package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRPicklist;
import com.idempierecloud.bpr.model.MBPRPicklistLine;

public class MBPRPicklistEvent  extends CustomEvent{
	
	private static CLogger log = CLogger.getCLogger(MBPRPicklist.class);
	private MBPRPicklist picklist = null;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Event : "+event.getTopic());
		picklist = (MBPRPicklist) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			checkQtySalesOrder();
		}
	}
	private void checkQtySalesOrder() {
		for (MBPRPicklistLine line : picklist.getLines()) {
			BigDecimal MovementQtyShipment = DB.getSQLValueBD(picklist.get_TrxName(), "Select coalesce(sum(bp2.movementqty),0) "
					+ " from bpr_picklist bp"
					+ " join bpr_picklistline bp2 on bp.bpr_picklist_id = bp2.bpr_picklist_id "
					+ " join m_inout mi2 on mi2.m_inout_id = bp2.m_inout_id "
					+ " join m_inoutline mi ON mi.m_inout_id = mi2.m_inout_id "
					+ " join c_order co on co.c_order_id = mi2.c_order_id "
					+ " join c_orderline co2 on co2.c_order_id = co.c_order_id "
					+ " where co.docstatus in ('CO') and mi2.docstatus not in ('VO','RE') and bp.docstatus not in ('VO','RE')"
					+ " and mi2.c_order_id = ? and co2.m_product_id = ?", 
					line.getM_InOut().getC_Order_ID(),line.getM_Product_ID());		
			
			BigDecimal QtyOrderedSO = DB.getSQLValueBD(picklist.get_TrxName(), "Select coalesce(co2.qtyordered,0)"
					+ "	from bpr_picklist bp"
					+ "	join bpr_picklistline bp2 on bp.bpr_picklist_id = bp2.bpr_picklist_id "
					+ "	join m_inout mi2 on mi2.m_inout_id = bp2.m_inout_id "
					+ "	join m_inoutline mi ON mi.m_inout_id = mi2.m_inout_id "
					+ "	join c_order co on co.c_order_id = mi2.c_order_id "
					+ "	join c_orderline co2 on co2.c_order_id = co.c_order_id "
					+ "	where co.docstatus in ('CO') and mi2.c_order_id = ?"
					+ "	and co2.m_product_id = ? and bp.docstatus not in ('VO','RE')"
					+ "	group by co2.qtyordered, co.c_order_id", line.getM_InOut().getC_Order_ID(),line.getM_Product_ID());
			
			BigDecimal qtyAvailable = QtyOrderedSO.subtract(MovementQtyShipment);
			
			if(qtyAvailable.setScale(2).compareTo(BigDecimal.ZERO.setScale(2))<0) {
				throw new AdempiereException("Gagal Complete Picklist!! Quantity Movement melebihi Quantity Ordered pada SO"
						+ ", Quantity Ordered SO : "+ QtyOrderedSO
						+ ", Quantity Available : "+ qtyAvailable
						+ ", Quantity Movement : "+ MovementQtyShipment
						+ ", pada Piklist Line : "+line.getLineNo()
						+ ", Product : "+line.getM_Product().toString());
			}
		}
	}
	@Override
	protected void doHandleEvent() {
		
		
	}

}
