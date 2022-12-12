package com.idempierecloud.bpr.model;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.compiere.model.MInvoice;
import org.compiere.model.Query;

public class MBPRListFakturPajak extends X_BPR_ListFakturPajak {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3794968571348189584L;

	public MBPRListFakturPajak(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public MBPRListFakturPajak(Properties ctx, int BPR_ListFakturPajak_ID, String trxName) {
		super(ctx, BPR_ListFakturPajak_ID, trxName);
	}

	public static MBPRListFakturPajak getNext(MInvoice invoice) {
		SimpleDateFormat format = new SimpleDateFormat("Y");
		String where = "isactive = 'Y' and fiscalyear =?"
				+ " and not exists (select 1 from bpr_historyfakturpajak bh where bpr_listfakturpajak.bpr_listfakturpajak_id=bh.bpr_listfakturpajak_id)";
		MBPRListFakturPajak pajak = new Query(invoice.getCtx(), MBPRListFakturPajak.Table_Name, where, invoice.get_TrxName())
				.setParameters(format.format(invoice.getDateAcct()))
				.setOrderBy("name")
				.first();
		return pajak;
	}


}
