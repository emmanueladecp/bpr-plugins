package com.idempierecloud.bpr.process;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.model.MBPRListFakturPajak;

public class GenerateNoFakturPajak extends CustomProcess {

	private String suffix;
	private int rangeTo;
	private int rangeFrom;
	private String year;

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("year")) {
				year = para[i].getParameterAsString();
			}else if (name.equals("suffix")) {
				suffix = para[i].getParameterAsString();
			}else if (name.equals("range")) {
				rangeFrom = para[i].getParameterAsInt();
				rangeTo = para[i].getParameter_ToAsInt();
			}
		}
	}			

	@Override
	protected String doIt() throws Exception {
		int count = 0;
		for(int i=rangeFrom;i<=rangeTo;i++) {
			String noFaktur = suffix+String.format("%04d", i);
			int BPR_ListFakturPajak_ID = DB.getSQLValue(get_TrxName(), "SELECT BPR_ListFakturPajak_ID FROM BPR_ListFakturPajak WHERE fiscalyear=? AND name=?", year, noFaktur);
			MBPRListFakturPajak faktur = new MBPRListFakturPajak(getCtx(), BPR_ListFakturPajak_ID, get_TrxName());
			faktur.setName(noFaktur);
			faktur.setFiscalYear(year);
			faktur.saveEx();
			addLog(faktur.getFiscalYear()+" - "+faktur.getName());
			count++;
		}
		return "Generated "+count;
	}

}
