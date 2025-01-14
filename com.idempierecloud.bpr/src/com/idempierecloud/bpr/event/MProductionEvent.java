package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MProduct;
import org.compiere.model.PO;
import org.compiere.model.X_M_RelatedProduct;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.MProductionExt;
import com.idempierecloud.bpr.model.MProductionLineExt;

public class MProductionEvent extends CustomEvent{

	private static CLogger log = CLogger.getCLogger(MProductionEvent.class);
	private final static int C_Doctype_ID_BPR_RiceToRice = 1000066;
	private final static int C_Doctype_ID_BPR_WIPRice = 1000086;
	private final static int C_Doctype_ID_BPR_GrainToRice = 1000067;
	
	private MProductionExt production = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Production Event : "+event.getTopic());
		production = (MProductionExt) po;
		
		
		if(event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
			checkItemFGWithItemComponent();
			checkProductCost();
			checkQtyUsed();
			checkRelatedProduct();
			checkWIP();
		}
	}
	
	private void checkItemFGWithItemComponent() {
		if(production.getReversal_ID()>0)
			return;
	
		//notes : BAHAN BAKU WIP TIDAK BOLEH SAMA DENGAN END PRODUCT
		
		if(!production.get_ValueAsBoolean("IsUseProductionPlan") && production.get_ValueAsInt("C_DocType_ID")==C_Doctype_ID_BPR_GrainToRice) {
			//Tab Bahan Baku WIP
			String sqlStmt = "select M_Productionline_ID, qtyused, m_product_id from M_ProductionLine where M_Production_ID = ? and isActive = 'Y' and jenisproduk = 'W'";
			
			//Cari End Product
			String sqlStmt2 = "select M_Productionline_ID, qtyused, m_product_id from M_ProductionLine where M_Production_ID = ? and isActive = 'Y' and jenisproduk = 'E'";
			
			
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			PreparedStatement pstmt2 = null;
			ResultSet rs2 = null;
			
			try {
				pstmt = DB.prepareStatement(sqlStmt, null);
				pstmt.setInt(1, production.get_ID());
				rs = pstmt.executeQuery();
				
				pstmt2 = DB.prepareStatement(sqlStmt2, null);
				pstmt2.setInt(1, production.get_ID());
				rs2 = pstmt2.executeQuery();
				
				while (rs.next()) {
					int M_Productionline_ID = rs.getInt(1);
					BigDecimal qtyused = rs.getBigDecimal(2);
					int M_Product_ID1 = rs.getInt(3);
					
					while (rs2.next()) {
						int M_Productionline_ID2 = rs2.getInt(1);
						BigDecimal qtyused2 = rs2.getBigDecimal(2);
						int M_Product_ID2 = rs2.getInt(3);
						
						if (M_Product_ID1 == M_Product_ID2) {
							throw new AdempiereException("Product Bahan Baku WIP tidak boleh sama dengan Product Barang Jadi");
						}
					}
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, sqlStmt.toString(), e);
				log.log(Level.SEVERE, sqlStmt2.toString(), e);
			} finally{
				DB.close(rs, pstmt);
				DB.close(rs2, pstmt2);
				rs = null;
				pstmt = null;
				rs2 = null;
				pstmt2 = null;
			}
		}
	}
	
	private void checkProductCost() {
		int M_CostElement_ID_AveragePO=1000004;
		for(MProductionLineExt line : production.getLines()) {
			BigDecimal MCost_CurrentCostPrice = DB.getSQLValueBD(line.get_TrxName(), "SELECT Coalesce(M_Cost.currentcostprice,0) FROM M_Cost WHERE AD_Org_ID = ? "+
			 " and M_Product_ID = ? and M_CostElement_ID=?",production.getAD_Org_ID(),line.getM_Product_ID(), M_CostElement_ID_AveragePO);
			if(MCost_CurrentCostPrice==null) {
				throw new AdempiereException("Tidak ditemukan Cost untuk Product : "+line.getM_Product().getName()
											+", Organization :  "+line.getAD_Org_ID()
											+", Cost Elemet : Average PO");
			}else if(MCost_CurrentCostPrice.signum()<=0) {
					throw new AdempiereException("Cost untuk Product : "+line.getM_Product().getName()
							+", Organization :  "+line.getAD_Org_ID()
							+", Cost Elemet : Average PO, Current Cost Price Harus Lebih Besar dari 0");
			}
		}
		
	}
	
	private void checkWIP() {
		if(!production.get_ValueAsBoolean("IsWIP"))
			return;
		if(production.get_ValueAsInt("C_DocType_ID")==C_Doctype_ID_BPR_WIPRice) {
			BigDecimal totalQty = DB.getSQLValueBD(production.get_TrxName(), "SELECT COALESCE(SUM(movementqty),0) FROM M_ProductionLine WHERE M_Production_ID=?", production.getM_Production_ID());
			
			if(totalQty.signum()!=0)
				throw new AdempiereException("total Bahan Baku dan WIP tidak sama. diff "+totalQty);	
		}
	}

	private void checkRelatedProduct() {
		if(production.getReversal_ID()>0)
			return;
		
		for(MProductionLineExt line : production.getLines()) {
			X_M_RelatedProduct relatedProduct = line.getRelatedProduct();
			if(relatedProduct!=null) {
				MProductionLineExt parent = MProductionLineExt.getLine(line.getCtx(), relatedProduct.getM_Product_ID(), line.getM_Production_ID(), line.get_TrxName());
				BigDecimal requiredQty = parent.getMovementQty().divide(parent.getM_Product().getWeight(), 0, RoundingMode.UP);
				BigDecimal qty = (BigDecimal) relatedProduct.get_Value("Qty");
				if(qty==null)
						qty = Env.ONE;
				
				requiredQty = requiredQty.multiply(qty);
				if(parent!=null && requiredQty.compareTo(line.getQtyUsed())>0)
					throw new AdempiereException("Qty "+line.getM_Product().getName()+" must be equal or more than Qty "+parent.getM_Product().getName());
			}
		}
	}

	private void checkQtyUsed() {
		//if Window Production Plan (Rice to Rice)
		if(!production.get_ValueAsBoolean("IsUseProductionPlan") && production.get_ValueAsInt("C_DocType_ID")==C_Doctype_ID_BPR_RiceToRice) {
			//if bahan baku
			String sqlStmt = "select M_Productionline_ID, qtyused from M_ProductionLine where M_Production_ID = ? and isActive = 'Y' and jenisproduk = 'B'";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			
			try {
				pstmt = DB.prepareStatement(sqlStmt, null);
				pstmt.setInt(1, production.get_ID());
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					int M_Productionline_ID = rs.getInt(1);
					BigDecimal qtyused = rs.getBigDecimal(2);
					MProductionLineExt line = new MProductionLineExt(production.getCtx(), M_Productionline_ID, production.get_TrxName());
					
					//validation if qtyused = 0, return error messege
					if(qtyused.compareTo(BigDecimal.ZERO)==0)
						throw new AdempiereException("QtyUsed Bahan Baku tidak boleh 0 pada Bahan Baku dengan produk : "+line.getM_Product().getValue()+"_"+line.getM_Product().getName());							
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, sqlStmt.toString(), e);
			} finally{
				DB.close(rs, pstmt);
				rs = null;
				pstmt = null;
			}
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
