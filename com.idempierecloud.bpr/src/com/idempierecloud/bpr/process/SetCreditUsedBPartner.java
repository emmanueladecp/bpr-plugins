package com.idempierecloud.bpr.process;

import java.math.BigDecimal;

import org.compiere.model.MBPartner;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;

import com.idempierecloud.bpr.base.CustomProcess;

public class SetCreditUsedBPartner extends CustomProcess{
	int C_BPartner_ID =0;
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals(MBPartner.COLUMNNAME_C_BPartner_ID))
				C_BPartner_ID = para[i].getParameterAsInt();	
		}
		
	}
	@Override
	protected String doIt() throws Exception {
		BigDecimal CreditUsed = BigDecimal.ZERO;
		CreditUsed = DB.getSQLValueBD(get_TrxName(), "SELECT calculate_credituse(?)", C_BPartner_ID);
		
		MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, get_TrxName());
		bp.setSO_CreditUsed(CreditUsed);
		bp.saveEx();
		return "Credit Used = "+bp.getSO_CreditUsed();
	}
}
