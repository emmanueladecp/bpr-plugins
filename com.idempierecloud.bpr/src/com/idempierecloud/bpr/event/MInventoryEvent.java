package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MCost;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.PO;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class MInventoryEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(MInventoryEvent.class);
	
	private MInventory inventory = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("inventory Event : "+event.getTopic());
		
		inventory = (MInventory) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			checkLines();
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			createCostAdjustment();
		}
	}

	private void checkLines() {
		if(!inventory.get_ValueAsBoolean("isUpdateCosting"))
			return;
		
		for(MInventoryLine line : inventory.getLines(true)) {
			if(line.getNewCostPrice().signum()==0)
				throw new AdempiereException("Inventory line "+line.getLine()+" new current cost price cannot be 0.");
		}
	}

	private void createCostAdjustment() {
		if(!inventory.get_ValueAsBoolean("isUpdateCosting"))
			return;
		
		MInventory costingDoc = new MInventory(inventory.getCtx(), 0, inventory.get_TrxName());
		int DOCTYPE_COST_ADJUSTMENT = DB.getSQLValue(inventory.get_TrxName(), "SELECT C_DocType_ID FROM C_DocType WHERE Name='Cost Adjustment' AND AD_Client_ID=?", inventory.getAD_Client_ID());
		costingDoc.setC_DocType_ID(DOCTYPE_COST_ADJUSTMENT);
		costingDoc.setCostingMethod(MCost.COSTINGMETHOD_AveragePO);
		costingDoc.setAD_Org_ID(inventory.getAD_Org_ID());
		costingDoc.setDocAction(DocAction.ACTION_Complete);
		costingDoc.saveEx();
		
		for(MInventoryLine line : inventory.getLines(true)) {
			MInventoryLine costingLine = new MInventoryLine(line.getCtx(), 0, line.get_TrxName());
			costingLine.setM_Inventory_ID(costingDoc.getM_Inventory_ID());
			costingLine.setM_Product_ID(line.getM_Product_ID());
			costingLine.setCurrentCostPrice(line.getCurrentCostPrice());
			costingLine.setNewCostPrice(line.getNewCostPrice());
			costingLine.setM_Locator_ID(0);
			costingLine.setAD_Org_ID(line.getAD_Org_ID());
			costingLine.setM_AttributeSetInstance_ID(line.getM_AttributeSetInstance_ID());
			costingLine.saveEx(); 
			
			line.setDescription(line.getDescription()+costingLine.getM_InventoryLine_ID());
			line.saveEx();
		}
		
		if(!costingDoc.processIt(MInventory.ACTION_Complete))
			throw new AdempiereException("Failed create Cost Adjustment "+costingDoc.getProcessMsg());
		costingDoc.saveEx();
		
		inventory.setDescription(inventory.getDescription()+", Cost Adjusment "+costingDoc.getDocumentNo());
		inventory.saveEx();
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
