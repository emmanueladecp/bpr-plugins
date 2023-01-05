package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
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
			checkCreditAvailable();
			checkPOReference();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			checkSalesRep();
			checkCreditAvailable();
			checkPOReference();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REACTIVATE)) {
			resetQtyReserved();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID)) {
			resetQtyReserved();
			updatePOReference();
		}else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			resetQtyReserved();
			updatePOReference();
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
		
		MOrder reference = new Query(order.getCtx(), MOrder.Table_Name, "C_Order_ID<>? && POReference=?", order.get_TrxName())
				.setParameters(order.getC_Order_ID(), order.getPOReference())
				.first();
		
		if(order!=null)
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
