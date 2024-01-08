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
import org.compiere.model.MMatchPO;
import org.compiere.model.MOrderLine;
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
		else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_REVERSECORRECT)) {
			checkFaktur();
		}	
		else if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			checkMovementDate();
			checkqtyShipment();
			checkDocStatusShipment();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {


		}
		else if(event.getTopic().equals(IEventTopics.DOC_AFTER_COMPLETE)) {
			checkProductType();
		}
	}
	
	private void checkProductType() {
		if(!invoice.isSOTrx()) {
			for(MInvoiceLine invoiceLine: invoice.getLines()) {
				 StringBuilder sql = new StringBuilder ("select mm.m_matchpo_id from c_invoiceline ci "
				 		+ "	join m_matchpo mm on ci.c_invoiceline_id = mm.c_invoiceline_id "
				 		+ "	where mm.c_orderline_id = ? and ci.c_invoiceline_id =?");
					PreparedStatement pstmnt = null;
					ResultSet rsl = null;
					try
					{
						pstmnt = DB.prepareStatement (sql.toString(), invoice.get_TrxName());
						int index = 1; 
			            pstmnt.setInt(index++, invoiceLine.getC_OrderLine_ID());
			            pstmnt.setInt(index++, invoiceLine.getC_InvoiceLine_ID());
						rsl = pstmnt.executeQuery ();
						while (rsl.next ()){
							MMatchPO mpo = new MMatchPO(invoiceLine.getCtx(), rsl.getInt(1), invoiceLine.get_TrxName());							
							if(!mpo.getC_OrderLine().getC_Order().isSOTrx()) {
								if(!mpo.getM_Product().getProductType().equals("I")) {
									mpo.setRef_MatchPO_ID(mpo.get_ID());
									mpo.saveEx();
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
	}
	
	private void setDoctype(String event) {
		//untuk create line from invoice #request-001261U
		if(event.equals(IEventTopics.PO_BEFORE_CHANGE)) {
			if(invoice.is_ValueChanged("C_DocTypeTarget_ID")) {
				invoice.setC_DocType_ID(invoice.getC_DocTypeTarget_ID());
			}
		}else if(event.equals(IEventTopics.PO_BEFORE_NEW)) {
			invoice.setC_DocType_ID(invoice.getC_DocTypeTarget_ID());
		}
		
	}

	private void setCreditUsed() {
		MBPartner bpartner = new MBPartner(invoice.getCtx(), invoice.getC_BPartner_ID(), invoice.get_TrxName());
		BigDecimal creditUsed = DB.getSQLValueBD(invoice.get_TrxName(), "SELECT calculate_credituse(?)", bpartner.getC_BPartner_ID());            
		bpartner.setSO_CreditUsed(creditUsed);
		bpartner.saveEx();
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

	
	@Override
	protected void doHandleEvent() {
		
	}

}
