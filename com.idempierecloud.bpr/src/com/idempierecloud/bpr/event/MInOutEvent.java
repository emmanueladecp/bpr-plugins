package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutConfirm;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInOutLineConfirm;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MUOMConversion;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRPicklist;
import com.idempierecloud.bpr.model.MBPRPicklistLine;

public class MInOutEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	private MInOut inout = null;
	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Minout Event : "+event.getTopic());
		inout = (MInOut) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_PREPARE)) {
			checkQtySalesOrder();
			checkAvailableQtyProduct();
			checkCustomerReturn();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID)) {
			checkShipment();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSEACCRUAL)) {
			checkShipment();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			checkShipment();
		}
	}
	private void checkCustomerReturn() {
		if(!inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerReturns) || !inout.get_ValueAsBoolean("IsSusut") || inout.isReversal())
			return;
		
		for(MInOutLine line : inout.getLines()) {
			if(!line.getM_Locator().getM_LocatorType().getName().equals("RETUR"))
				continue;
			
			BigDecimal qty = MUOMConversion.convertProductFrom(line.getCtx(), line.getM_Product_ID(), line.getC_UOM_ID(), line.getQtyEntered());
			BigDecimal timbanganNetAmt = (BigDecimal) line.get_Value("TimbanganNetAmt");
			if(timbanganNetAmt==null || timbanganNetAmt.signum()<=0)
				throw new AdempiereException("TimbanganNetAmt invalid. "+timbanganNetAmt);
			BigDecimal diff = qty.subtract(timbanganNetAmt);
			if(diff.signum()<=0)
				return;
			
			MInOutLine susut = new MInOutLine(inout);
			susut.setM_Product_ID(line.getM_Product_ID());
			susut.setQty(diff);
			susut.setC_UOM_ID(line.getM_Product().getC_UOM_ID());
			int LocatorSusut_ID = DB.getSQLValue(null, "SELECT M_Locator_ID From M_Locator Where M_Locator.M_LocatorType_ID = 1000000 And M_Locator.M_Warehouse_ID=?", inout.getM_Warehouse_ID());
			susut.setM_Locator_ID(LocatorSusut_ID);
			susut.setLine(line.getLine()+20);
			susut.setC_OrderLine_ID(line.getC_OrderLine_ID());
			susut.setDescription("SUSUT");
			susut.saveEx();
		}
		
	}
	private void checkShipment() {
		int BPR_PiclistLine_ID = DB.getSQLValue(inout.get_TrxName(), "select bpr_picklistline_id from bpr_picklistline bp "
				+ " join bpr_picklist bp2 on bp2.bpr_picklist_id = bp.bpr_picklist_id "
				+ " where bp2.docstatus not in ('VO','RE') and bp.m_inout_id = ?", inout.getM_InOut_ID());
		if (BPR_PiclistLine_ID>0) {
			MBPRPicklistLine picklistLine= new MBPRPicklistLine(inout.getCtx(), BPR_PiclistLine_ID, inout.get_TrxName());
			MBPRPicklist picklist = (MBPRPicklist) picklistLine.getBPR_Picklist();
			throw new AdempiereException("GAGAL!! Shipment : "+inout.getDocumentNo()+" Sudah digunakan oleh Picklist : "+picklist.getDocumentNo());
		}
		for(MInOutLine line: inout.getLines(false)) {
			int C_InvoiceLine_ID = DB.getSQLValue(inout.get_TrxName(), "select c_invoiceline_id from c_invoiceline ci "
					+ " join c_invoice ci2 on ci2.c_invoice_id = ci.c_invoice_id where ci2.docstatus not in ('VO','RE')"
					+ " and ci.m_inoutline_id = ?", line.getM_InOutLine_ID());
			if(C_InvoiceLine_ID > 0) {
				MInvoiceLine iLine = new MInvoiceLine(line.getCtx(), C_InvoiceLine_ID, line.get_TrxName());
				MInvoice invoice = (MInvoice) iLine.getC_Invoice();
				if(inout.isSOTrx())
					throw new AdempiereException("Shipment : "+inout.getDocumentNo()+"Sudah digunakan Invoice : "+invoice.getDocumentNo());
				else
					throw new AdempiereException("Material receipt : "+inout.getDocumentNo()+"Sudah digunakan Invoice : "+invoice.getDocumentNo());
				
			}
		}
	}
	
	private void checkAvailableQtyProduct() {
		if(!inout.isSOTrx() || !inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerShipment))
			return;
		MInOutLine[] lines = inout.getLines(true);
		for (MInOutLine line : lines) {
			BigDecimal qtyonhand = DB.getSQLValueBD(inout.get_TrxName(), "SELECT COALESCE(SUM(QtyOnHand), 0)"
					+ "	FROM M_Storageonhand s"
					+ "	WHERE s.M_Product_ID=? AND s.m_locator_id=?", line.getM_Product_ID(),line.getM_Locator_ID());
			
			BigDecimal qtyIntransit = DB.getSQLValueBD(inout.get_TrxName(), "SELECT COALESCE(SUM(s.confirmedqty), 0)"
					+ "	FROM M_InOutLineConfirm s"
					+ "	JOIN M_InOutConfirm c ON s.M_InOutConfirm_ID=c.M_InOutConfirm_ID"
					+ "	JOIN M_InOutLine iol ON s.sM_InOutLine_ID=iol.M_InOutLine_ID"
					+ "	WHERE c.docstatus in ('DR','IP') AND iol.M_Product_ID=? AND iol.m_locator_id=? "
					+ " AND iol.M_inoutline_ID <> ?", line.getM_Product_ID(),line.getM_Locator_ID(),line.getM_InOutLine_ID());
			
			BigDecimal qtyAvailable = qtyonhand.subtract(qtyIntransit);
			
			if(qtyAvailable.compareTo(line.getMovementQty())<0)
				throw new AdempiereException("Gagal Complete!!"
						+", Quantity Available : "+qtyAvailable
						+", Quantity OnHand : "+qtyonhand
						+", Quantity Intransit : "+qtyIntransit
						+", Quantity Movement : "+line.getMovementQty()
						+", pada Shipment Line : "+line.getLine()
						+", Product : "+line.getM_Product().toString()
						+" Locator "+line.getM_Locator().getValue());
		}
	}
	
	private void checkQtySalesOrder() {
		if(!inout.isSOTrx() || !inout.getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerShipment))
			return;
		MInOutLine[] lines = inout.getLines(true);
		for (MInOutLine line : lines) {
			BigDecimal qtyOrderedSO = DB.getSQLValueBD(inout.get_TrxName(), "Select coalesce(co.qtyordered,0) from c_orderline co where co.c_orderline_id = ?", line.getC_OrderLine_ID());
			
			BigDecimal MovementQty = DB.getSQLValueBD(inout.get_TrxName(),"Select coalesce(sum(mi.movementqty),0) from m_inoutline mi "
					+ " join m_inout mi2 ON mi.m_inout_id = mi2.m_inout_id "
					+ " where mi2.docstatus not in ('VO','RE') and mi.c_orderline_id = ? and mi.m_inoutline_id <> ?", line.getC_OrderLine_ID(),line.getM_InOut_ID());
			
			BigDecimal qtyAvailable = qtyOrderedSO.subtract(MovementQty);
			
			if(qtyAvailable.compareTo(line.getMovementQty())<0) {
				throw new AdempiereException("Gagal Complete!! Quantity Movement melebihi Quantity Ordered pada SO"
						+ ", Quantity Ordered SO : "+ qtyOrderedSO
						+ ", Quantity Available : "+ qtyAvailable
						+ ", Quantity Movement : "+ line.getMovementQty()
						+ ", SUM Quantity Movement : "+ MovementQty
						+ ", pada Shipment Line : "+line.getLine()
						+ ", Product : "+line.getM_Product().toString()
						+ ", Locator "+line.getM_Locator().getValue());
			}else {
				throw new AdempiereException("Gagal Complete!!");
			}
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
