package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MDocType;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.MUOMConversion;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
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
			setLocator();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			setLocator();
			setLocatorTurus();
			setLocatorByOrder();
		}
	}
	
	
	private void setLocatorTurus() {
		if(line.getC_OrderLine_ID()==0 || line.getC_OrderLine().getC_Order().isSOTrx())
			return;
		
		MOrderLine orderLine = (MOrderLine) line.getC_OrderLine();
		if(orderLine.get_ValueAsInt("M_Locator_ID")>0)
			line.setM_Locator_ID(orderLine.get_ValueAsInt("M_Locator_ID"));
	}


	private void setLocatorByOrder() {
		if(line.getC_OrderLine_ID()==0)
			return;
		
		MOrderLine orderLine = (MOrderLine) line.getC_OrderLine();
		
		
		if(!orderLine.getC_Order().getC_DocType().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_OnCreditOrder))
			return;
		
		if(orderLine.get_ValueAsInt("M_Locator_ID")>0)
			line.setM_Locator_ID(orderLine.get_ValueAsInt("M_Locator_ID"));
	}


	private void checkQtyEntered() {
		BigDecimal qtyEntered = line.getQtyEntered();
		
		if(line.getM_Product().getC_UOM_ID()!=line.getC_UOM_ID()) {
			qtyEntered = MUOMConversion.convertProductTo(line.getCtx(), line.getM_Product_ID(), line.getC_UOM_ID(), line.getMovementQty());
			if(line.getQtyEntered().compareTo(qtyEntered)!=0)
				line.setQtyEntered(qtyEntered);
		}
	}
	
	private void setLocator() {
		int DocType_MM_Customer_Reject = 1000063;
		int DocType_MM_Customer_Return = 1000015;
		int M_LocatorType_ID = 0;
		if(line.getM_InOut().getC_DocType_ID()== DocType_MM_Customer_Reject) {
			M_LocatorType_ID = 1000006; // REJECT	
		}else if(line.getM_InOut().getC_DocType_ID()== DocType_MM_Customer_Return) {
			M_LocatorType_ID = 1000004;	// RETUR
		}
		if(line.getM_Locator().getM_LocatorType_ID()==1000006 || 
				line.getM_Locator().getM_LocatorType_ID()==1000004) {
			return;
		}
		
		if(M_LocatorType_ID==0 || (line.getDescription()!=null && line.getDescription().equals("SUSUT")))
			return;
		
		int locator = DB.getSQLValue(line.get_TrxName(), "SELECT M_Locator_ID From M_Locator Where M_LocatorType_ID = ? And M_Warehouse_ID=?", M_LocatorType_ID, line.getM_InOut().getM_Warehouse_ID());
		line.setM_Locator_ID(locator);
		
		
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
