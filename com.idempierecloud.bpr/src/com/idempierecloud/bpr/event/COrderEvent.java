package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;
import org.compiere.model.MDocType;

import com.idempierecloud.bpr.base.CustomEvent;

public class COrderEvent extends CustomEvent{

	private static CLogger log = CLogger.getCLogger(COrderLineEvent.class);
	
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
		}
	}
	
	private void setPotongKarung() {
		MDocType dt = (MDocType) order.getC_DocType();
		if(!order.isSOTrx()&&dt.get_ValueAsBoolean("IsTurus")) {
			int c_charge_id_potongKarung = 1000139;
			int lineNO = DB.getSQLValue(order.get_TrxName(),"select max(line) from c_orderline co where C_Order_ID=?", order.getC_Order_ID());
			BigDecimal biayaPotongKarung = DB.getSQLValueBD(order.get_TrxName(), "select coalesce(sum(co.QtyPack),0)*coalesce (max(co.priceentered),0) *0.12 "
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
			line.setPriceEntered(biayaPotongKarung.negate());
			line.setPriceActual(biayaPotongKarung.negate());
			line.setC_Tax_ID(1000000);//Bebas PPN
			line.set_ValueOfColumn("LCO_WithholdingType_ID", 1000005);//Non Pph
			line.setC_UOM_ID(100);//Each
			line.save();			
		}
		
	}

	private void checkshipment() {
        if(order.getC_DocTypeTarget().getDocSubTypeSO()!=null&&order.getC_DocTypeTarget().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_OnCreditOrder)) {
        	return;
        }else {
        	MOrderLine[] lines = order.getLines();
    		for(MOrderLine line:lines) {
    			int m_inout_id = DB.getSQLValue(line.get_TrxName(), "select mi2.m_inout_id from m_inoutline mi "
    					+ " join m_inout mi2 on mi.m_inout_id = mi2.m_inout_id "
    					+ " where mi2.docstatus not in ('RE','VO') and mi.c_orderline_id = ?", line.getC_OrderLine_ID());
    			MInOut shipment = new MInOut(line.getCtx(), m_inout_id, line.get_TrxName());
    			if(m_inout_id > 0) {
					String msg = order.isSOTrx()?"Sales":"Purchase"+" Order Line "+line.getLine()+" sudah digunakan. Silahkan void/reverse correct "+
								(order.isSOTrx()?"Shipment":"Material Receipt")+" : "+shipment.getDocumentNo();
					throw new AdempiereException(msg);
				}
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
		if(order.getC_DocTypeTarget().getDocSubTypeSO()!=null&&order.getC_DocTypeTarget().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_OnCreditOrder)) {
			List<MInvoice> invoices = new Query(order.getCtx(), MInvoice.Table_Name, "C_Order_ID=?", order.get_TrxName())
					.setParameters(order.getC_Order_ID())
					.list();
			for(MInvoice invoice : invoices){
				if(invoice.processIt(DocAction.ACTION_Reverse_Correct))
					invoice.saveEx();
				else
					return "Failed cancel invoice "+invoice.toString();
			}
			
			List<MInOut> shipments = new Query(order.getCtx(), MInOut.Table_Name, "C_Order_ID=?", order.get_TrxName())
					.setParameters(order.getC_Order_ID())
					.list();
			for(MInOut shipment : shipments){
				if(shipment.processIt(DocAction.ACTION_Reverse_Correct))
					shipment.saveEx();
				else
					return "Failed cancel shipment "+shipment.toString();
			}
		}
		return null;
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
