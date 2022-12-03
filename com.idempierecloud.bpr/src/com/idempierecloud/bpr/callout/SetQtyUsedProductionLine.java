package com.idempierecloud.bpr.callout;

import java.math.BigDecimal;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetQtyUsedProductionLine extends CustomCallout {

	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		
		BigDecimal qtyEntered = (BigDecimal) getValue();
		getTab().setValue("QtyUsed", qtyEntered.negate());
		
		return null;
	}

}
