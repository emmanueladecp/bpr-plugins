package com.idempierecloud.bpr.callout;

import java.math.BigDecimal;

import org.compiere.model.MInOutLine;
import org.compiere.model.MProduct;
import org.compiere.model.MUOMConversion;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetMovementQtyConfirmLine extends CustomCallout {

	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		
		BigDecimal qtyEntered = (BigDecimal) getValue();
		int M_InOutLine_ID = (int) getTab().getValue("M_InOutLine_ID");
		int C_UOM_ID = (int) getTab().getValue("C_UOM_ID");
		if(M_InOutLine_ID==0 || C_UOM_ID==0)
			return null;
		
		MInOutLine line = new MInOutLine(getCtx(), M_InOutLine_ID, null);
		
		if(line.getM_Product().getC_UOM_ID()!=C_UOM_ID) {
			BigDecimal movementQty = MUOMConversion.convertProductFrom(getCtx(), line.getM_Product_ID(), C_UOM_ID, qtyEntered);
			setValue("ConfirmedQty", movementQty);
		}else {
			setValue("ConfirmedQty", qtyEntered);
		}
		
		return null;
	}
}
