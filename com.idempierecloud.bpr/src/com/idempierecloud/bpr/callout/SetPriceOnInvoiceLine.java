package com.idempierecloud.bpr.callout;

import java.math.BigDecimal;

import org.compiere.model.MProduct;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetPriceOnInvoiceLine extends CustomCallout{
	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		String IsSOTrx = getTab().get_ValueAsString("isSoTrx");
		if(IsSOTrx.equals("N"))
			return null;
		BigDecimal OngkosAngkut = (BigDecimal) getTab().getValue("OngkosAngkut");
		BigDecimal SubsidiAmt = (BigDecimal) getTab().getValue("SubsidiAmt");
		BigDecimal QtyInvoiced = (BigDecimal) getTab().getValue("QtyInvoiced");
		if(getTab().getValue("M_Product_ID")==null)
			return "No Calculate custom Price Invoice : No Product";
		int M_Product_ID =(Integer)getTab().getValue("M_Product_ID");
		MProduct product = new MProduct(getCtx(), M_Product_ID, null);
		BigDecimal priceList =(BigDecimal)getValue(); 
		BigDecimal priceActual = priceList.add(OngkosAngkut).add(SubsidiAmt);
		BigDecimal priceEntered = priceActual.multiply(product.getWeight());
		BigDecimal LineNetAmt = priceActual.multiply(QtyInvoiced);	
		
		getTab().setValue("PriceActual", priceActual);
		getTab().setValue("PriceEntered", priceEntered);
		getTab().setValue("LineNetAmt", LineNetAmt);
			
		return null;
	}
}
