package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_M_RelatedProduct;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MProductionLineExt;

public class MProductionLineEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(MProductionLineEvent.class);
	
	private MProductionLineExt prodLine = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("production line Event : "+event.getTopic());
		
		prodLine = (MProductionLineExt) po;
		if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW))
			checkRelatedProduct();
		else if(event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE))
			checkRelatedProduct();
	}
	
	private void checkRelatedProduct() {
		List<X_M_RelatedProduct> relatedProducts = getRelatedProducts();
		if(!prodLine.isEndProduct() && relatedProducts.size()==0 && prodLine.isProcessed())
			return;
		
		for(X_M_RelatedProduct relatedProduct : relatedProducts) {
			MProductionLineExt line = new Query(prodLine.getCtx(), MProductionLineExt.Table_Name, "M_Production_ID=? AND M_Product_ID=?", prodLine.get_TrxName())
					.setParameters(prodLine.getM_Production_ID(), relatedProduct.getRelatedProduct_ID())
					.first();
			
			if(prodLine.getM_Product().getWeight().equals(Env.ZERO))
				throw new AdempiereException("Berat Produk belum diset "+prodLine.getM_Product().getName());
			
			BigDecimal qtyUsed = prodLine.getMovementQty().divide(prodLine.getM_Product().getWeight(), 0, RoundingMode.UP);
			BigDecimal qty = (BigDecimal) relatedProduct.get_Value("Qty");
			if(qty==null)
					qty = Env.ONE;
			
			qtyUsed = qtyUsed.multiply(qty);
			
			if(line==null) {
				line = new MProductionLineExt(prodLine.getCtx(), 0, prodLine.get_TrxName());
				line.setAD_Org_ID(prodLine.getM_Production().getAD_Org_ID());
				line.setM_Production_ID(prodLine.getM_Production_ID());
				line.setM_Locator_ID(prodLine.getM_Locator_ID());
				line.setM_Product_ID(relatedProduct.getRelatedProduct_ID());
				line.set_ValueOfColumn("JenisProduk", "P");
				line.setIsEndProduct(false);
			}
			line.setQtyUsed(qtyUsed);
			line.saveEx();
		}
	}

	private List<X_M_RelatedProduct> getRelatedProducts() {
		return new Query(prodLine.getCtx(), X_M_RelatedProduct.Table_Name, X_M_RelatedProduct.COLUMNNAME_M_Product_ID+"=? AND "+X_M_RelatedProduct.COLUMNNAME_RelatedProductType+"=?", prodLine.get_TrxName())
				.setParameters(prodLine.getM_Product_ID(), "B")
				.list();
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
