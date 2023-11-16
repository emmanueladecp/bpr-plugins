package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MBPRHistoryFakturPajak;
import com.idempierecloud.bpr.model.MBPRListFakturPajak;

public class CInvoiceEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CInvoiceEvent.class);
	
	private MInvoice invoice = null;
	private final static int M_LocatorType_CustomerShipment = 1000002;
	private final static int C_Doctype_AR_CreditMemo = 1000004;

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
			checkMovementDate();
			checkqtyShipment();
			setFaktur();
			checkDocStatusShipment();
		}
	}
	
	private void checkqtyShipment() {
		if(invoice.isSOTrx()) {
			for(MInvoiceLine invoiceLine : invoice.getLines(true)){
				if(invoiceLine.getM_InOutLine_ID()>0) {
					MInOutLine shipLine = (MInOutLine) invoiceLine.getM_InOutLine();
					if(shipLine.getMovementQty().compareTo(invoiceLine.getQtyInvoiced())!=0) {
						if(shipLine.getM_Locator().getM_LocatorType_ID()==M_LocatorType_CustomerShipment) {
							throw new AdempiereException("Qty Shipment tidak sama dengan Qty Invoice,"
									+ " Qty Shipment : "+shipLine.getQtyEntered()
									+ " Qty Invoice  : "+invoiceLine.getQtyEntered()
									+ " Product : "+invoiceLine.getM_Product().getName());
						}
					}	
				}
			}
		}
		
	}
	
	private void checkMovementDate() {
		if(invoice.isSOTrx()){
			if(invoice.getReversal_ID()>0)
				return;
			Date DateAcc = invoice.getDateAcct();
		    int monthInv = DateAcc.getMonth();
		    StringBuilder sql = new StringBuilder ("select distinct mi.m_inout_id from c_invoiceline ci "
		    		+ "	join c_invoice ci2 on ci.c_invoice_id = ci2.c_invoice_id  "
		    		+ "	left join m_inoutline mi on ci.m_inoutline_id = mi.m_inoutline_id "
		    		+ "	where ci2.issotrx = 'Y' and ci2.c_invoice_id = ? and mi.m_inout_id>0");
			PreparedStatement pstmnt = null;
			ResultSet rsl = null;
			try
			{
				pstmnt = DB.prepareStatement (sql.toString(), invoice.get_TrxName());
				int index = 1; 
	            pstmnt.setInt(index++, invoice.getC_Invoice_ID());
				rsl = pstmnt.executeQuery ();
				while (rsl.next ()){
					int inout =  rsl.getInt(1);
					if(inout>0) {
						MInOut shipment = new MInOut(invoice.getCtx(), rsl.getInt(1), invoice.get_TrxName());
						Date DateAcc2 = shipment.getDateAcct();
						int mountShp = DateAcc2.getMonth();
						if(mountShp!=monthInv) {
							throw new AdempiereException("Periode Invoice berbeda dengan Periode Shipment!");
						}
					}
				}
			}
			catch (SQLException e)
			{
				log.log(Level.SEVERE, " CInvoiceEvent - " + sql.toString(), e);
			}
			finally
			{
				DB.close(rsl, pstmnt);
				rsl = null;
				pstmnt = null;
			}
		    
		    
			
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
        if(!invoice.isSOTrx() || invoice.get_ValueAsString("TypePajak")==null || 
                invoice.get_ValueAsInt("BPR_ListFakturPajak_ID")>0 || invoice.getC_DocTypeTarget_ID() == C_Doctype_AR_CreditMemo)
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
