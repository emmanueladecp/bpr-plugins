package com.idempierecloud.bpr.callout;

import java.math.BigDecimal;

import org.compiere.model.MProduct;

import com.idempierecloud.bpr.base.CustomCallout;

public class SetPriceListOnInvoiceLine  extends CustomCallout{
	
	private static final BigDecimal ZERO = BigDecimal.ZERO;
	
	@Override
	protected String start() {
		if(getValue()==null)
			return null;
		String IsSOTrx = getTab().get_ValueAsString("isSoTrx");
		if(IsSOTrx.equals("N"))
			return null;
		if(getTab().getValue("M_Product_ID")==null)
			return null;
		BigDecimal OngkosAngkut = (BigDecimal) getTab().getValue("OngkosAngkut");
		BigDecimal SubsidiAmt = (BigDecimal) getTab().getValue("SubsidiAmt");
		BigDecimal QtyInvoiced = (BigDecimal) getTab().getValue("QtyInvoiced");
		int M_Product_ID =(Integer)getTab().getValue("M_Product_ID");
		MProduct product = new MProduct(getCtx(), M_Product_ID, null);
		BigDecimal priceEntered =(BigDecimal)getValue(); 
		BigDecimal priceActual =(BigDecimal)getValue(); 
		
		//handling bug divide by zero if weight = 0
		
		if (product.getWeight().compareTo(ZERO) == 0) {
			priceActual = priceEntered;
		} else {
			if (product.getWeight() ==  BigDecimal.valueOf(0) )
			{
				priceActual = priceEntered;
			} else {
				priceActual = priceEntered.divide(product.getWeight()).setScale(0);
			}
		}
		
		BigDecimal PriceList = priceActual.subtract(OngkosAngkut).subtract(SubsidiAmt);
		BigDecimal LineNetAmt = priceActual.multiply(QtyInvoiced);	
		
		getTab().setValue("PriceActual", priceActual);
		getTab().setValue("PriceList", PriceList);
		getTab().setValue("LineNetAmt", LineNetAmt);
			
		return null;
	}
}