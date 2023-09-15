package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.List;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.MInOutLine;
import org.compiere.model.MLocator;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.MMovementLineMA;
import org.compiere.model.MProduct;
import org.compiere.model.MStorageOnHand;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRMaterialRequestLine;

public class MMovementEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(MMovementEvent.class);
	
	private MMovement movement = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("movement Event : "+event.getTopic());
		
		movement = (MMovement) po;
		String desc = movement.getC_DocType().getDescription();
		
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			checkProductCost();
			if(desc!=null && desc.equals("CONFIRM")) {
				if(movement.getReversal_ID()==0) {
					checkMovementLine();
					checkMovementLineSusut();
					checkMaterialMove();
					setMaterialDate();
				}else {
					setMaterialDate();
				}
				
			}
			checkReversal();
			checkAvailableQtyProduct();
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			if(desc!=null && desc.equals("INTRANSIT"))
				createMovementConfirm();
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_VOID)) {
			checkMovementRequest();
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_REVERSECORRECT)) {
			checkMovementRequest();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSEACCRUAL)) {
			if(desc!=null && desc.equals("INTRANSIT")) {
				checkConfirmMovement();
			}
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			if(desc!=null && desc.equals("INTRANSIT")) {
				checkConfirmMovement();
			}
		}
	}
	
	private void checkProductCost() {
		int M_CostElement_ID_AveragePO=1000004;
		for(MMovementLine line : movement.getLines(true)) {
			BigDecimal MCost_CurrentCostPrice = DB.getSQLValueBD(line.get_TrxName(), "SELECT Coalesce(M_Cost.currentcostprice,0) FROM M_Cost WHERE AD_Org_ID = ? "+
			 " and M_Product_ID = ? and M_CostElement_ID=?",movement.getAD_Org_ID(),line.getM_Product_ID(), M_CostElement_ID_AveragePO);
			
			if(MCost_CurrentCostPrice == null) {
				throw new AdempiereException("Tidak ditemukan Cost untuk Product : "+line.getM_Product().getName()
						+", Organization :  "+line.getAD_Org_ID()
						+", Cost Elemet : Average PO");
			}
			else if(MCost_CurrentCostPrice.signum()<=0) {
					throw new AdempiereException("Cost untuk Product : "+line.getM_Product().getName()
							+", Organization :  "+line.getAD_Org_ID()
							+", Cost Elemet : Average PO, Current Cost Price Harus Lebih Besar dari 0");			
			}
		}
	}

	private void checkConfirmMovement() {
		List<MMovement> confirms = new Query(movement.getCtx(), MMovement.Table_Name, "MoveReference = ?", movement.get_TrxName())
				.setParameters(movement.getDocumentNo())
				.list();
		for(MMovement confirm : confirms) {
			if(confirm.getDocStatus().equals(MMovement.STATUS_Completed)) {
				throw new AdempiereException("Tidak dapat Reverse Material move. Confirm Move sudah di Complete");
			}
		}
		
	}

	private void checkMaterialMove() {
		if(movement.get_ValueAsString("MoveReference").equalsIgnoreCase(null))
			return;
		List<MMovement> materials = new Query(movement.getCtx(), MMovement.Table_Name, "DocumentNo = ?", movement.get_TrxName())
				.setParameters(movement.get_ValueAsString("MoveReference"))
				.list();
		for(MMovement material : materials) {
			if(material.getDocStatus().equals(MMovement.STATUS_Reversed)) {
				throw new AdempiereException("Tidak dapat Complete Confirm Move. Material Move sudah di Reverse");
			}
		}
			
	}

	private void checkReversal() {
		if(movement.getReversal_ID()==0)
			return;
		
		for(MMovementLine line : movement.getLines(false)) {
			int no = MMovementLineMA.deleteMovementLineMA(line.getM_MovementLine_ID(), movement.get_TrxName());
			if (no > 0)
				if (log.isLoggable(Level.CONFIG)) log.config("Delete old #" + no);
		
			if (line.getM_AttributeSetInstance_ID() == 0)
			{
							
				MProduct product = MProduct.get(movement.getCtx(), line.getM_Product_ID());
				String MMPolicy = product.getMMPolicy();
				MStorageOnHand[] storages = MStorageOnHand.getWarehouse(movement.getCtx(), 0, line.getM_Product_ID(), 0, 
						null, MClient.MMPOLICY_FiFo.equals(MMPolicy), true, line.getM_LocatorTo_ID(), movement.get_TrxName());
	
				BigDecimal qtyToDeliver = line.getMovementQty().abs();
	
				for (MStorageOnHand storage: storages)
				{
					if (storage.getQtyOnHand().compareTo(qtyToDeliver) >= 0)
					{
						MMovementLineMA ma = new MMovementLineMA (line, 
								storage.getM_AttributeSetInstance_ID(),
								qtyToDeliver.negate(),storage.getDateMaterialPolicy(),true);
						ma.saveEx();		
						qtyToDeliver = Env.ZERO;
						if (log.isLoggable(Level.FINE)) log.fine( ma + ", QtyToDeliver=" + qtyToDeliver);		
					}
					else
					{	
						MMovementLineMA ma = new MMovementLineMA (line, 
									storage.getM_AttributeSetInstance_ID(),
									storage.getQtyOnHand().negate(),storage.getDateMaterialPolicy(),true);
						ma.saveEx();	
						qtyToDeliver = qtyToDeliver.subtract(storage.getQtyOnHand());
						if (log.isLoggable(Level.FINE)) log.fine( ma + ", QtyToDeliver=" + qtyToDeliver);		
					}
					if (qtyToDeliver.signum() == 0)
						break;
				}
								
				//	No AttributeSetInstance found for remainder
				if (qtyToDeliver.signum() != 0)
				{
					MMovementLineMA ma = MMovementLineMA.addOrCreate(line, 0, qtyToDeliver, movement.getMovementDate(),true) ;
					ma.saveEx();
					if (log.isLoggable(Level.FINE)) log.fine("##: " + ma);
					
				}
			}	//	attributeSetInstance
		}
	}

	private void checkMovementRequest() {
		for(MMovementLine line : movement.getLines(false)) {
			MBPRMaterialRequestLine requestLine = new Query(movement.getCtx(), MBPRMaterialRequestLine.Table_Name, "M_MovementLine_ID=?", movement.get_TrxName())
					.setParameters(line.getM_MovementLine_ID())
					.first();
			if(requestLine==null)
				return;
			
			requestLine.setM_MovementLine_ID(0);
			requestLine.saveEx();
		}
	}

	private void createMovementConfirm() {
		if(movement.getReversal_ID()>0)
			return;
		
		int DocType_MovementConfirm = DB.getSQLValue(movement.get_TrxName(), "SELECT C_DocType_ID FROM C_DocType WHERE AD_Client_ID=? AND DocBaseType='MMM' AND Description='CONFIRM'", movement.getAD_Client_ID());
		if(DocType_MovementConfirm==0)
			throw new AdempiereException("No Document Type Confirm Movement");
		
		MMovement movementConfirm = new MMovement(movement.getCtx(), 0, movement.get_TrxName());
		movementConfirm.setAD_Org_ID(movement.getM_WarehouseTo().getAD_Org_ID());
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
		MClient client = new MClient(movement.getCtx(), movement.getAD_Client_ID(), movement.get_TrxName());
		if(client.getName().equals("Belitang")) {
			MLocator locatorSusut = null;
			
			for(MMovementLine line : movement.getLines(true)) {
				if(line.getTargetQty().compareTo(line.getMovementQty())==0)
					continue;
				String percentage = DB.getSQLValueString(line.get_TrxName(), "SELECT Value FROM AD_SysConfig WHERE name like 'SusutMovement'");
				BigDecimal num = new BigDecimal(percentage);
				BigDecimal qtySusut = line.getTargetQty().subtract(line.getMovementQty());
				BigDecimal penentu = (line.getTargetQty().multiply(num)).divide(BigDecimal.valueOf(100));
				
				if(qtySusut.compareTo(penentu)>0) {
					String sqlWhere = "M_LocatorType_ID IN (SELECT M_LocatorType_ID FROM M_LocatorType lt WHERE lt.isSusut='Y' and lt.name like '%BA')"
									  + " AND M_Warehouse_ID=?";
					locatorSusut = new Query(movement.getCtx(), MLocator.Table_Name, sqlWhere, movement.get_TrxName())
							.setParameters(movement.getM_Warehouse_ID())
							.first();
					
					if(locatorSusut==null)
						throw new AdempiereException("No Locator Susut BA for Warehouse "+movement.getM_Warehouse().getName());
				}else if(qtySusut.compareTo(penentu)<=0) {
					String sqlWhere = "M_LocatorType_ID IN (SELECT M_LocatorType_ID FROM M_LocatorType lt WHERE lt.isSusut='Y' and lt.name not like '%BA')"
							  + " AND M_Warehouse_ID=?";
					locatorSusut = new Query(movement.getCtx(), MLocator.Table_Name, sqlWhere, movement.get_TrxName())
							.setParameters(movement.getM_Warehouse_ID())
							.first();
					
					if(locatorSusut==null)
						throw new AdempiereException("No Locator Susut for Warehouse "+movement.getM_Warehouse().getName());
				}
				if(locatorSusut==null)
					throw new AdempiereException("No Locator Susut for Warehouse "+movement.getM_Warehouse().getName());
				
				if(line.getM_LocatorTo_ID()==locatorSusut.getM_Locator_ID())
					continue;
				
				createMovementLineSusut(line, locatorSusut);
			}
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
		lineSusut.set_ValueOfColumn("M_LocatorAlias_ID", lineSusut.get_Value("M_LocatorAlias_ID"));
		lineSusut.saveEx();
	}
	
	private void checkAvailableQtyProduct() {
		for(MMovementLine line : movement.getLines(true)) {		
			BigDecimal qtyonhand = DB.getSQLValueBD(line.get_TrxName(), "SELECT COALESCE(SUM(QtyOnHand), 0) FROM M_Storageonhand s"
					+ "	WHERE s.M_Product_ID=? AND s.m_locator_id=?", line.getM_Product_ID(),line.getM_Locator_ID());
			
			BigDecimal qtyIntransit = DB.getSQLValueBD(line.get_TrxName(), "SELECT COALESCE(SUM(s.confirmedqty), 0)"
					+ "	FROM M_InOutLineConfirm s"
					+ "	JOIN M_InOutConfirm c ON s.M_InOutConfirm_ID=c.M_InOutConfirm_ID"
					+ "	JOIN M_InOutLine iol ON s.M_InOutLine_ID=iol.M_InOutLine_ID"
					+ "	WHERE c.docstatus in ('DR','IP','IN') AND iol.M_Product_ID=? AND iol.m_locator_id=?"
					, line.getM_Product_ID(),line.getM_Locator_ID());
			
			BigDecimal qtyAvailable = qtyonhand.subtract(qtyIntransit).subtract(line.getMovementQty());
			if(qtyAvailable.signum()<0)
				throw new AdempiereException("Gagal Complete!! Quantity Avaibility = "+qtyAvailable+", Quantity Intransit Shipment = "+qtyIntransit+", Quantity Movement = "+line.getMovementQty()+", pada Movement Line : "+line.getLine()+", Product : "+line.getM_Product().getValue()+"_"+line.getM_Product().getName()+" locator "+line.getM_Locator().getValue());		
		}
	}


	private void setMaterialDate() {
		movement.setMovementDate(new Timestamp(System.currentTimeMillis()));
	}
	
	@Override
	protected void doHandleEvent() {
		
	}

}
