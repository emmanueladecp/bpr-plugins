package com.idempierecloud.bpr.event;

import java.math.BigDecimal;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRHistoryFakturPajak;
import com.idempierecloud.bpr.model.MBPRListFakturPajak;

public class CInvoiceEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	
	private MInvoice invoice = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("invoice Event : "+event.getTopic());
		
		invoice = (MInvoice) po;
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_VOID))
			checkFaktur();
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			checkFaktur();
		}	
		else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			setFaktur();
			checkDocStatusShipment();
		}
	}

	private void checkFaktur() {
		if(invoice.get_ValueAsInt("BPR_ListFakturPajak_ID")==0)
			return;
		
		int history = DB.getSQLValue(invoice.get_TrxName(), "SELECT BPR_HistoryFakturPajak_ID FROM BPR_HistoryFakturPajak WHERE BPR_ListFakturPajak_ID=? AND C_Invoice_ID=? AND isUploaded='Y'", invoice.get_ValueAsInt("BPR_ListFakturPajak_ID"), invoice.getC_Invoice_ID());
		if(history>0)
			throw new AdempiereException("Faktur Pajak telah diupload. Invoice tidak bisa dibatalkan");
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
	private void setFaktur() {
		if(!invoice.isSOTrx() || invoice.get_ValueAsString("TypePajak")==null || invoice.get_ValueAsInt("BPR_ListFakturPajak_ID")>0)
			return;
		
		MBPRListFakturPajak pajak = MBPRListFakturPajak.getNext(invoice);
		if(pajak==null)
			throw new AdempiereException("Tidak ada nomor faktur pajak yang tersedia");
		
		
		MBPRHistoryFakturPajak history = MBPRHistoryFakturPajak.addHistory(invoice, pajak);

		invoice.set_ValueOfColumn(MBPRListFakturPajak.COLUMNNAME_BPR_ListFakturPajak_ID, pajak.getBPR_ListFakturPajak_ID());
		invoice.set_ValueOfColumn(MBPRHistoryFakturPajak.COLUMNNAME_BPR_HistoryFakturPajak_ID, history.getBPR_HistoryFakturPajak_ID());
		invoice.set_ValueOfColumn("tax_no", history.getDescription());
		invoice.saveEx();
	}

	
	@Override
	protected void doHandleEvent() {
		
	}

}
