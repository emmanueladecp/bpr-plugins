package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.util.List;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MOrder;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class MRequisitionEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(MRequisitionEvent.class);
	
	private MRequisition req = null;

	
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Requisition Event : "+event.getTopic());
		
		req = (MRequisition) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			checkTimbanganPO();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			checkTimbanganPO();
		}else if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			checkTimbanganNetAmt();
		}else if(event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)) {
			checkTimbanganNetAmt();
		}
	}

	private void checkTimbanganPO() {
		if(req.get_ValueAsInt("BPR_Timbangan_ID")==0)
			return;
		
		MRequisition anotherReq = new Query(req.getCtx(), MRequisition.Table_Name, "BPR_Timbangan_ID=? AND M_Requisition_ID<>?", req.get_TrxName())
				.setParameters(req.get_ValueAsInt("BPR_Timbangan_ID"), req.getM_Requisition_ID())
				.first();
				
		if(anotherReq!=null)
			throw new AdempiereException("Timbangan sudah digunakan di req "+anotherReq.getDocumentNo());
		
	}

	private void checkTimbanganNetAmt() {
		if(req.get_ValueAsInt("timbanganNetAmt")==0)
			return;
		
		BigDecimal timbanganNetAmt = (BigDecimal) req.get_Value("timbanganNetAmt");
		BigDecimal totalQtyPack = DB.getSQLValueBD(req.get_TrxName(), "SELECT COALESCE(SUM(qtyPack),0) FROM M_RequisitionLine WHERE M_Requisition_ID=?", req.getM_Requisition_ID());
		List<MRequisitionLine> lines = new Query(req.getCtx(), MRequisitionLine.Table_Name, MRequisitionLine.COLUMNNAME_M_Requisition_ID+"=?", req.get_TrxName())
				.setParameters(req.getM_Requisition_ID())
				.list();
		
		for(MRequisitionLine line : lines) {
			BigDecimal qtyPack = (BigDecimal) line.get_Value("qtyPack");
			if(qtyPack==null)
					qtyPack= Env.ZERO;
			
			BigDecimal newQtyOrdered = qtyPack
					.divide(totalQtyPack)
					.multiply(timbanganNetAmt);
			line.setQty(newQtyOrdered);
			line.saveEx();
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub

	}

}
