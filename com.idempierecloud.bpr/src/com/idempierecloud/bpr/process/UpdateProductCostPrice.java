package com.idempierecloud.bpr.process;

import org.compiere.model.MCost;
import org.compiere.process.ProcessInfoParameter;

import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.model.MBPRPOBahanBaku;
import com.idempierecloud.bpr.model.MBPRPOBahanBakuLine;

public class UpdateProductCostPrice extends CustomProcess {

	private int m_BPR_POBahanBaku_ID;

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals(MBPRPOBahanBaku.COLUMNNAME_BPR_POBahanBaku_ID))
				m_BPR_POBahanBaku_ID = para[i].getParameterAsInt();	
		}

	}

	@Override
	protected String doIt() throws Exception {
		if(m_BPR_POBahanBaku_ID==0)
			m_BPR_POBahanBaku_ID = getRecord_ID();
		
		int noCosts = 0;
		int updatedCosts = 0;
		
		MBPRPOBahanBaku bahanBaku = new MBPRPOBahanBaku(getCtx(), m_BPR_POBahanBaku_ID, get_TrxName());
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
