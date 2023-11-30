package com.idempierecloud.bpr.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInvoice;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.model.MBPRHistoryFakturPajak;
import com.idempierecloud.bpr.model.MBPRListFakturPajak;

public class SetFakturPajakInvoice extends CustomProcess{

	
	String DocNoInv = null;
	int Count = 0;
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("DocumentNo"))
				DocNoInv = para[i].getParameterAsString();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {
		StringBuilder sql = new StringBuilder ("select ci.c_invoice_id, ci.documentno from c_invoice ci "
				+ "	where ci.issotrx = 'Y' and c_doctypetarget_id <> 1000004 and ci.docstatus in ('CO') "
				+ "	and ci.bpr_historyfakturpajak_id is null and ci.bpr_listfakturpajak_id is null and tax_no is null "
				+ "	and ad_client_id = 1000003");
        if(DocNoInv!= null) {
			sql.append(" and ci.documentno = ?");
		}
		
		PreparedStatement pstmnt = null;
		ResultSet rsl = null;
		try
		{
			pstmnt = DB.prepareStatement (sql.toString(), get_TrxName());
			if(!DocNoInv.equals(null))
				pstmnt.setString(1, DocNoInv);
			rsl = pstmnt.executeQuery ();
			while (rsl.next ()){
				MInvoice invoice = new MInvoice(getCtx(),rsl.getInt(1), get_TrxName());
				MBPRListFakturPajak pajak = MBPRListFakturPajak.getNext(invoice);
				if(pajak==null)
					throw new AdempiereException("Tidak ada nomor faktur pajak yang tersedia");
				
				
				MBPRHistoryFakturPajak history = MBPRHistoryFakturPajak.addHistory(invoice, pajak);

				invoice.set_ValueOfColumn(MBPRListFakturPajak.COLUMNNAME_BPR_ListFakturPajak_ID, pajak.getBPR_ListFakturPajak_ID());
				invoice.set_ValueOfColumn(MBPRHistoryFakturPajak.COLUMNNAME_BPR_HistoryFakturPajak_ID, history.getBPR_HistoryFakturPajak_ID());
				invoice.set_ValueOfColumn("tax_no", history.getDescription());
				invoice.saveEx();
				
				Count++;
				
			}
		}
		catch (SQLException e){
			 log.log(Level.SEVERE, "Process_SetFakturInvoice- " + sql.toString(), e);
		}
		finally{
			DB.close(rsl, pstmnt);
			rsl = null;
			pstmnt = null;
		}
		
		return "Count Generate Faktur Invoice : "+Count;
	}
	

}
