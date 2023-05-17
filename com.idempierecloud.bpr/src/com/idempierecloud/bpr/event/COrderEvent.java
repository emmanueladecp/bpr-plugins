package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MOrderPaySchedule;
import org.compiere.model.MSysConfig;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.osgi.service.event.Event;
import org.compiere.model.MDocType;

import com.idempierecloud.bpr.base.CustomEvent;

public class COrderEvent extends CustomEvent{

	private static CLogger log = CLogger.getCLogger(COrderLineEvent.class);
	String m_processMsg = null;
	private MOrder order = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Order Event : "+event.getTopic());
		
		order = (MOrder) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			checkSalesRep();
			setCreditAvailable();
			checkPOReference();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			checkCreditAvailable();
			checkPOReference();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			setPriceCost();
			setPotongKarung();
			setInsentif();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_PREPARE)) {
			setCreditUseBP();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REACTIVATE)) {
			resetMStorageReservation();
			resetQtyReserved();
			checkCreditOrder();
			checkshipment();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID)) {
			resetMStorageReservation();
			resetQtyReserved();
			updatePOReference();
			resetCreditUsed();
			checkCreditOrder();
			checkshipment();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			resetQtyReserved();
			updatePOReference();
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			checkWarehouseOrder();
		}else if(event.getTopic().equals(IEventTopics.DOC_AFTER_CLOSE)) {
			checkCreditUsedSOClose();
		}
		
	}
	
	private void checkCreditUsedSOClose() {
		if(order.isSOTrx()) {
			 BigDecimal outstandingCreditUsed = DB.getSQLValueBD(order.get_TrxName(), "with ship as (select co2.c_orderline_id, sum(ci.qtyinvoiced) as qtyinvoiced "
			 		+ " from c_orderline co2 "
			 		+ " left join m_inoutline mi on mi.c_orderline_id = co2.c_orderline_id "
			 		+ " join m_inout mi2 on mi.m_inout_id =mi2.m_inout_id "
			 		+ " left join c_invoiceline ci ON ci.m_inoutline_id = mi.m_inoutline_id  "
			 		+ " join c_invoice ci2 on ci.c_invoice_id =ci2.c_invoice_id "
			 		+ " where ci2.docstatus = 'CO' and mi2.docstatus = 'CO' "
			 		+ " group by co2.c_orderline_id ) "
			 		+ " SELECT sum((co.qtyordered - coalesce(ship.qtyinvoiced,0))*co.priceentered )as creditUseBack "
			 		+ " FROM c_orderline co "
			 		+ " left join ship on ship.c_orderline_id = co.c_orderline_id "
			 		+ " where co.c_order_id = ?", order.getC_Order_ID());
			 	MBPartner bp = (MBPartner) order.getC_BPartner();
				BigDecimal creditUsed = bp.getSO_CreditUsed().add(outstandingCreditUsed);
				bp.setSO_CreditUsed(creditUsed);
				bp.saveEx();
		}
	}

	private void setInsentif() {		
		MDocType docType = (MDocType) order.getC_DocTypeTarget();
		if(!docType.get_ValueAsBoolean("isTurus"))
			return;
		for(MOrderLine line:order.getLines()) {
			if(line.getM_Product_ID()==1003383||line.get_ValueAsInt("relatedproduct_ID")==1003383) {//GABAH 64 BELITANG KERING SUPPLIER
				StringBuffer sqlStmt = new StringBuffer();
			    sqlStmt.append(" select c_order_id,m_product_id,percetase from adempiere.bpr_insentif_v where c_order_id=?");
		 
			    PreparedStatement pstmt = null;
			    ResultSet rs = null;	    
			    try{
			    	pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			    	int index = 1;
			    	pstmt.setInt(index++, order.getC_Order_ID());
			    	
				    rs = pstmt.executeQuery();
				    while (rs.next()){
				    	BigDecimal percentage = rs.getBigDecimal("percetase");
				    	if(percentage.compareTo(BigDecimal.valueOf(60))>=0) {
				    		line.set_ValueOfColumn("IsInsentif", true);
							line.saveEx();
				    	}
				    }		    
			    }catch(Exception e){
			    	log.log(Level.SEVERE, sqlStmt.toString());
			    }finally{
			    	DB.close(rs, pstmt);
			    	pstmt = null;
			    	rs = null;
			    }
			}
		}
	}
	
    private void setPotongKarung() {
        MDocType dt = (MDocType) order.getC_DocType();
        if(!order.isSOTrx()&&dt.get_ValueAsBoolean("IsTurus")&&order.get_ValueAsInt("AD_Org_ID")==1000003) {//BPR1
            int c_charge_id_potongKarung = 1000139;
            int lineNO = DB.getSQLValue(order.get_TrxName(),"select max(line)+10 from c_orderline co where C_Order_ID=?", order.getC_Order_ID());
            BigDecimal biayaPotongKarung = DB.getSQLValueBD(order.get_TrxName(), "select coalesce(sum(co.QtyPack),0)*coalesce (max(co.pricenet),0) *0.12 "
                    + " from c_orderline co "
                    + " join c_order co2 on co.c_order_id =co2.c_order_id "
                    + " join c_doctype cd on co2.c_doctypetarget_id = cd.c_doctype_id"
                    + " where co2.issotrx = 'N' and cd.isturus = 'Y' and co.c_order_id = ?", order.getC_Order_ID());
            MOrderLine line = new MOrderLine(order.getCtx(), 0, order.get_TrxName());
            line.setAD_Org_ID(order.getAD_Org_ID());
            line.setC_Order_ID(order.getC_Order_ID());
            line.setLine(lineNO);
            line.setC_BPartner_ID(order.getC_BPartner_ID());
            line.setC_BPartner_Location_ID(order.getC_BPartner_Location_ID());
            line.setDatePromised(order.getDatePromised());
            line.setDateOrdered(order.getDateOrdered());
            line.setC_Charge_ID(c_charge_id_potongKarung);
            line.setQtyEntered(BigDecimal.ONE);
            line.setQtyOrdered(BigDecimal.ONE);
            line.set_ValueOfColumn("PriceNet", biayaPotongKarung.negate());
            line.setPrice(biayaPotongKarung.negate());
            line.setC_Tax_ID(1000000);//Bebas PPN
            line.set_ValueOfColumn("LCO_WithholdingType_ID", 1000005);//Non Pph
            line.setC_UOM_ID(100);//Each
            line.save();            
        } 
    }

	private void checkshipment() {
      	MOrderLine[] lines = order.getLines();
   		for(MOrderLine line:lines) {
   			List<MInOutLine> mlines = new Query(line.getCtx(), MInOutLine.Table_Name, " Exists (select M_Inout_ID from M_inout where M_inout.m_inout_id = m_inoutline.m_inout_id "
   					+ " and m_inout.docstatus not in ('VO','RE')) and m_inoutline.c_orderline_id = ?", line.get_TrxName())
					.setClient_ID().setOnlyActiveRecords(true)
					.setParameters(line.getC_OrderLine_ID())
					.list();
   			for(MInOutLine mline : mlines){
   				MInOut shipment = new MInOut(mline.getCtx(), mline.getM_InOut_ID(), mline.get_TrxName());
   	   			String msg = (order.isSOTrx()?"Sales":"Purchase")+" Order Line "+line.getLine()+" sudah digunakan. Silahkan void/reverse correct "+
   							(order.isSOTrx()?"Shipment":"Material Receipt")+" : "+shipment.getDocumentNo()+" line : "+mline.getLine();
   				throw new AdempiereException(msg);
   			}
   			
   		}
    }

	private void resetCreditUsed() {
		if(order.isSOTrx()) {
			MBPartner bp = (MBPartner) order.getC_BPartner();
			BigDecimal creditUsed = bp.getSO_CreditUsed().subtract(order.getGrandTotal());
			bp.setSO_CreditUsed(creditUsed);
			bp.saveEx();	
			order.set_ValueOfColumn("isdone", false);
		}
	}

	private void setCreditUseBP() {
		if(order.isSOTrx()) {
			if(!order.get_ValueAsBoolean("isdone")) {
				MBPartner bp = (MBPartner) order.getC_BPartner();
				BigDecimal creditUsed = bp.getSO_CreditUsed().add(order.getGrandTotal());
				bp.setSO_CreditUsed(creditUsed);
				bp.saveEx();
			}
			order.set_ValueOfColumn("isdone", true);
		}
	}

	/**
	 * Purchase Order Price Cost
	 */
    private void setPriceCost() {
		if(order.isSOTrx())
			return ;
		
		for(MOrderLine line : order.getLines()) {
			if(line.get_ValueAsBoolean("isGrossUpPPN") && line.getC_Tax_ID()>0) {
				BigDecimal rate = line.getC_Tax().getRate();
				BigDecimal priceCost = line.getPriceEntered().add(line.getPriceEntered().multiply(rate.divide(Env.ONEHUNDRED, line.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP)));
				line.setPriceCost(priceCost);
				line.saveEx();
			}
		}
	}

	private void setCreditAvailable() {
        if(order.isSOTrx()) {//if sales order
            if(order.getC_BPartner_ID()>0) {
                if(order.getC_BPartner_ID()!=order.get_ValueAsInt("C_BPartnerSR_ID")) {//if document new or c_bpartner_id is change
                    BigDecimal SO_CreditAvailable = this.getBPCreditAvailable();
                    order.set_ValueOfColumn("SO_CreditAvailable", SO_CreditAvailable);
                    order.set_ValueOfColumn("C_BPartnerSR_ID", order.getC_BPartner_ID());
                }
            }
        }
    }
	
	private void updatePOReference() {
		if(!order.isSOTrx() || order.getPOReference()==null || order.getPOReference().isEmpty())
			return;
		
		order.setPOReference(order.getPOReference()+"**");
		order.saveEx();
	}

	/**
	 * Sales Order PO Reference Unique
	 */
	private void checkPOReference() {
		if(!order.isSOTrx() || order.getPOReference()==null || order.getPOReference().isEmpty() || order.getPOReference().endsWith("**"))
			return;
		
		MOrder reference = new Query(order.getCtx(), MOrder.Table_Name, "C_Order_ID<>? AND POReference=?", order.get_TrxName())
				.setParameters(order.getC_Order_ID(), order.getPOReference())
				.first();
		
		if(reference!=null)
			throw new AdempiereException("Duplikat PO Reference : "+reference.getDocumentNo());
	}

	private void resetMStorageReservation() {
		MDocType dt = (MDocType) order.getC_DocTypeTarget();
		if(order.isSOTrx() && !dt.get_ValueAsBoolean("IsRetur")) {
			for(MOrderLine line : order.getLines()) {
				if(line.getQtyReserved().setScale(0).compareTo(BigDecimal.ZERO)>0) {
					
					final String sqli = "DELETE FROM M_Storagereservationlog WHERE DocumentNo =? and M_Product_ID=? AND M_Warehouse_ID=? AND M_AttributeSetInstance_ID=? AND IsSOTrx=?";
					DB.executeUpdateEx(sqli, new Object[] {order.getDocumentNo(), line.getM_Product_ID(), 
							line.getM_Warehouse_ID(), line.getM_AttributeSetInstance_ID(), order.isSOTrx()}, line.get_TrxName());
					
					final String sql = "UPDATE M_StorageReservation SET Qty=Qty-?, Updated=getDate(), UpdatedBy=? " +
							"WHERE M_Product_ID=? AND M_Warehouse_ID=? AND M_AttributeSetInstance_ID=? AND IsSOTrx=?";
					DB.executeUpdateEx(sql, new Object[] {line.getQtyReserved(), Env.getAD_User_ID(Env.getCtx()), line.getM_Product_ID(), 
							line.getM_Warehouse_ID(), line.getM_AttributeSetInstance_ID(), order.isSOTrx()}, line.get_TrxName());
				}
			}
		}
	}
	
	private void resetQtyReserved() {
		for(MOrderLine line : order.getLines()) {
			line.setQtyReserved(Env.ZERO);
			line.saveEx();
		}
	}
	
	private void checkCreditAvailable() {
		if(order.is_ValueChanged("SO_CreditAvailable")) {
			BigDecimal BPCreditAvailable = this.getBPCreditAvailable();
			BigDecimal amtApproval = DB.getSQLValueBD(order.get_TrxName(), "SELECT COALESCE(AmtApproval,0) FROM AD_Role WHERE AD_Role_ID=?", Env.getAD_Role_ID(Env.getCtx()));
			BigDecimal SO_CreditAvailable = (BigDecimal) order.get_Value("SO_CreditAvailable");
			if(SO_CreditAvailable==null)
				SO_CreditAvailable = Env.ZERO;
			
			if(SO_CreditAvailable.compareTo(BPCreditAvailable)>0 && SO_CreditAvailable.compareTo(amtApproval)>0)
				throw new AdempiereException("Maks SO_CreditAvailable for current Role is "+amtApproval);
		}
	}
	
	private BigDecimal getBPCreditAvailable() {
		return order.getC_BPartner().getSO_CreditLimit().subtract(order.getC_BPartner().getSO_CreditUsed());
	}
	private void checkSalesRep() {
		if(order.get_ValueAsInt("SalesRep_ID2")>0)
			order.setSalesRep_ID(order.get_ValueAsInt("SalesRep_ID2"));
	}
	
	private String checkCreditOrder() {
		if(order.getC_DocTypeTarget().getDocSubTypeSO()!=null&&order.getC_DocTypeTarget().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_OnCreditOrder)
				||order.getC_DocTypeTarget().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_WarehouseOrder)) {
			List<MInvoice> invoices = new Query(order.getCtx(), MInvoice.Table_Name, "C_Order_ID=?", order.get_TrxName())
					.setParameters(order.getC_Order_ID())
					.list();
			if(order.getC_DocTypeTarget().getDocSubTypeSO()!=null&&order.getC_DocTypeTarget().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_OnCreditOrder)) {
				for(MInvoice invoice : invoices){
					if(!invoice.getDocStatus().equals(MOrder.DOCSTATUS_Completed)) {
						throw new AdempiereException("Please Check Status Invoice");
					}
					if(invoice.processIt(DocAction.ACTION_Reverse_Correct)) {
						MInvoice reversal = (MInvoice) invoice.getReversal();
						if(!reversal.getDocStatus().equalsIgnoreCase("RE")&&!invoice.getDocStatus().equalsIgnoreCase("RE")) {
		                   String msg = (order.isSOTrx()?"Invoice (Customer)":"Invoice (Vendor)")+" : "+invoice.getDocumentNo()+" Gagal Reverse!!";
		                   throw new AdempiereException(msg);
						}
						invoice.saveEx();
					}
					else
						throw new AdempiereException( "Failed cancel invoice "+invoice.toString());
				}
			}else if (order.getC_DocTypeTarget().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_WarehouseOrder)) {
				for(MInvoice invoice : invoices){
					if(invoice.getDocStatus().equals(MOrder.DOCSTATUS_Voided)||invoice.getDocStatus().equals(MOrder.DOCSTATUS_Reversed)) {
						continue;
					}else if(invoice.getDocStatus().equals(MOrder.DOCSTATUS_Drafted)) {
						if(invoice.processIt(DocAction.ACTION_Void)) {
							if(!invoice.getDocStatus().equals(MInvoice.DOCSTATUS_Voided)) {
			                   String msg = (order.isSOTrx()?"Invoice (Customer)":"Invoice (Vendor)")+" : "+invoice.getDocumentNo()+" Gagal Void!!";
			                   throw new AdempiereException(msg);
							}
							invoice.saveEx();
						}else
							throw new AdempiereException( "Failed cancel invoice "+invoice.toString());
					}else if(invoice.getDocStatus().equals(MOrder.DOCSTATUS_Completed)) {
						if(invoice.processIt(DocAction.ACTION_Reverse_Correct)) {
							MInvoice reversal = (MInvoice) invoice.getReversal();
							if(!reversal.getDocStatus().equalsIgnoreCase("RE")&&!invoice.getDocStatus().equalsIgnoreCase("RE")) {
			                   String msg = (order.isSOTrx()?"Invoice (Customer)":"Invoice (Vendor)")+" : "+invoice.getDocumentNo()+" Gagal Reverse!!";
			                   throw new AdempiereException(msg);
							}
							invoice.saveEx();
						}else
							throw new AdempiereException( "Failed cancel invoice "+invoice.toString());
					}else {
						throw new AdempiereException("Please Check Invoice :"+invoice.getDocumentNo()+", status invoice : "+invoice.getDocStatus());
					}
				}
			}
			
			
			List<MInOut> shipments = new Query(order.getCtx(), MInOut.Table_Name, "C_Order_ID=?", order.get_TrxName())
					.setParameters(order.getC_Order_ID())
					.list();
			for(MInOut shipment : shipments){
				if(shipment.processIt(DocAction.ACTION_Reverse_Correct)) {
					MInOut reversal = (MInOut) shipment.getReversal();
					if(!reversal.getDocStatus().equalsIgnoreCase("RE")&&!shipment.getDocStatus().equalsIgnoreCase("RE")) {
	                   String msg = (order.isSOTrx()?"Shipment":"Material Receipt")+" : "+shipment.getDocumentNo()+" Gagal Reverse!!";
	                   throw new AdempiereException(msg);
					}
					shipment.saveEx();
				}
				else {
					throw new AdempiereException("Failed cancel shipment "+shipment.toString());
				}
			}
		}
		return null;
	}
	
	private String checkWarehouseOrder() {
		boolean realTimePOS = MSysConfig.getBooleanValue(MSysConfig.REAL_TIME_POS, false , order.getAD_Client_ID());
		int M_InOut_ID = DB.getSQLValue(order.get_TrxName(), "select M_InOut_ID from M_InOut where C_Order_ID = ?", order.getC_Order_ID());
		MInOut io = new MInOut(order.getCtx(), M_InOut_ID, order.get_TrxName());
		if ( MDocType.DOCSUBTYPESO_WarehouseOrder.equals(order.getC_DocType().getDocSubTypeSO())){
				MInvoice invoice = createInvoice ((MDocType) order.getC_DocType(), io, realTimePOS ? null : order.getDateOrdered());
				if (invoice == null)
					return DocAction.STATUS_Invalid;
			}	//	Invoice
		return m_processMsg;
	}
	
	protected MInvoice createInvoice (MDocType dt, MInOut shipment, Timestamp invoiceDate)
	{
		if (log.isLoggable(Level.INFO)) log.info(dt.toString());
		MInvoice invoice = new MInvoice (order, dt.getC_DocTypeInvoice_ID(), invoiceDate);
		if (!invoice.save(order.get_TrxName()))
		{
			m_processMsg = "Could not create Invoice";
			return null;
		}
		
		//	If we have a Shipment - use that as a base
		if (shipment != null)
		{
			if (!MOrder.INVOICERULE_AfterDelivery.equals(order.getInvoiceRule()))
				order.setInvoiceRule(MOrder.INVOICERULE_AfterDelivery);
			//
			MInOutLine[] sLines = shipment.getLines(false);
			for (int i = 0; i < sLines.length; i++)
			{
				MInOutLine sLine = sLines[i];
				//
				MInvoiceLine iLine = new MInvoiceLine(invoice);
				iLine.setShipLine(sLine);
				//	Qty = Delivered	
				if (sLine.sameOrderLineUOM())
					iLine.setQtyEntered(sLine.getQtyEntered());
				else
					iLine.setQtyEntered(sLine.getMovementQty());
				iLine.setQtyInvoiced(sLine.getMovementQty());
				if (!iLine.save(order.get_TrxName()))
				{
					m_processMsg = "Could not create Invoice Line from Shipment Line";
					return null;
				}
				//
				sLine.setIsInvoiced(true);
				if (!sLine.save(order.get_TrxName()))
				{
					log.warning("Could not update Shipment line: " + sLine);
				}
			}
		}
		else	//	Create Invoice from Order
		{
			if (!MOrder.INVOICERULE_Immediate.equals(order.getInvoiceRule()))
				order.setInvoiceRule(MOrder.INVOICERULE_Immediate);
			//
			MOrderLine[] oLines = order.getLines();
			for (int i = 0; i < oLines.length; i++)
			{
				MOrderLine oLine = oLines[i];
				//
				MInvoiceLine iLine = new MInvoiceLine(invoice);
				iLine.setOrderLine(oLine);
				//	Qty = Ordered - Invoiced	
				iLine.setQtyInvoiced(oLine.getQtyOrdered().subtract(oLine.getQtyInvoiced()));
				if (oLine.getQtyOrdered().compareTo(oLine.getQtyEntered()) == 0)
					iLine.setQtyEntered(iLine.getQtyInvoiced());
				else
					iLine.setQtyEntered(iLine.getQtyInvoiced().multiply(oLine.getQtyEntered())
						.divide(oLine.getQtyOrdered(), 12, RoundingMode.HALF_UP));
				if (!iLine.save(order.get_TrxName()))
				{
					m_processMsg = "Could not create Invoice Line from Order Line";
					return null;
				}
			}
		}
		
		// Copy payment schedule from order to invoice if any
		for (MOrderPaySchedule ops : MOrderPaySchedule.getOrderPaySchedule(order.getCtx(), order.getC_Order_ID(), 0, order.get_TrxName())) {
			MInvoicePaySchedule ips = new MInvoicePaySchedule(order.getCtx(), 0, order.get_TrxName());
			PO.copyValues(ops, ips);
			ips.setC_Invoice_ID(invoice.getC_Invoice_ID());
			ips.setAD_Org_ID(ops.getAD_Org_ID());
			ips.setProcessing(ops.isProcessing());
			ips.setIsActive(ops.isActive());
			if (!ips.save()) {
				m_processMsg = "ERROR: creating pay schedule for invoice from : "+ ops.toString();
				return null;
			}
		}
		
		invoice.saveEx(order.get_TrxName());
		order.setC_CashLine_ID(invoice.getC_CashLine_ID());
		if (!MOrder.DOCSTATUS_Completed.equals(invoice.getDocStatus()))
		{
			m_processMsg = "@C_Invoice_ID@: " + invoice.getProcessMsg();
			return null;
		}
		return invoice;
	}	//	createInvoice
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
