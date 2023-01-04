package com.idempierecloud.bpr.callout;

import java.math.BigDecimal;

import org.compiere.model.MBPartner;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetSOCreditAvailable extends CustomCallout {

	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		
		String isSOTrx = getTab().get_ValueAsString("IsSOTrx");
		if(isSOTrx==null || isSOTrx.equals("N"))
			return null;
		
		int C_BPartner_ID = (int) getValue();
		
		MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, null);
		BigDecimal creditAvailable = bp.getSO_CreditLimit().subtract(bp.getSO_CreditUsed());
		setValue("SO_CreditAvailable", creditAvailable);
		
		return null;
	}

}
