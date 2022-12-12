package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.MInvoice;

public class MBPRHistoryFakturPajak extends X_BPR_HistoryFakturPajak {

	/**
	 * 
	 */
	private static final long serialVersionUID = 724230471466599336L;

	public MBPRHistoryFakturPajak(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	public MBPRHistoryFakturPajak(Properties ctx, int BPR_HistoryFakturPajak_ID, String trxName) {
		super(ctx, BPR_HistoryFakturPajak_ID, trxName);
	}

	public static MBPRHistoryFakturPajak addHistory(MInvoice invoice, MBPRListFakturPajak pajak) {
		MBPRHistoryFakturPajak history = new MBPRHistoryFakturPajak(invoice.getCtx(), 0, invoice.get_TrxName());
		history.setC_Invoice_ID(invoice.getC_Invoice_ID());
		history.setBPR_HistoryFakturPajak_ID(pajak.getBPR_ListFakturPajak_ID());
		history.setDescription(invoice.get_ValueAsString("TypePajak")+"."+pajak.getName());
		history.saveEx();
		return history;
	}
}
