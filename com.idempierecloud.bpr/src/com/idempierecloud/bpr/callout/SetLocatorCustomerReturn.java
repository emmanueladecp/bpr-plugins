package com.idempierecloud.bpr.callout;

import java.util.Properties;

import org.compiere.util.DB;
import org.compiere.util.Env;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetLocatorCustomerReturn extends CustomCallout {

	@Override
	protected String start() {
		if(!getTab().getName().equals("Customer Return Line"))
			return null;

		Properties ctx = getCtx();
		Integer locatorID = (Integer) getValue();
		String isSusut = Env.getContext(ctx, getWindowNo(), "IsSusut", true);
		if (isSusut.equals("Y") && getTab().getValue("M_InOutLine_ID")==null) {
			int warehouseID = Env.getContextAsInt(ctx, getWindowNo(), "M_Warehouse_ID", true);
			if (warehouseID > 0) {
				locatorID = DB.getSQLValue(null, "SELECT M_Locator_ID From M_Locator Where M_Locator.M_LocatorType_ID = 1000004 And M_Locator.M_Warehouse_ID=?", warehouseID);
				if (locatorID >0) {
					setValue("M_Locator_ID", locatorID);
				}
			}
		}
		return null;
	}

}
