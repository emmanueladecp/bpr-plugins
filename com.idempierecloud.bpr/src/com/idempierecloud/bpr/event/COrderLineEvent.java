package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.MUOMConversion;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class COrderLineEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(COrderLineEvent.class);
	
	private MOrderLine orderLine = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("OrderLine Event : "+event.getTopic());
		
		orderLine = (MOrderLine) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			setPricePOTurus();
			calculateOngkosAngkut();
			calculateAdditionalCost();
			calculatePrice();
			calculateLinetNetAmt();
			setDiscount();
			checkSOCreditLimit();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			calculateOngkosAngkut();
			calculateAdditionalCost();
			calculatePrice();
			calculateLinetNetAmt();
			setDiscount();
			checkSOCreditLimit();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_DELETE)) {
			checkRequisitionLine();
		}
	}	
	
	private void calculateAdditionalCost() {
		if(!orderLine.getC_Order().isSOTrx() || orderLine.getM_Product_ID()==0)
			return;
		
		StringBuffer additionalCostSql = new StringBuffer();
		additionalCostSql.append(" select mp.m_product_id , mp.value, mp.name, mp.m_product_category_id , ba.issoline, ba.c_bp_group_id , ba.m_pricelist_id , ba.costamt, ba.issoline, bal.weightfrom , bal.weightto");
		additionalCostSql.append(" from m_product mp");
		additionalCostSql.append(" join bpr_additionalcost_line bal on mp.m_product_category_id = bal.m_product_category_id");
		additionalCostSql.append(" join bpr_additionalcost ba on bal.bpr_additionalcost_id = ba.bpr_additionalcost_id");
		additionalCostSql.append(" where ba.c_bp_group_id=? and ba.m_pricelist_id=? and mp.m_product_id = ?");
		
		PreparedStatement pstmt = DB.prepareStatement(additionalCostSql.toString(), orderLine.get_TrxName());
		ResultSet rs = null;
		BigDecimal subsidiAmt = Env.ZERO;
		try {
			pstmt.setInt(1, orderLine.getC_Order().getC_BPartner().getC_BP_Group_ID());
			pstmt.setInt(2, orderLine.getC_Order().getM_PriceList_ID());
			pstmt.setInt(3, orderLine.getM_Product_ID());
			rs = pstmt.executeQuery();
			while(rs.next()) {
				BigDecimal costAmt = rs.getBigDecimal("costamt");
				if(costAmt==null)
					costAmt = Env.ZERO;
				if(rs.getBoolean("issoline")) {
					subsidiAmt = subsidiAmt.add(costAmt);
				}else {
					BigDecimal totalQty = DB.getSQLValueBD(orderLine.get_TrxName(), "SELECT COALESCE(SUM(qtyordered),0) FROM C_OrderLine WHERE C_Order_ID=? AND C_OrderLine_ID<>?", orderLine.getC_Order_ID(), orderLine.getC_OrderLine_ID());
					totalQty = totalQty.add(orderLine.getQtyOrdered());
					BigDecimal weightFrom = rs.getBigDecimal("weightfrom");
					BigDecimal weightTo = rs.getBigDecimal("weightto");
					if(weightFrom==null)
						weightFrom = Env.ZERO;

					if(weightTo==null)
						weightTo = Env.ZERO;
					if(weightFrom.equals(Env.ZERO) && weightTo.equals(Env.ZERO))
						continue;
					if(totalQty.compareTo(weightFrom)>=0 && totalQty.compareTo(weightTo)<=0)
					{
						subsidiAmt = subsidiAmt.add(costAmt);
					}
				}
			}
		} catch (SQLException e) {
			log.info(e.getLocalizedMessage());
			throw new AdempiereException("Failed calculate additional cost");
		}
		
		orderLine.set_ValueOfColumn("SubsidiAmt", subsidiAmt);

	}

	private void checkSOCreditLimit() {
		if(orderLine.getC_Order().isSOTrx()) {
			MOrder order = (MOrder) orderLine.getC_Order();
			BigDecimal grandTotal = order.getGrandTotal(); ;
			BigDecimal SO_CreditAvaiable = (BigDecimal) order.get_Value("SO_CreditAvailable");
			if(SO_CreditAvaiable.compareTo(grandTotal)<0) {
				log.warning("Grand Total Melebihi SO Credit Available pada Header");
				throw new AdempiereException("Grand Total Melebihi SO Credit Available pada Header");
			}
		}
	}
	
	private void setPricePOTurus() {
		MDocType docType = (MDocType) orderLine.getC_Order().getC_DocTypeTarget();
		if(!docType.get_ValueAsBoolean("isTurus"))
			return;
		
		if(orderLine.get_ValueAsBoolean("isFOC")) {
			orderLine.setPriceEntered(Env.ZERO);
			orderLine.setPriceList(Env.ZERO);
			orderLine.setPriceActual(Env.ZERO);
			orderLine.setPriceLimit(Env.ZERO);
			return;
		}
		
		if(orderLine.get_ValueAsInt("relatedProduct_ID")==0)
			return;
		
		MProduct relatedProduct = new MProduct(orderLine.getCtx(), orderLine.get_ValueAsInt("relatedProduct_ID"), orderLine.get_TrxName());
		int M_PriceList_Version_ID = DB.getSQLValue(orderLine.get_TrxName(), "SELECT M_PriceList_Version_ID FROM M_PriceList_Version WHERE M_PriceList_ID=? AND ValidFrom<=? order By ValidFrom DESC Limit 1", orderLine.getC_Order().getM_PriceList_ID(), orderLine.getC_Order().getDateOrdered());
		if(M_PriceList_Version_ID<=0)
			throw new AdempiereException("No Product Price for "+relatedProduct.getName());
		
		MProductPrice price = MProductPrice.get(orderLine.getCtx(), M_PriceList_Version_ID, orderLine.get_ValueAsInt("relatedProduct_ID"), orderLine.get_TrxName());
		orderLine.setPriceEntered(price.getPriceList());
		orderLine.setPriceList(price.getPriceList());
		orderLine.setPriceActual(price.getPriceList());
		orderLine.setPriceLimit(price.getPriceLimit());
	}

	private void checkRequisitionLine() {
		int no = DB.executeUpdate("UPDATE M_RequisitionLine SET C_OrderLine_ID=null WHERE C_orderLine_id=?", orderLine.getC_OrderLine_ID(), orderLine.get_TrxName());
		log.info("Updated RequisitionLine "+no);
	}
	
	private void calculatePrice() {
		MOrder order = (MOrder)orderLine.getC_Order();
		if(!order.get_ValueAsBoolean("isSOTrx"))
			return;
		if(orderLine.getM_Product_ID()==0)
			return;
		MDocType docType = (MDocType) orderLine.getC_Order().getC_DocTypeTarget();
		if(docType.get_ValueAsBoolean("isTurus"))
			return;
		BigDecimal ongkosAngkut = (BigDecimal) orderLine.get_Value("OngkosAngkut");
		BigDecimal priceEntered = ongkosAngkut.add(orderLine.getPriceList());
		BigDecimal subsidiAmt = (BigDecimal) orderLine.get_Value("SubsidiAmt");
		if(subsidiAmt==null)
			subsidiAmt = Env.ZERO;
		
		priceEntered = priceEntered.add(subsidiAmt);
		orderLine.setPriceActual(priceEntered);
		priceEntered = MUOMConversion.convertProductFrom(order.getCtx(), orderLine.getM_Product_ID(), orderLine.getC_UOM_ID(), priceEntered);
		orderLine.setPriceEntered(priceEntered);
	}
	
	private void calculateLinetNetAmt() {
		if(orderLine.getM_Product_ID()==0)
			return;
		BigDecimal LineNetAmt = orderLine.getPriceEntered().multiply(orderLine.getQtyEntered());	
		orderLine.setLineNetAmt(LineNetAmt);
	}
	
	private void calculateOngkosAngkut() {
		MOrder order = (MOrder)orderLine.getC_Order();
		if(!order.get_ValueAsBoolean("isSOTrx"))
			return;
		if(order.getDeliveryViaRule().equalsIgnoreCase("")) {
			return;
		}		
		if(order.getDeliveryViaRule().equalsIgnoreCase("D")) {//Delivery
			if(orderLine.getM_Product_ID()==0)
				return;
			if(orderLine.getC_BPartner_Location_ID()==0)
				return;
			MBPartnerLocation BPLoc = new MBPartnerLocation(orderLine.getCtx(), orderLine.getC_BPartner_Location_ID(), orderLine.get_TrxName());
			BigDecimal BPR_OngkosAngkut = DB.getSQLValueBD(BPLoc.get_TrxName(), "Select OngkosAngkut from BPR_OngkosAngkutDetail where C_City_ID = ?", BPLoc.get_ValueAsInt("C_City_ID"));
			if(BPR_OngkosAngkut!=null) {
				orderLine.set_ValueOfColumn("OngkosAngkut", BPR_OngkosAngkut);
			}
		}
		else if (order.getDeliveryViaRule().equalsIgnoreCase("P")) {//Pickup
			BigDecimal ongkosAngkut = BigDecimal.ZERO;
			orderLine.set_ValueOfColumn("OngkosAngkut", ongkosAngkut);
		}
	}
	
	private void setDiscount() {
		if(orderLine.getM_Product_ID()==0)
			return;
		orderLine.setDiscount(BigDecimal.ZERO);
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
