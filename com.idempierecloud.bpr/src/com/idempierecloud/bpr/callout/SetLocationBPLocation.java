package com.idempierecloud.bpr.callout;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetLocationBPLocation  extends CustomCallout{

	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		setValue("C_Location_ID", 1000000);
		return null;
	}

}
