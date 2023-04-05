package com.idempierecloud.bpr.callout;

import java.math.BigDecimal;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetInternalUseQty  extends CustomCallout {

	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		
		BigDecimal QtyAdd = (BigDecimal) getValue();
		getTab().setValue("QtyInternalUse", QtyAdd.negate());
		
		return null;
	}

}
