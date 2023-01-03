package com.idempierecloud.bpr.callout;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.compiere.util.Env;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetDatePromiseMOrderLine extends CustomCallout {

	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		
		Timestamp DateOrdered = (Timestamp) getValue();
		String isSOTrx = Env.getContext(getCtx(), getWindowNo(), "IsSOTrx");
		if(DateOrdered==null|| (isSOTrx!=null && isSOTrx.equals("N")))
			return null;  
        
		Date date = new Date(DateOrdered.getTime());         
		Calendar cal = Calendar.getInstance();
		cal.setTime(date); 
		cal.add(Calendar.DATE, 2); 

		Date modifiedDate = cal.getTime();
		Timestamp DatePromised=new Timestamp(modifiedDate.getTime());  
		
		getTab().setValue("DatePromised", DatePromised);
		return null;
	}

}
