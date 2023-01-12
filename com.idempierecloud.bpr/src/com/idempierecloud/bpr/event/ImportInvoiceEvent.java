package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MInvoice;
import org.compiere.model.MPayment;
import org.compiere.model.PO;
import org.compiere.model.X_I_Invoice;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class ImportInvoiceEvent extends CustomEvent {

private static CLogger log = CLogger.getCLogger(MPayment.class);
	
	private X_I_Invoice importInvoice = null;
	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Import Invoice Event : "+event.getTopic());
		
		importInvoice = (X_I_Invoice) po;
		if (event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)) {
			setTaxNo();
		}
	}
	
	private void setTaxNo() {
		if(!importInvoice.is_ValueChanged("C_Invoice_ID"))
			return;
		
		if(importInvoice.getC_Invoice_ID()>0 && !importInvoice.get_ValueAsString("tax_no").isEmpty()) {
			MInvoice invoice = (MInvoice) importInvoice.getC_Invoice();
			invoice.set_ValueOfColumn("tax_no", importInvoice.get_Value("tax_no"));
			invoice.saveEx();
		}
	}

	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub

	}

}
