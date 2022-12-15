package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
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
			checkTimbanganPO();
			checkSalesRep();
			setCreditAvailable();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			checkTimbanganPO();
			checkTimbanganNetAmt();
			checkSalesRep();
			setCreditAvailable();
		}
	}
	private void setCreditAvailable() {
		if(order.isSOTrx()) {
			if(order.getC_BPartner_ID()>0) {
				BigDecimal SO_CreditAvaiable = order.getC_BPartner().getSO_CreditLimit().subtract(order.getC_BPartner().getSO_CreditUsed());
				order.set_ValueOfColumn("SO_CreditAvailable", SO_CreditAvaiable);			}
		}
	}
	
	private void checkSalesRep() {
		if(order.get_ValueAsInt("SalesRep_ID2")>0)
			order.setSalesRep_ID(order.get_ValueAsInt("SalesRep_ID2"));
	}

	private void checkTimbanganPO() {
		if(order.isSOTrx() || order.get_ValueAsInt("BPR_Timbangan_ID")==0)
			return;
		
		MOrder anotherOrder = new Query(order.getCtx(), MOrder.Table_Name, "BPR_Timbangan_ID=? AND C_Order_ID<>?", order.get_TrxName())
				.setParameters(order.get_ValueAsInt("BPR_Timbangan_ID"), order.getC_Order_ID())
				.first();
				
		if(anotherOrder!=null)
			throw new AdempiereException("Timbangan sudah digunakan di Order "+anotherOrder.getDocumentNo());
		
	}

	private void checkTimbanganNetAmt() {
		if(order.isSOTrx() || order.get_ValueAsInt("timbanganNetAmt")==0)
			return;
		
		BigDecimal timbanganNetAmt = (BigDecimal) order.get_Value("timbanganNetAmt");
		BigDecimal totalQtyEntered = DB.getSQLValueBD(order.get_TrxName(), "SELECT COALESCE(SUM(QtyEntered),0) FROM C_OrderLine WHERE C_Order_ID=?", order.getC_Order_ID());
		for(MOrderLine line : order.getLines()) {
			BigDecimal newQtyOrdered = line.getQtyEntered().subtract(
					line.getQtyEntered()
					.divide(totalQtyEntered, 2, RoundingMode.HALF_UP)
					.multiply(totalQtyEntered.subtract(timbanganNetAmt))
				);
			line.setQtyOrdered(newQtyOrdered);
			line.saveEx();
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
