package com.idempierecloud.bpr.callout;

import java.util.List;

import org.compiere.model.MUOMConversion;
import org.compiere.model.Query;
import org.compiere.util.Env;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetUOMOrderLine extends CustomCallout {

	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		
		Integer M_Product_ID = (Integer) getValue();
		String isSOTrx = Env.getContext(getCtx(), getWindowNo(), "IsSOTrx");
		
		if(M_Product_ID==0 || (isSOTrx!=null && isSOTrx.equals("N")))
			return null;
		
		MUOMConversion conversion = new Query(getCtx(), MUOMConversion.Table_Name, "M_Product_ID=?", null) 
				.setParameters(M_Product_ID)
				.first();
		if(conversion!=null) {
			setValue("C_UOM_ID", conversion.getC_UOM_To_ID());
		}
		
		return null;
	}

}
