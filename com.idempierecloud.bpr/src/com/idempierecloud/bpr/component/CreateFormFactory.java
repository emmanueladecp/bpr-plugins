package com.idempierecloud.bpr.component;

import org.compiere.grid.ICreateFrom;
import org.compiere.grid.ICreateFromFactory;
import org.compiere.model.GridTab;
import org.compiere.model.I_C_Order;

import com.idempierecloud.bpr.form.CreateFromPicklist;
import com.idempierecloud.bpr.form.WCreateFromOrder;
import com.idempierecloud.bpr.model.I_BPR_Picklist;

public class CreateFormFactory implements ICreateFromFactory {

	@Override
	public ICreateFrom create(GridTab mTab) {
		String tableName = mTab.getTableName();
		if (tableName.equals(I_C_Order.Table_Name))
			return new WCreateFromOrder(mTab);
		else if (tableName.equals(I_BPR_Picklist.Table_Name))
			return new CreateFromPicklist(mTab);
		return null;
	}

}
