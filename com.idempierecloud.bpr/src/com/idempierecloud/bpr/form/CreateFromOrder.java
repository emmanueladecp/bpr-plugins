package com.idempierecloud.bpr.form;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import org.compiere.apps.IStatusBar;
import org.compiere.grid.CreateFrom;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MForecast;
import org.compiere.model.MForecastLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisitionLine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

public class CreateFromOrder extends CreateFrom {

	/**  Loaded Forecast         */
	protected int AD_Client_ID = 0;
	protected int AD_Org_ID = 0;
	protected boolean isSOTrx = true;
	protected boolean isSubcont = false;
	
	public CreateFromOrder(GridTab mTab) {
		super(mTab);
		if (log.isLoggable(Level.INFO)) log.info(mTab.toString());
	}

	@Override
	public Object getWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean dynInit() throws Exception {
		log.config("");
		setTitle(Msg.getElement(Env.getCtx(), "C_Order_ID", false) + " .. " + Msg.translate(Env.getCtx(), "CreateFrom"));
		AD_Client_ID = ((Integer) getGridTab().getValue("AD_Client_ID")).intValue();
		AD_Org_ID = ((Integer) getGridTab().getValue("AD_Org_ID")).intValue();
		isSOTrx = getGridTab().getValueAsBoolean("isSOTrx");
		isSubcont = getGridTab().getValueAsBoolean("IsSubcontracting");
		return true;
	}

