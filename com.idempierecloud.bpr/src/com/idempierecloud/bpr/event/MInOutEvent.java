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
		log.fine("Minout Confirm Event : "+event.getTopic());
		inout = (MInOut) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			checkAvailableQtyProduct();
		}
	}
	private void checkAvailableQtyProduct() {
		if(inout.isSOTrx()) {
			MInOutLine[] lines = inout.getLines(true);
			for (MInOutLine line : lines) {
				BigDecimal qtyAvailable = DB.getSQLValueBD(inout.get_TrxName(), "select bomQtyAvailable(?,?,?) AS QtyAvailable "
						+ " from M_Product p "
						+ " LEFT OUTER JOIN M_ProductPrice pr ON (p.M_Product_ID=pr.M_Product_ID AND pr.IsActive='Y')"
						+ " LEFT OUTER JOIN M_AttributeSet pa ON (p.M_AttributeSet_ID=pa.M_AttributeSet_ID)"
						+ " LEFT OUTER JOIN M_Product_PO ppo ON (p.M_Product_ID=ppo.M_Product_ID AND ppo.IsCurrentVendor='Y' AND ppo.IsActive='Y')"
						+ " LEFT OUTER JOIN C_BPartner bp ON (ppo.C_BPartner_ID=bp.C_BPartner_ID)"
						+ " WHERE p.M_Product_ID=?", line.getM_Product_ID(),inout.getM_Warehouse_ID(),line.getM_Locator_ID(),line.getM_Product_ID());
				if(qtyAvailable.compareTo(line.getMovementQty())<0)
					throw new AdempiereException("Gagal Complete!! Quantity Avaibility = "+qtyAvailable+", Quantity Movement = "+line.getMovementQty()+", pada Shipment Line : "+line.getLine()+", Product : "+line.getM_Product().getValue()+"_"+line.getM_Product().getName());
			}
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
