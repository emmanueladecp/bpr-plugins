package com.idempierecloud.bpr.component;

import org.compiere.grid.ICreateFrom;
import org.compiere.grid.ICreateFromFactory;
import org.compiere.model.GridTab;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_M_InOut;
import org.compiere.model.I_M_Movement;

import com.idempierecloud.bpr.form.CreateFromMaterialMovement;
import com.idempierecloud.bpr.form.CreateFromPicklist;
import com.idempierecloud.bpr.form.WCreateFromOrder;
import com.idempierecloud.bpr.form.WCreateFromShipmentUI;
import com.idempierecloud.bpr.model.I_BPR_Picklist;

public class CreateFormFactory implements ICreateFromFactory {

	@Override
	public ICreateFrom create(GridTab mTab) {
		String tableName = mTab.getTableName();
		if (tableName.equals(I_C_Order.Table_Name))
			return new WCreateFromOrder(mTab);
		else if (tableName.equals(I_BPR_Picklist.Table_Name))
			return new CreateFromPicklist(mTab);
		else if (tableName.equals(I_M_Movement.Table_Name))
			return new CreateFromMaterialMovement(mTab);
		else if (tableName.equals(I_M_InOut.Table_Name))
			return new WCreateFromShipmentUI(mTab);
		return null;
	}

}
