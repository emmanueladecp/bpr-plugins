package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MLocator;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class MMovementEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(MMovementEvent.class);
	
	private MMovement movement = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("movement Event : "+event.getTopic());
		
		movement = (MMovement) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			checkMovementLine();
		}if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			if(movement.getC_DocType().getDescription().equals("INTRANSIT"))
				createMovementConfirm();
			else if(movement.getC_DocType().getDescription().equals("CONFIRM"))
				checkMovementLineSusut();
		}
	}

	private void createMovementConfirm() {
		int DocType_MovementConfirm = DB.getSQLValue(movement.get_TrxName(), "SELECT C_DocType_ID FROM C_DocType WHERE AD_Client_ID=? AND DocBaseType='MMM' AND Description='CONFIRM'", movement.getAD_Client_ID());
		if(DocType_MovementConfirm==0)
			throw new AdempiereException("No Document Type Confirm Movement");
		
		MMovement movementConfirm = new MMovement(movement.getCtx(), 0, movement.get_TrxName());
		movementConfirm.setAD_Org_ID(movement.getAD_Org_ID());
		movementConfirm.set_ValueOfColumn("moveReference", movement.getDocumentNo());
		movementConfirm.setC_DocType_ID(DocType_MovementConfirm);
		movementConfirm.setM_Warehouse_ID(movement.getM_Warehouse_ID());
		movementConfirm.setM_WarehouseTo_ID(movement.getM_WarehouseTo_ID());
		movementConfirm.setMovementDate(movement.getMovementDate());
		movementConfirm.setIsInTransit(true);
		movementConfirm.setDocAction(MMovement.ACTION_Complete);
		movementConfirm.saveEx();
		
		for(MMovementLine line : movement.getLines(false)) {
			MMovementLine confirmLine = new MMovementLine(movementConfirm);
			confirmLine.setM_Product_ID(line.getM_Product_ID());
			confirmLine.setM_Locator_ID(line.getM_LocatorTo_ID());
			confirmLine.set_ValueOfColumn("M_LocatorAlias_ID", line.getM_Locator_ID());
			confirmLine.setM_LocatorTo_ID(line.get_ValueAsInt("M_LocatorToAlias_ID"));
			confirmLine.setTargetQty(line.getTargetQty());
			confirmLine.setMovementQty(line.getMovementQty());
			confirmLine.saveEx();
		}
		
		movement.setDescription("Confirm Movement "+movementConfirm.getDocumentNo());
		movement.saveEx();
	}

	private void checkMovementLine() {
		for(MMovementLine line : movement.getLines(true)) {
			if(line.getConfirmedQty().compareTo(line.getMovementQty())>0)
				throw new AdempiereException("Confirmed Qty over that Movement Qty."+line.toString());
		}
	}

	private void checkMovementLineSusut() {
		if(!movement.getC_DocType().getDescription().equals("CONFIRM"))
			return;
		
		MLocator locatorSusut = null;
		
		for(MMovementLine line : movement.getLines(true)) {
			if(line.getConfirmedQty().compareTo(line.getMovementQty())==0)
				continue;
			
			MMovement moveReference = new Query(line.getCtx(), MMovement.Table_Name, "DocumentNo=?", line.get_TrxName())
					.setParameters(movement.get_ValueAsString("MoveReference"))
					.first();
			
			if(moveReference==null)
				throw new AdempiereException("Move Reference not found "+movement.get_ValueAsInt("MoveReference"));
			
			if(locatorSusut==null) {
				String sqlWhere = "M_LocatorType_ID IN (SELECT M_LocatorType_ID FROM M_LocatorType lt WHERE lt.isSusut='Y')"
								  + " AND M_Warehouse_ID=?";
				locatorSusut = new Query(movement.getCtx(), MLocator.Table_Name, sqlWhere, movement.get_TrxName())
						.setParameters(movement.getM_Warehouse_ID())
						.first();
				
				if(locatorSusut==null)
					throw new AdempiereException("No Locator Susut for Warehouse "+movement.getM_Warehouse().getName());
			}
			
			createMovementSusut(moveReference, line, locatorSusut);
		}
		
		
	}

	private void createMovementSusut(MMovement moveReference, MMovementLine line, MLocator locatorSusut) {

		int DocType_MovementSusut = DB.getSQLValue(movement.get_TrxName(), "SELECT C_DocType_ID FROM C_DocType WHERE AD_Client_ID=? AND DocBaseType='MMM' AND Description='SUSUT'", movement.getAD_Client_ID());
		if(DocType_MovementSusut==0)
			throw new AdempiereException("No Document Type Material Susut");
		
		MMovement movementSusut = new MMovement(line.getCtx(), 0, line.get_TrxName());
		movementSusut.setAD_Org_ID(line.getAD_Org_ID());
		movementSusut.set_ValueOfColumn("moveReference",moveReference.getDocumentNo());
		movementSusut.setDescription("Movement Susut "+moveReference.getDocumentNo());
		movementSusut.setC_DocType_ID(DocType_MovementSusut);
		movementSusut.setM_Warehouse_ID(moveReference.getM_Warehouse_ID());
		movementSusut.setM_WarehouseTo_ID(moveReference.getM_Warehouse_ID());
		movementSusut.setMovementDate(moveReference.getMovementDate());
		movementSusut.setDocAction(MMovement.ACTION_Complete);
		movementSusut.saveEx();
		
		MMovementLine lineSusut = new MMovementLine(movementSusut);
		lineSusut.setM_Locator_ID(line.getM_Locator_ID());
		lineSusut.setM_LocatorTo_ID(locatorSusut.getM_Locator_ID());
		lineSusut.setMovementQty(line.getMovementQty().subtract(line.getConfirmedQty()));
		lineSusut.setM_Product_ID(line.getM_Product_ID());
		lineSusut.saveEx();
		
		if(!movementSusut.processIt(MMovement.ACTION_Complete)) {
			throw new AdempiereException("Failed Complete Movement Susut "+movementSusut.getProcessMsg());
		}
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
