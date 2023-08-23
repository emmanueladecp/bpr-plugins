package com.idempierecloud.bpr.process;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;

import org.compiere.model.MCost;
import org.compiere.model.MPeriod;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.model.MBPRPOBahanBaku;
import com.idempierecloud.bpr.model.MBPRPOBahanBakuLine;

public class ScheduleAnalisaPembelianBahanBaku extends CustomProcess {
	
	int m_AD_Client_ID = 0;
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = para[i].getParameterAsInt();	
		}
	}

	@Override
	protected String doIt() throws Exception {
		LocalDate date = LocalDate.now();
		int id = 0;
		if(date.getDayOfMonth()==1) {
			MPeriod period = new Query(getCtx(), MPeriod.Table_Name, " NOW()-INTERVAL '1 DAY' >= startdate AND NOW()-INTERVAL '1 DAY' <= enddate ", get_TrxName())
					.setClient_ID()
					.setOnlyActiveRecords(true)
					.first();
			
			StringBuilder sql = new StringBuilder ("select  period, c_period_id, harga_beras_rata  from bpr_periodavgberas where c_period_id = ?");
			PreparedStatement pstmnt = null;
			ResultSet rsl = null;
			try
			{
				pstmnt = DB.prepareStatement (sql.toString(), get_TrxName());
				int index = 1; 
				pstmnt.setInt(index++, period.getC_Period_ID());
				rsl = pstmnt.executeQuery ();
				while (rsl.next ()){
					MBPRPOBahanBaku header = new MBPRPOBahanBaku(getCtx(), 0, get_TrxName());
					header.setAD_Org_ID(0);
					header.setName(period.getName());
					header.setC_Period_ID(period.getC_Period_ID());
					header.setCostingMethod("S");
					header.setAmount(rsl.getBigDecimal(3));
					header.saveEx();
					id = header.getBPR_POBahanBaku_ID();
				}
			}
			catch (SQLException e){
			 log.log(Level.SEVERE, " ScheduleAnalisaPembelianBahanBaku- " + sql.toString(), e);
			}
			finally{
			DB.close(rsl, pstmnt);
			rsl = null;
			pstmnt = null;
			}
		}
		int noCosts = 0;
		int updatedCosts = 0;
		
		MBPRPOBahanBaku bahanBaku = new MBPRPOBahanBaku(getCtx(), id, get_TrxName());
		if(bahanBaku.isProcessed())
			return "Record already locked";
		
		MBPRPOBahanBakuLine[] lines = bahanBaku.getLines();
		for(MBPRPOBahanBakuLine line : lines) {
			if(line.getNewCostPrice().signum()==0) {
				noCosts++;
			}else {
				MCost cost = line.getCost();
				cost.setCurrentCostPrice(line.getNewCostPrice());
				cost.saveEx();
				updatedCosts++;
			}
			
			line.setProcessed(true);
			line.saveEx();
		}
		
		bahanBaku.setProcessed(true);
		bahanBaku.saveEx();
		
		return "Updated "+lines.length+", No Product Costs : "+noCosts+", Updated Costs : "+updatedCosts;	
	}

}
