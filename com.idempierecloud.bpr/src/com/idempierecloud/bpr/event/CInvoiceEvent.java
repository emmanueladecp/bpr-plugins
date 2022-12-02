package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class CInvoiceEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	
	private MInvoice invoice = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("invoice Event : "+event.getTopic());
		
		invoice = (MInvoice) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID))
			checkFaktur();
		else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE))
			checkDocStatusShipment();
	}

	private void checkDocStatusShipment() {
		if(!invoice.get_ValueAsBoolean("isSOTrx"))
			return;
		MInvoiceLine[] lines = invoice.getLines();
		for(MInvoiceLine line:lines) {
			if(line.getM_InOutLine_ID()>0) {
				MInOut shipment = (MInOut) line.getM_InOutLine().getM_InOut();
				if(shipment.getDocStatus().equalsIgnoreCase("CO"))
					return;
				else if(shipment.getDocStatus().equalsIgnoreCase("CL"))
					return;
				else
					throw new AdempiereException("Shipment Document No : "+shipment.getDocumentNo()+" pada Invoice Line No "+ line.getLine()+" Belum complete!");
			}
		}
	}
	private void checkFaktur() {
		if(invoice.get_ValueAsInt("BPR_ListFakturPajak_ID")==0)
			return;
		
		String isUploaded = DB.getSQLValueString(invoice.get_TrxName(), "SELECT IsUploaded FROM BPR_ListFakturPajak WHERE BPR_ListFakturPajak_ID=?", invoice.get_ValueAsInt("BPR_ListFakturPajak_ID"));
		
		if(isUploaded!=null && isUploaded.equals("Y")) {
			throw new AdempiereException("Faktur Pajak already uploaded");
		}
		
		invoice.set_ValueOfColumn("BPR_ListFakturPajak_ID", null);
		invoice.saveEx();
	}
	
	@Override
	protected void doHandleEvent() {
		
	}

}