	/**
	 *  Load Organization Field.
	 */	
	protected ArrayList<KeyNamePair> loadOrganizationData() {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append("SELECT AD_Org_ID, Name AS OrganizationName FROM AD_Org WHERE AD_Client_ID=?");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			pstmt.setInt(1, AD_Client_ID);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			}			
		}catch (SQLException e){
			log.log(Level.SEVERE, sqlStmt.toString(), e);
		}finally{
			DB.close(rs, pstmt);
			pstmt = null;
			rs = null;
		}		
		return list;
	}
	
	/**
	 *  Load DocType Field.
	 */	
	protected ArrayList<KeyNamePair> loadRequisitionData(int AD_Org_ID) {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append(" select distinct mf.M_Requsition_ID, mf.DocumentNo ")
			.append(" from M_Requisition mf")
			.append(" where mf.AD_Client_ID=? ")
			.append(" and mf.AD_Org_ID=? ")
			.append(" and mf.DocStatus IN ('CO','CL') ");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setInt(2, AD_Org_ID);
				
			rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			}			
		}catch (SQLException e){
			log.log(Level.SEVERE, sqlStmt.toString(), e);
		}finally{
			DB.close(rs, pstmt);
			pstmt = null;
			rs = null;
		}		
		return list;
	}
	
	protected Vector<Vector<Object>> getRequisitionData(int AD_Client_ID, int AD_Org_ID, int M_Requisition_ID)
	{
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    StringBuffer sqlStmt = new StringBuffer();
	    sqlStmt.append("select mf.M_Forecast_ID, mf.DocumentNo, mf.C_DocType_ID, cdt.Name as DocTypeName, mfl.DatePromised, mf.TypeForecast, ")
	    	.append("cdt.C_DocType_ID, cdt.Name, mf.C_Campaign_ID, cc.Name as CampaignName, ")
	    	.append("mfl.M_ForecastLine_ID, mfl.Line, mp.M_Product_ID, mp.Value as ProductValue, mp.Name as ProductName, uom.C_UOM_ID, COALESCE(uom.UOMSymbol,uom.Name) as UOMName, ")
	    	.append("mfl.Qty-(")
	    	.append("select coalesce(sum(ol.QtyOrdered),0) ")
	    	.append("from c_orderline ol ")
	    	.append("join c_order o on ol.c_order_id=o.c_order_id and o.docstatus not in ('VO','RE') ")
	    	.append("where ol.m_forecastline_id=mfl.m_forecastline_id) as Qty, ")
	    	.append("mfl.PriceEntered, mfl.Discount, mfl.C_Tax_ID, ct.Name as TaxName, ")
	    	.append("cur.C_Currency_ID, cur.ISO_Code as CurrencyName, mfl.LineNetAmt, mfl.M_RequisitionLine_ID ")
	    	.append("from M_Forecast mf join M_ForecastLine mfl on (mf.M_Forecast_ID=mfl.M_Forecast_ID) ")
	    	.append("join C_DocType cdt on (mf.C_DocType_ID=cdt.C_DocType_ID) ")
	    	.append("join M_Product mp on (mfl.M_Product_ID=mp.M_Product_ID) ")
	    	.append("join C_UOM uom on (mfl.C_UOM_ID=uom.C_UOM_ID) ")
	    	.append("join C_Currency cur on (mfl.C_Currency_ID=cur.C_Currency_ID) ")
	    	.append("left join C_Campaign cc on (mf.C_Campaign_ID=cc.C_Campaign_ID) ")
	    	.append("left join C_Tax ct on (mfl.C_Tax_ID=ct.C_Tax_ID) ")
	    	.append("where mf.AD_Client_ID=? ")
	    	.append("and mf.AD_Org_ID=? ")
			.append("and mf.DocStatus IN ('CO','CL') ")
	    	.append("and mfl.Qty-(")
	    	.append("select coalesce(sum(ol.QtyOrdered),0) ")
	    	.append("from c_orderline ol ")
	    	.append("join c_order o on ol.c_order_id=o.c_order_id and o.docstatus not in ('VO','RE') ")
	    	.append("where ol.m_forecastline_id=mfl.m_forecastline_id)>0 ");
	    
	    sqlStmt.append("order by mf.M_Forecast_ID, mfl.Line");
	    
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;	    
	    try{
	    	pstmt = DB.prepareStatement(sqlStmt.toString(), null);
	    	pstmt.setInt(1, AD_Client_ID);
	    	pstmt.setInt(2, AD_Org_ID);
	    	pstmt.setInt(3, M_Requisition_ID);
		    
		    rs = pstmt.executeQuery();
		    while (rs.next()){
		    	Vector<Object> line = new Vector<Object>(13);
	    		line.add(false);   	// 0-Selection
	    		line.add(rs.getBigDecimal("Qty"));  // 1-Qty
	    		KeyNamePair pp = new KeyNamePair(rs.getInt("C_UOM_ID"), rs.getString("UOMName"));
	    		line.add(pp);
	    		pp = new KeyNamePair(rs.getInt("M_Product_ID"), rs.getString("ProductValue"));
	    		line.add(pp);
	    		line.add(rs.getString("ProductName"));
	    		pp = new KeyNamePair(rs.getInt("M_Forecast_ID"), rs.getString("DocumentNo"));
	    		line.add(pp);
	    		pp = new KeyNamePair(rs.getInt("M_ForecastLine_ID"), rs.getString("Line"));
	    		line.add(pp);
	    		line.add(rs.getTimestamp("DatePromised"));
	    		pp = new KeyNamePair(rs.getInt("C_DocType_ID"), rs.getString("Name"));
	    		line.add(pp);
	    		line.add(rs.getString("TypeForecast"));
	    		pp = new KeyNamePair(rs.getInt("C_Campaign_ID"), rs.getString("CampaignName"));
	    		line.add(pp);
	    		line.add(rs.getBigDecimal("PriceEntered"));
	    		line.add(rs.getBigDecimal("Discount"));
	    		pp = new KeyNamePair(rs.getInt("C_Tax_ID"), rs.getString("TaxName"));
	    		line.add(pp);
	    		line.add(rs.getBigDecimal("LineNetAmt"));
	    		
	    		data.add(line);
		    }		    
	    }catch(Exception e){
	    	log.log(Level.SEVERE, sqlStmt.toString());
	    }finally{
	    	DB.close(rs, pstmt);
	    	pstmt = null;
	    	rs = null;
	    }
	    
	    return data;
	}	
	
	protected void configureMiniTable (IMiniTable miniTable)
	{
		miniTable.setColumnClass(0, Boolean.class, false);     //  Selection
		miniTable.setColumnClass(1, BigDecimal.class, false);  //  Qty
		miniTable.setColumnClass(2, String.class, true);       //  UOM
		miniTable.setColumnClass(3, String.class, true);   	   //  Product Value
		miniTable.setColumnClass(4, String.class, true);       //  Product Name
		if(!isSOTrx && isSubcont){
			miniTable.setColumnClass(5, String.class, true);   	   //  PP_Order_ID
			miniTable.setColumnClass(6, String.class, true);   	   //  PP_Order_BOMLine_ID
			miniTable.setColumnClass(7, String.class, true);   	   //  PP_Order_BOMLine_ID
			miniTable.setColumnClass(8, Timestamp.class, true);    //  DateOrdered
			miniTable.setColumnClass(9, String.class, true);       //  C_DocType_ID
			miniTable.setColumnClass(10, BigDecimal.class, true);   //  PriceList
		}else{
			miniTable.setColumnClass(5, String.class, true);       //  M_Forecast_ID
			miniTable.setColumnClass(6, String.class, true);	   //  M_ForecastLine_ID
			miniTable.setColumnClass(7, Timestamp.class, true);    //  DatePromised
			miniTable.setColumnClass(8, String.class, true);       //  C_DocType_ID
			miniTable.setColumnClass(9, String.class, true);       //  ForecastType
			miniTable.setColumnClass(10, String.class, true);       //  C_Campaign_ID
			miniTable.setColumnClass(11, BigDecimal.class, true);  //  PriceEntered
			miniTable.setColumnClass(12, BigDecimal.class, true);  //  Discount
			miniTable.setColumnClass(13, String.class, true);      //  C_Tax_ID
			miniTable.setColumnClass(14, BigDecimal.class, true);  //  LineNetAmt
		}
		
		//  Table UI
		miniTable.autoSize();		
	}
	
	protected Vector<String> getOISColumnNames()
	{
		//  Header Info
	    Vector<String> columnNames = new Vector<String>(14);
	    columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
	    columnNames.add(Msg.translate(Env.getCtx(), "Quantity"));
	    columnNames.add(Msg.translate(Env.getCtx(), "C_UOM_ID"));
	    columnNames.add("Product Key");
	    columnNames.add("Product Name");
	    if(!isSOTrx && isSubcont){
	    	columnNames.add(Msg.getElement(Env.getCtx(), "PP_Order_ID", false));
		    columnNames.add(Msg.getElement(Env.getCtx(), "PP_Order_BOMLine_ID", false));
		    columnNames.add("Sample Sheet");
		    columnNames.add(Msg.getElement(Env.getCtx(), "DateOrdered"));
		    columnNames.add(Msg.translate(Env.getCtx(), "C_DocType_ID"));
		    columnNames.add(Msg.translate(Env.getCtx(), "PriceList"));
	    	
	    }else{
	    	columnNames.add(Msg.getElement(Env.getCtx(), "M_Forecast_ID", false));
		    columnNames.add(Msg.getElement(Env.getCtx(), "M_ForecastLine_ID", false));
		    columnNames.add(Msg.getElement(Env.getCtx(), "DatePromised"));
		    columnNames.add(Msg.translate(Env.getCtx(), "C_DocType_ID"));
		    columnNames.add(Msg.translate(Env.getCtx(), "TypeForecast"));
		    columnNames.add(Msg.translate(Env.getCtx(), "Unit Bisnis"));
		    columnNames.add(Msg.translate(Env.getCtx(), "PriceEntered"));
		    columnNames.add(Msg.translate(Env.getCtx(), "Discount"));
		    columnNames.add(Msg.translate(Env.getCtx(), "C_Tax_ID"));
		    columnNames.add(Msg.translate(Env.getCtx(), "LineNetAmt"));
	    }
	    
	    return columnNames;
	}
	
	
	@Override
	public void info(IMiniTable miniTable, IStatusBar statusBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save(IMiniTable miniTable, String trxName) {
		int C_Order_ID = ((Integer) getGridTab().getValue("C_Order_ID")).intValue();
		MOrder order = new MOrder(Env.getCtx(), C_Order_ID, trxName);
		if (log.isLoggable(Level.CONFIG)) log.config(order.toString());
		int M_RequisitionLine_ID = 0;

		// Lines
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue()) {
				BigDecimal Qty = (BigDecimal)miniTable.getValueAt(i, 1);
				KeyNamePair pp = (KeyNamePair)miniTable.getValueAt(i, 2);
				int C_UOM_ID = pp.getKey();
				pp = (KeyNamePair)miniTable.getValueAt(i, 3);
				int M_Product_ID = pp.getKey();
				if(!isSOTrx && isSubcont){
					pp = (KeyNamePair)miniTable.getValueAt(i, 6);
					int PP_Order_BOMLine_ID = pp.getKey();
					BigDecimal PriceList = (BigDecimal)miniTable.getValueAt(i, 10);
					
					MOrderLine orderLine = new MOrderLine(order);
					orderLine.setM_Product_ID(M_Product_ID, C_UOM_ID);
					orderLine.setQty(Qty);
					orderLine.setPrice(PriceList);
					orderLine.set_ValueOfColumn("PP_Order_BOMLine_ID", PP_Order_BOMLine_ID);
					orderLine.saveEx(trxName);
				}else{
					pp = (KeyNamePair)miniTable.getValueAt(i, 5);
					int M_Forecast_ID = pp.getKey();
					pp = (KeyNamePair)miniTable.getValueAt(i, 6);
					int M_ForecastLine_ID = pp.getKey();
					MForecastLine forecastLine = null;
					if (M_ForecastLine_ID>0){
						forecastLine = new MForecastLine(Env.getCtx(), M_ForecastLine_ID, trxName);
						if(forecastLine.get_ValueAsInt("M_RequisitionLine_ID")>0)
							M_RequisitionLine_ID = forecastLine.get_ValueAsInt("M_RequisitionLine_ID");					
					}
					//Timestamp DatePromised = (Timestamp)miniTable.getValueAt(i, 7);
					BigDecimal PriceEntered = (BigDecimal)miniTable.getValueAt(i, 11);
					BigDecimal Discount = (BigDecimal)miniTable.getValueAt(i, 12);
					int C_Tax_ID = DB.getSQLValue(trxName, "SELECT C_Tax_ID FROM C_Tax WHERE AD_Client_ID=? AND IsDefault='Y'", AD_Client_ID);
					pp = (KeyNamePair)miniTable.getValueAt(i, 13);
					if (pp.getKey()>0)
						C_Tax_ID = pp.getKey();
					
					int precision = 2;
					if (M_Product_ID != 0)
					{
						MProduct product = MProduct.get(Env.getCtx(), M_Product_ID);
						precision = product.getUOMPrecision();
					}
					Qty = Qty.setScale(precision, RoundingMode.HALF_DOWN);
					//
					if (log.isLoggable(Level.FINE)) log.fine("Line QtyEntered=" + Qty
							+ ", Product=" + M_Product_ID 
							+ ", ForecastLine=" + M_ForecastLine_ID
							+ ", RequisitionLine=" + M_RequisitionLine_ID);
					
					MOrderLine orderLine = new MOrderLine(order);
					orderLine.setM_Product_ID(M_Product_ID, C_UOM_ID);
					orderLine.setQty(Qty);
					if (M_RequisitionLine_ID>0){
						orderLine.set_ValueOfColumn("M_RequisitionLine_ID", M_RequisitionLine_ID);
						MRequisitionLine reqLine = new MRequisitionLine(Env.getCtx(), M_RequisitionLine_ID, trxName);				
						orderLine.setDescription(reqLine.getDescription());
						if (reqLine.get_ValueAsInt("C_Activity_ID")>0)
							orderLine.setC_Activity_ID(reqLine.get_ValueAsInt("C_Activity_ID"));
						if(reqLine.getM_AttributeSetInstance_ID()>0)
							orderLine.setM_AttributeSetInstance_ID(reqLine.getM_AttributeSetInstance_ID());
					}
					
					if (M_ForecastLine_ID>0){
						MForecast forecast = new MForecast(Env.getCtx(), M_Forecast_ID, trxName);
						orderLine.set_ValueOfColumn("C_Campaign_ID", forecast.get_ValueAsInt("C_Campaign_ID"));
						orderLine.set_ValueOfColumn("M_ForecastLine_ID", M_ForecastLine_ID);
						orderLine.setM_AttributeSetInstance_ID(forecastLine.get_ValueAsInt("M_AttributeSetInstance_ID"));
					}
					
					if (C_Tax_ID>0)
						orderLine.setC_Tax_ID(C_Tax_ID);
					
					orderLine.setPrice(PriceEntered);
					orderLine.setPriceList(PriceEntered);
					orderLine.setDiscount(Discount);
					BigDecimal discountAmt = PriceEntered.multiply(Discount.divide(Env.ONEHUNDRED));
					orderLine.setPriceActual(PriceEntered.subtract(discountAmt));
					orderLine.saveEx(trxName);
					
					if (M_RequisitionLine_ID>0){
						MRequisitionLine reqLine = new MRequisitionLine(Env.getCtx(), M_RequisitionLine_ID, trxName);
						reqLine.set_ValueOfColumn("QtyOrdered", reqLine.getQtyOrdered().add(Qty));
						reqLine.saveEx(trxName);
					}
				}
			}
		}
		return true;
	}

}
