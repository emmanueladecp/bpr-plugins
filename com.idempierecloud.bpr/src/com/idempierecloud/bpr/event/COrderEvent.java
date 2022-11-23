package com.idempierecloud.bpr.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class COrderEvent extends CustomEvent{

	private static CLogger log = CLogger.getCLogger(COrderLineEvent.class);
	
	private MOrder order = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("Order Event : "+event.getTopic());
		
		order = (MOrder) po;
		if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			checkTimbanganPO();
			calculateOngkosAngkut();
			checkSalesRep();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			checkTimbanganPO();
			calculateOngkosAngkut();
			checkTimbanganNetAmt();
			checkSalesRep();
		}
	}
	
	private void checkSalesRep() {
		if(order.get_ValueAsInt("SalesRep_ID2")>0)
			order.setSalesRep_ID(order.get_ValueAsInt("SalesRep_ID2"));
	}

	private void checkTimbanganPO() {
		if(order.isSOTrx() || order.get_ValueAsInt("BPR_Timbangan_ID")==0)
			return;
		
		MOrder anotherOrder = new Query(order.getCtx(), MOrder.Table_Name, "BPR_Timbangan_ID=? AND C_Order_ID<>?", order.get_TrxName())
				.setParameters(order.get_ValueAsInt("BPR_Timbangan_ID"), order.getC_Order_ID())
				.first();
				
		if(anotherOrder!=null)
			throw new AdempiereException("Timbangan sudah digunakan di Order "+anotherOrder.getDocumentNo());
		
	}

	private void checkTimbanganNetAmt() {
		if(order.isSOTrx() || order.get_ValueAsInt("timbanganNetAmt")==0)
			return;
		
		BigDecimal timbanganNetAmt = (BigDecimal) order.get_Value("timbanganNetAmt");
		BigDecimal totalQtyEntered = DB.getSQLValueBD(order.get_TrxName(), "SELECT COALESCE(SUM(QtyEntered),0) FROM C_OrderLine WHERE C_Order_ID=?", order.getC_Order_ID());
		for(MOrderLine line : order.getLines()) {
			BigDecimal newQtyOrdered = line.getQtyEntered().subtract(
					line.getQtyEntered()
					.divide(totalQtyEntered, 2, RoundingMode.HALF_UP)
					.multiply(totalQtyEntered.subtract(timbanganNetAmt))
				);
			line.setQtyOrdered(newQtyOrdered);
			line.saveEx();
		}
	}
	
	private void calculateOngkosAngkut() {
		
		if(order.getDeliveryViaRule().equalsIgnoreCase("")) {
			return;
		}
			
		String sqlStmt = "select C_OrderLine_ID from C_OrderLine where C_Order_ID = ? and isActive = 'Y' ";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = DB.prepareStatement(sqlStmt, null);
			pstmt.setInt(1, order.get_ID());
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				
				int C_OrderLine_ID = rs.getInt(1);
				MOrderLine orderLine = new MOrderLine(order.getCtx(), C_OrderLine_ID, order.get_TrxName());
				if(order.getDeliveryViaRule().equalsIgnoreCase("D")) {
					
					if(orderLine.getM_Product_ID()==0)
						return;
					if(orderLine.getC_BPartner_Location_ID()==0)
						return;
					MBPartnerLocation BPLoc = new MBPartnerLocation(orderLine.getCtx(), orderLine.getC_BPartner_Location_ID(), orderLine.get_TrxName());
					BigDecimal BPR_OngkosAngkut = DB.getSQLValueBD(BPLoc.get_TrxName(), "Select OngkosAngkut from BPR_OngkosAngkutDetail where C_City_ID = ?", BPLoc.get_ValueAsInt("C_City_ID"));
					BigDecimal ongkosAngkut = BPR_OngkosAngkut.multiply(orderLine.getQtyEntered()).multiply(orderLine.getM_Product().getWeight());
					orderLine.set_ValueOfColumn("OngkosAngkut", ongkosAngkut);
					
				}else if (order.getDeliveryViaRule().equalsIgnoreCase("P")){
					if(orderLine.getM_Product_ID()==0)
						return;
					if(orderLine.getC_BPartner_Location_ID()==0)
						return;
					BigDecimal ongkosAngkut = BigDecimal.ZERO;
					orderLine.set_ValueOfColumn("OngkosAngkut", ongkosAngkut);
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sqlStmt.toString(), e);
		} finally{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
	}
	
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

}
