package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOutLine;
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
		String desc = movement.getC_DocType().getDescription();
		
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			if(desc!=null && desc.equals("CONFIRM")) {
				checkMovementLine();
				checkMovementLineSusut();
			}
			checkAvailableQtyProduct();
		}if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			if(desc!=null && desc.equals("INTRANSIT"))
				createMovementConfirm();
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
			confirmLine.setTargetQty(line.getMovementQty());
			confirmLine.setMovementQty(line.getMovementQty());
			confirmLine.saveEx();
		}
		
		movement.setDescription("Confirm Movement "+movementConfirm.getDocumentNo());
		movement.saveEx();
	}

	private void checkMovementLine() {
		for(MMovementLine line : movement.getLines(true)) {
			if(line.getMovementQty().compareTo(line.getTargetQty())>0)
				throw new AdempiereException("Movement Qty over that Target Qty."+line.toString());
			
			BigDecimal thresholdQty = line.getTargetQty().multiply(BigDecimal.valueOf(0.05));
			if(line.getMovementQty().compareTo(thresholdQty)<0)
				throw new AdempiereException("Movement Qty must over than "+thresholdQty);
			
		}
	}

	private void checkMovementLineSusut() {
		if(!movement.getC_DocType().getDescription().equals("CONFIRM"))
			return;
		
		MLocator locatorSusut = null;
		
		for(MMovementLine line : movement.getLines(true)) {
			if(line.getTargetQty().compareTo(line.getMovementQty())==0)
				continue;
			
			if(locatorSusut==null) {
				String sqlWhere = "M_LocatorType_ID IN (SELECT M_LocatorType_ID FROM M_LocatorType lt WHERE lt.isSusut='Y')"
								  + " AND M_Warehouse_ID=?";
				locatorSusut = new Query(movement.getCtx(), MLocator.Table_Name, sqlWhere, movement.get_TrxName())
						.setParameters(movement.getM_Warehouse_ID())
						.first();
				
				if(locatorSusut==null)
					throw new AdempiereException("No Locator Susut for Warehouse "+movement.getM_Warehouse().getName());
			}
			
			if(line.getM_LocatorTo_ID()==locatorSusut.getM_Locator_ID())
				continue;
			
			createMovementLineSusut(line, locatorSusut);
		}
		
		
	}

	private void createMovementLineSusut(MMovementLine line, MLocator locatorSusut) {
		MMovementLine lineSusut = new Query(movement.getCtx(), MMovementLine.Table_Name, "M_Movement_ID=? AND M_Product_ID=? AND M_LocatorTo_ID=?", movement.get_TrxName())
				.setParameters(line.getM_Movement_ID(), line.getM_Product_ID(), locatorSusut.getM_Locator_ID())
				.firstOnly();
		
		if(lineSusut==null) {
			lineSusut = new MMovementLine(movement);
			lineSusut.setM_Locator_ID(line.getM_Locator_ID());
			lineSusut.setM_LocatorTo_ID(locatorSusut.getM_Locator_ID());
		}
		lineSusut.setMovementQty(line.getTargetQty().subtract(line.getMovementQty()));
		lineSusut.setTargetQty(line.getTargetQty());
		lineSusut.setM_Product_ID(line.getM_Product_ID());
		lineSusut.saveEx();
	}
	
	private void checkAvailableQtyProduct() {
		for(MMovementLine line : movement.getLines(true)) {			
			BigDecimal qtyAvailable = DB.getSQLValueBD(line.get_TrxName(), "SELECT COALESCE(SUM(QtyOnHand), 0)"
					+ "	FROM M_Storageonhand s"
					+ "	WHERE s.M_Product_ID=? AND s.m_locator_id=?", line.getM_Product_ID(),line.getM_Locator_ID());
			if(qtyAvailable.compareTo(line.getMovementQty())<0)
				throw new AdempiereException("Gagal Complete!! Quantity Avaibility = "+qtyAvailable+", Quantity Movement = "+line.getMovementQty()+", pada Movement Line : "+line.getLine()+", Product : "+line.getM_Product().getValue()+"_"+line.getM_Product().getName()+" locator "+line.getM_Locator().getValue());		
		}
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
