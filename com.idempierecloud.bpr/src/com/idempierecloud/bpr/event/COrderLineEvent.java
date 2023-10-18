package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategory;
import org.compiere.model.MProductPrice;
import org.compiere.model.MTax;
import org.compiere.model.MUOMConversion;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.globalqss.model.X_LCO_WithholdingCalc;
import org.globalqss.model.X_LCO_WithholdingType;
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
			setQtyOrdered();
			setPricePOTurus();
			setWitholdingType();
			calculateGrossUp();
			calculateOngkosAngkut();
			calculateAdditionalCost();
			calculatePrice();
			setProposalRetur();
			calculateLinetNetAmt();
			setDiscount();
			checkSOCreditLimit();
			setIfOrderlineFOC();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			setQtyOrdered();
			calculatePriceInsentif();
			setWitholdingType();
			calculateGrossUp();
			calculateOngkosAngkut();
			calculateAdditionalCost();
			calculatePrice();
			calculateLinetNetAmt();
			setDiscount();
			checkSOCreditLimit();
			setIfOrderlineFOC();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_DELETE)) {
			checkRequisitionLine();
		}else if(event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)) {
			checkCreditUsedChange();
		}else if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			checkCreditUsedChange();
		}
	}
	
	private void checkCreditUsedChange() {
		MOrder order = (MOrder) orderLine.getC_Order();
		MDocType doctype = (MDocType) order.getC_DocTypeTarget();
		if(order.isSOTrx()&&!doctype.get_ValueAsBoolean("isRetur")){
			if(order.get_ValueAsBoolean("isdone")&&order.getDocStatus().equals(MOrder.DOCSTATUS_InProgress)) {
					MBPartner bp = (MBPartner) order.getC_BPartner();
					BigDecimal sumLineAmt = DB.getSQLValueBD(orderLine.get_TrxName(), " Select coalesce(sum(linenetamt),0) from c_orderline where c_orderline_id not in (?) and c_order_id = ? ", orderLine.get_ID(),orderLine.getC_Order_ID());
									
					BigDecimal LineNetAmt = orderLine.getLineNetAmt();
					BigDecimal OldLineNetAmt = (BigDecimal) orderLine.get_ValueOld("LineNetAmt");
					
					
					BigDecimal GrandTotal = LineNetAmt.add(sumLineAmt);
					BigDecimal OldGrandTotal = OldLineNetAmt.add(sumLineAmt);
					//reset credit used
					BigDecimal creditUsed = bp.getSO_CreditUsed().subtract(OldGrandTotal);
					//set new credit used
					BigDecimal NewcreditUsed = creditUsed.add(GrandTotal);
					bp.setSO_CreditUsed(NewcreditUsed);
					bp.saveEx();
			}
		}
	}
	
	private void calculatePriceInsentif() {
		MDocType docType = (MDocType) orderLine.getC_Order().getC_DocTypeTarget();
		if(!docType.get_ValueAsBoolean("isTurus"))
			return;
		int m_inout_id = DB.getSQLValue(orderLine.get_TrxName(), "select coalesce(m_inout_id) from m_inoutline where c_orderline_id = ?", orderLine.getC_OrderLine_ID());
		if(orderLine.get_ValueAsBoolean("IsInsentif")&&m_inout_id<=0) {
			
			 StringBuffer sqlStmt = new StringBuffer();
			    sqlStmt.append(" select c_order_id,m_product_id,percetase from adempiere.bpr_insentif_v where c_order_id=?");
			    PreparedStatement pstmt = null;
			    ResultSet rs = null;	    
			    try{
			    	pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			    	int index = 1;
			    	pstmt.setInt(index++, orderLine.getC_Order_ID());
			    	
				    rs = pstmt.executeQuery();
				    while (rs.next()){
				    	BigDecimal percentage = rs.getBigDecimal("percetase");
				    	if(percentage.compareTo(BigDecimal.valueOf(60))>=0) {
				    		BigDecimal insentif =((BigDecimal) orderLine.get_Value("PriceNet")).add(new BigDecimal (100));
					    	orderLine.set_ValueOfColumn("PriceNet", insentif);
					    	orderLine.setPrice(insentif);
				    	}
				    }		    
			    }catch(Exception e){
			    	log.log(Level.SEVERE, sqlStmt.toString());
			    }finally{
			    	DB.close(rs, pstmt);
			    	pstmt = null;
			    	rs = null;
			    }
		}
	}
		
		
	
	
	private void setIfOrderlineFOC() {
		final int Doctype_ManualOrder = 1000060; 
		if(orderLine.getC_Order().getC_DocTypeTarget_ID()==Doctype_ManualOrder && orderLine.getC_Order().isSOTrx()) {
			if(orderLine.get_ValueAsBoolean("isFOC")) {
				orderLine.setPrice(BigDecimal.ZERO);
				orderLine.setLineNetAmt(BigDecimal.ZERO);
				orderLine.set_ValueOfColumn("subsidiamt", BigDecimal.ZERO);;
			}
		}
	}
	
	private void calculateGrossUp() {
		// If Sales Order, Skip
		if(orderLine.getC_Order().isSOTrx())
			return;
		
		// if no one grossup checked 
		if(!orderLine.get_ValueAsBoolean("isGrossUpPPh") && !orderLine.get_ValueAsBoolean("isGrossUpPPN"))
			return;
	
		BigDecimal priceNet = (BigDecimal) orderLine.get_Value("PriceNet");
		if(priceNet==null)
			priceNet = Env.ZERO;
		
		BigDecimal priceEntered = priceNet;
		if(orderLine.get_ValueAsBoolean("isGrossUpPPh")) {
			if(orderLine.get_ValueAsInt("LCO_WithholdingType_ID")==0)
				throw new AdempiereException("No Withholding type found "+orderLine.getLine());
				
			X_LCO_WithholdingType type = new X_LCO_WithholdingType(orderLine.getCtx(), orderLine.get_ValueAsInt("LCO_WithholdingType_ID"), orderLine.get_TrxName());
			
			X_LCO_WithholdingCalc calc = new Query(orderLine.getCtx(), X_LCO_WithholdingCalc.Table_Name, X_LCO_WithholdingCalc.COLUMNNAME_LCO_WithholdingType_ID+"=?", orderLine.get_TrxName())
					.setParameters(type.getLCO_WithholdingType_ID())
					.first();
	
			if(calc==null)
				throw new AdempiereException("No Withholding calc found "+type.getName());
			
			BigDecimal taxRate = calc.getC_Tax().getRate();
			
			priceEntered = priceNet.divide(Env.ONE.subtract(taxRate.divide(Env.ONEHUNDRED, 4, RoundingMode.HALF_UP)), 4, RoundingMode.HALF_UP);
		}
		
		if(orderLine.get_ValueAsBoolean("isGrossUpPPN")) {
			MTax ppnGrossUp = new Query(orderLine.getCtx(), MTax.Table_Name, "isGrossUpPPN='Y'", orderLine.get_TrxName())
					.setClient_ID()
					.first();
			
			if(ppnGrossUp==null)
				throw new AdempiereException("No Tax Rate for PPN Gross Up");
			
			BigDecimal taxRate = ppnGrossUp.getRate();
			BigDecimal ppn = priceNet.multiply(taxRate.divide(Env.ONEHUNDRED, 4, RoundingMode.HALF_UP));
			priceEntered = priceEntered.add(ppn);
		}
		
		orderLine.setPrice(priceEntered);
	}

	private void setWitholdingType() {
		MDocType docType = (MDocType) orderLine.getC_Order().getC_DocTypeTarget();
		if(!docType.get_ValueAsBoolean("isTurus") || orderLine.getM_Product_ID()==0)
			return;

		BigDecimal priceNet = (BigDecimal) orderLine.get_Value("PriceNet");
		if(priceNet==null)
			priceNet = Env.ZERO;
		
		MProductCategory productCategory = (MProductCategory) orderLine.getM_Product().getM_Product_Category();
		if(!productCategory.get_ValueAsBoolean("IsPph")) {
			orderLine.setPrice(priceNet);
			return;
		}
		
		MBPartner bp = (MBPartner) orderLine.getC_Order().getC_BPartner();
		String pph = null;
		if(bp.get_ValueAsBoolean("IsNpwp")) {
			pph = "PPH 0.25%";
		}else {
			pph = "PPH 0.5%";
		}
		
		X_LCO_WithholdingType type = new Query(orderLine.getCtx(), X_LCO_WithholdingType.Table_Name, "name=?", orderLine.get_TrxName())
				.setParameters(pph)
				.first();
		
		if(type==null)
			throw new AdempiereException("No Withholding Type found "+pph);
		
		orderLine.set_ValueOfColumn("LCO_WithholdingType_ID", type.getLCO_WithholdingType_ID());
	}

	private void calculateAdditionalCost() {
		if(!orderLine.getC_Order().isSOTrx() || orderLine.getM_Product_ID()==0)
			return;
		
		StringBuffer additionalCostSql = new StringBuffer();
		additionalCostSql.append(" select mp.m_product_id , mp.value, mp.name, mp.m_product_category_id , ba.issoline, ba.c_bp_group_id , ba.m_pricelist_id , ba.costamt, ba.issoline, bal.weightfrom , bal.weightto");
		additionalCostSql.append(" from m_product mp");
		additionalCostSql.append(" join bpr_additionalcost_line bal on mp.m_product_category_id = bal.m_product_category_id");
		additionalCostSql.append(" join bpr_additionalcost ba on bal.bpr_additionalcost_id = ba.bpr_additionalcost_id");
		additionalCostSql.append(" where ba.c_bp_group_id=? and ba.m_pricelist_id=? and mp.m_product_id = ? and ba.isactive = 'Y' and bal.isactive='Y' ");
		
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
		if(orderLine.getC_Order().isSOTrx()&&!orderLine.getC_Order().getDocStatus().equals("CO")) {
			if(orderLine.getC_Order().getC_DocTypeTarget_ID()!=1000084) {//proposal retur
				MOrder order = (MOrder) orderLine.getC_Order();
				BigDecimal lineamt = orderLine.getLineNetAmt();
				BigDecimal SO_CreditAvaiable = (BigDecimal) order.get_Value("SO_CreditAvailable");
				if(SO_CreditAvaiable==null)
					SO_CreditAvaiable = Env.ZERO;
				BigDecimal sumLineAmt = DB.getSQLValueBD(orderLine.get_TrxName(), " Select coalesce(sum(linenetamt),0) from c_orderline where c_orderline_id not in (?) and c_order_id = ? ", orderLine.get_ID(),orderLine.getC_Order_ID());
				BigDecimal grandTotal = lineamt.add(sumLineAmt);
				if(SO_CreditAvaiable.compareTo(grandTotal)<0) {
					log.warning("Grand Total Melebihi SO Credit Available pada Header");
					throw new AdempiereException("Grand Total Melebihi SO Credit Available pada Header");
				}
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
		int M_PriceList_Version_ID = DB.getSQLValue(orderLine.get_TrxName(), "SELECT M_PriceList_Version_ID FROM M_PriceList_Version WHERE isActive='Y' AND M_PriceList_ID=? AND ValidFrom<=? order By ValidFrom DESC Limit 1", orderLine.getC_Order().getM_PriceList_ID(), orderLine.getC_Order().getDateOrdered());
		
		MProductPrice price = MProductPrice.get(orderLine.getCtx(), M_PriceList_Version_ID, orderLine.get_ValueAsInt("relatedProduct_ID"), orderLine.get_TrxName());
		if(price==null)
			throw new AdempiereException("No Product Price for "+relatedProduct.getName()+" M_PriceList_Version_ID:"+M_PriceList_Version_ID);
		orderLine.setPriceEntered(price.getPriceList());
		orderLine.setPriceList(price.getPriceList());
		orderLine.setPriceActual(price.getPriceList());
		orderLine.setPriceLimit(price.getPriceLimit());
		orderLine.set_ValueOfColumn("PriceNet", price.getPriceList());
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
		int C_DocType_ID_CustomerReturnBPR=1000084;
        if((docType.get_ID()==C_DocType_ID_CustomerReturnBPR)) {
        	return;
        }
        	
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
	private void setProposalRetur() {
		
		MOrder order = (MOrder)orderLine.getC_Order();
		int C_DocType_ID_CustomerReturnBPR=1000084;
		
		if(order.getC_DocTypeTarget_ID()==C_DocType_ID_CustomerReturnBPR) {
			int C_Invoiceline_ID = DB.getSQLValue(orderLine.get_TrxName(), "select max(c_invoiceline_id) from c_invoiceline ci "
					+ " join c_invoice ci2 on ci.c_invoice_id = ci2.c_invoice_id "
					+ " where ci.m_product_id=? and ci2.C_BPartner_ID=? and ci2.docstatus in ('CO','CL') and ci2.isSoTrx='Y'", orderLine.getM_Product_ID(), orderLine.getC_BPartner_ID());
			if(C_Invoiceline_ID > 0) {
				MInvoiceLine inLine = new MInvoiceLine(orderLine.getCtx(),C_Invoiceline_ID, orderLine.get_TrxName());
				orderLine.setPriceEntered(inLine.getPriceEntered());
				orderLine.setPriceActual(inLine.getPriceActual());
			}
			else {
				orderLine.setPriceActual(orderLine.getPriceList());
			}
		}
	}
	
	private void setQtyOrdered() {
		if(orderLine.getC_UOM_ID()!=orderLine.getM_Product().getC_UOM_ID()) {
			if(orderLine.getM_Product_ID()==0)
				return;
			BigDecimal qtyOrdered = MUOMConversion.convertProductFrom(orderLine.getCtx(), orderLine.getM_Product_ID(), orderLine.getC_UOM_ID(), orderLine.getQtyEntered());
			orderLine.setQtyOrdered(qtyOrdered);
		}	
	}
	
	@Override
	protected void doHandleEvent() {
		
	}

}
