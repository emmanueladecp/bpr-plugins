package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

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
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_PREPARE)) {
			setCreditUseBP();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REACTIVATE)) {
			resetQtyReserved();
			checkshipment();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID)) {
			resetQtyReserved();
			updatePOReference();
			resetCreditUsed();
			checkshipment();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			resetQtyReserved();
			updatePOReference();
		}
	}
	
	private void checkshipment() {
        if(order.isSOTrx()&& order.getC_DocTypeTarget().getDocSubTypeSO()!=null&&order.getC_DocTypeTarget().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_OnCreditOrder)) {
        	return;
        }else {
        	MOrderLine[] lines = order.getLines();
    		for(MOrderLine line:lines) {
    			int m_inout_id = DB.getSQLValue(line.get_TrxName(), "select mi2.m_inout_id from m_inoutline mi "
    					+ " join m_inout mi2 on mi.m_inout_id = mi2.m_inout_id "
    					+ " where mi2.docstatus not in ('RE','VO') and mi.c_orderline_id = ?", line.getC_OrderLine_ID());
    			MInOut shipment = new MInOut(line.getCtx(), m_inout_id, line.get_TrxName());
    			if(m_inout_id > 0) {
    				throw new AdempiereException("Order ini telah digunakan Shipment/Receipt : "+shipment.getDocumentNo());
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
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
