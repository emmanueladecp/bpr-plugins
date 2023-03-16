package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class MInventoryLineEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(MInventoryLineEvent.class);
	
	private MInventoryLine inventoryLine = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("inventory line Event : "+event.getTopic());
		
		inventoryLine = (MInventoryLine) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			checkNewCurrentPrice();
			setQtyCount();
			checkQtyAdd();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			checkNewCurrentPrice();
			setQtyCount();
			checkQtyAdd();
		}
	}
	
	/*Ticket #request-001268 [BPR] Tambahkan validasi pada QtyAdd*/
	private void checkQtyAdd() {
		if(inventoryLine.getM_Inventory().getC_DocType_ID()==1000105) {
			BigDecimal QtyAdd = (BigDecimal)inventoryLine.get_Value("QtyAdd");
			if(QtyAdd.signum()<0) {
				throw new AdempiereException("Quantity Add tidak boleh minus");
			}else if (QtyAdd.signum()>0) {
				BigDecimal check =inventoryLine.getQtyInternalUse().subtract(QtyAdd);
				if(check.signum()<0) {
					inventoryLine.setQtyInternalUse(QtyAdd.negate());
				}
			}
		}
	}

	private void checkNewCurrentPrice() {
		MInventory inventory = (MInventory) inventoryLine.getM_Inventory();
		if(!inventory.get_ValueAsBoolean("isUpdateCosting"))
			return;
		
		if(inventoryLine.getNewCostPrice().signum()==0)
			throw new AdempiereException("Inventory line "+inventoryLine.getLine()+" new current cost price cannot be 0.");
	}

	private void setQtyCount() {
		if(inventoryLine.isProcessed() || inventoryLine.getQtyCount().signum()!=0)
			return;
		
		if(inventoryLine.getQtyCsv().signum()!=0) {
			inventoryLine.setQtyCount(inventoryLine.getQtyBook().add(inventoryLine.getQtyCsv()));
		}
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
