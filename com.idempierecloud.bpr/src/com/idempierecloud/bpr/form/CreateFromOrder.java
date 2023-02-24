package com.idempierecloud.bpr.form;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import org.compiere.apps.IStatusBar;
import org.compiere.grid.CreateFrom;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MRequisitionLine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

public class CreateFromOrder extends CreateFrom {

	/**  Loaded Forecast         */
	protected int AD_Client_ID = 0;
	protected int AD_Org_ID = 0;
	protected int M_Requisition_ID = 0;
	protected int C_BPartner_ID = 0;
	protected String NotaTimbangan = null;
	protected boolean isSOTrx = true;
	protected int M_Locator_ID = 0;
	
	int C_Order_ID = ((Integer) getGridTab().getValue("C_Order_ID")).intValue();
	MOrder order = new MOrder(Env.getCtx(), C_Order_ID, null);


    String isturus = DB.getSQLValueString(order.get_TrxName(), "select coalesce (cd.isturus,'N') from c_order co "
    		+ " join c_doctype cd on co.c_doctypetarget_id = cd.c_doctype_id where c_order_id = ?", C_Order_ID);
    int isRMP = DB.getSQLValue(order.get_TrxName(), "select count(AD_Client_ID) from ad_client ac where value like 'RMP' and ad_client_id =?", AD_Client_ID);
	
	public CreateFromOrder(GridTab mTab) {
		super(mTab);
		if (log.isLoggable(Level.INFO)) log.info(mTab.toString());
	}

	@Override
	public Object getWindow() {
		return null;
	}

	@Override
	public boolean dynInit() throws Exception {
		log.config("");
		setTitle(Msg.getElement(Env.getCtx(), "C_Order_ID", false) + " .. " + Msg.translate(Env.getCtx(), "CreateFrom"));
		AD_Client_ID = ((Integer) getGridTab().getValue("AD_Client_ID")).intValue();
		AD_Org_ID = ((Integer) getGridTab().getValue("AD_Org_ID")).intValue();
		isSOTrx = getGridTab().getValueAsBoolean("isSOTrx");
		return true;
	}

	/**
	 *  Load Organization Field.
	 */	
	protected ArrayList<KeyNamePair> loadOrganizationData() {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append("SELECT AD_Org_ID, Name AS OrganizationName FROM AD_Org WHERE isActive='Y' and AD_Client_ID=?");
		
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
	
	protected ArrayList<KeyNamePair> loadLocatorData() {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append("SELECT M_Locator_ID, Value FROM M_Locator WHERE isActive='Y' and AD_Org_ID=?");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			pstmt.setInt(1, AD_Org_ID);
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
	protected ArrayList<KeyNamePair> loadRequisitionData() {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer sqlStmt = new StringBuffer();
		if(isturus.equals("Y")&&isRMP<1) {
			sqlStmt.append(" select r.M_Requisition_ID, r.documentNo || ' - ' || r.DateRequired as DocumentNo");
		}else {
			sqlStmt.append(" select r.M_Requisition_ID, r.documentNo || ' - ' || r.DateRequired as DocumentNo");
		}
		sqlStmt.append(" from M_Requisition r")
		    .append(" left join bpr_timbangan t on t.bpr_timbangan_id=r.bpr_timbangan_id")
			.append(" where r.AD_Client_ID=? ")
			.append(" and r.AD_Org_ID=? ")
			.append(" and r.DocStatus IN ('CO','CL') ")
		    .append(" and exists(select 1 from M_Requisitionline rl")
		    .append(" left join c_orderline ol on rl.c_orderline_id=ol.c_orderline_id")
		    .append(" left join c_order o on ol.c_order_id=o.c_order_id")
		    .append(" where r.isactive = 'Y' AND r.m_requisition_id=rl.m_requisition_id")
		    .append(" and rl.c_orderline_id is null or o.docstatus in ('VO','RE'))")
		;

	    if(NotaTimbangan!=null && !NotaTimbangan.isEmpty())
	    	sqlStmt.append(" and t.value like '%"+NotaTimbangan+"%'");
	    String isturus = DB.getSQLValueString(order.get_TrxName(), "select coalesce (cd.isturus,'N') from c_order co "
	    		+ " join c_doctype cd on co.c_doctypetarget_id = cd.c_doctype_id where c_order_id = ?", C_Order_ID);
		int isRMP = DB.getSQLValue(order.get_TrxName(), "select count(AD_Client_ID) from ad_client ac where value like 'RMP' and ad_client_id =?", AD_Client_ID);
	    if(isturus.equals("Y")&&isRMP<1) {
	    	if(C_BPartner_ID>0)
				sqlStmt.append(" and r.c_bpartner_id=? ");
	    	sqlStmt.append(" and r.C_DocType_ID=1000088 ");//doctype PR Bahan Baku
	    	sqlStmt.append(" order by r.DateRequired DESC");
	    }
	    else {
	    	sqlStmt.append(" and r.C_DocType_ID<>1000088 ");
	    }
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			int index = 1;
			pstmt.setInt(index++, AD_Client_ID);
			pstmt.setInt(index++, AD_Org_ID);
			if(isturus.equals("Y")&&isRMP<1) {
				if(C_BPartner_ID>0)
					pstmt.setInt(index++, C_BPartner_ID);
			}
				
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
	
	protected Vector<Vector<Object>> getRequisitionData()
	{
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    StringBuffer sqlStmt = new StringBuffer();
	    sqlStmt.append(" select rl.m_requisitionline_id, rl.line || ' - ' || r.documentno as documentno, rl.qty,");
	    sqlStmt.append(" p.m_product_id, p.value as productvalue, p.name as productname,");
	    sqlStmt.append(" uom.c_uom_id, uom.name as UOMName");
	    sqlStmt.append(" from m_requisitionline rl");
	    sqlStmt.append(" join m_product p on rl.m_product_id=p.m_product_id");
	    sqlStmt.append(" join c_uom uom on rl.c_uom_id=uom.c_uom_id");
	    sqlStmt.append(" join m_requisition r on rl.m_requisition_id=r.m_requisition_id");
	    sqlStmt.append(" left join c_orderline ol on rl.c_orderline_id=ol.c_orderline_id");
	    sqlStmt.append(" left join c_order o on ol.c_order_id=o.c_order_id");
	    sqlStmt.append(" left join bpr_timbangan t on t.bpr_timbangan_id=r.bpr_timbangan_id");
	    sqlStmt.append(" where (rl.c_orderline_id is null or o.docstatus in ('VO','RE'))");

	    if(NotaTimbangan!=null && !NotaTimbangan.isEmpty())
	    	sqlStmt.append(" and t.value like '%"+NotaTimbangan+"%'");
	    
	    sqlStmt.append(" and r.isactive = 'Y' and rl.isactive='Y' and r.docstatus in ('CO') and r.ad_client_id = "+AD_Client_ID);
	    if(M_Requisition_ID>0) {
	    	sqlStmt.append(" and r.m_requisition_id=?");
	    }
	    if(isturus.equals("Y")&&isRMP<1) {
	    	if(C_BPartner_ID>0) {
	    		sqlStmt.append(" and r.c_bpartner_id=?");
	    	}
	    	sqlStmt.append(" and r.C_DocType_ID=1000088 ");//doctype PR Bahan Baku	  
	    }
	    else {
	    	sqlStmt.append(" and r.C_DocType_ID<>1000088 ");
	    }
 
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;	    
	    try{
	    	pstmt = DB.prepareStatement(sqlStmt.toString(), null);
	    	int index = 1;
	    	if(M_Requisition_ID>0)
	    		pstmt.setInt(index++, M_Requisition_ID);
	    	if(isturus.equals("Y")&&isRMP<1) {
	    		if(C_BPartner_ID>0)
	    			pstmt.setInt(index++, C_BPartner_ID);
	    	}
	    	
		    rs = pstmt.executeQuery();
		    while (rs.next()){
		    	Vector<Object> line = new Vector<Object>(13);
	    		line.add(false);   	// 0-Selection
	    		line.add(rs.getBigDecimal("Qty"));  // 1-Qty
	    		KeyNamePair pp = new KeyNamePair(rs.getInt("C_UOM_ID"), rs.getString("UOMName")); // 2-UOM
	    		line.add(pp);
	    		pp = new KeyNamePair(rs.getInt("M_Product_ID"), rs.getString("ProductValue")); //3-Product
	    		line.add(pp);
	    		pp = new KeyNamePair(rs.getInt("M_RequisitionLine_ID"), rs.getString("ProductName")); //4-RequisitionLine
	    		line.add(pp);
	    		line.add(rs.getString("DocumentNo")); //5-Requisition
	    		
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
		miniTable.setColumnClass(5, String.class, true);       //  Requisition
		
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
	    columnNames.add(Msg.translate(Env.getCtx(), "M_Requisition_ID"));
	    
	    return columnNames;
	}
	
	
	@Override
	public void info(IMiniTable miniTable, IStatusBar statusBar) {
		BigDecimal qty = Env.ZERO;
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue()) {
				qty = qty.add((BigDecimal)miniTable.getValueAt(i, 1));
			}
		}
		statusBar.setStatusLine("Selected Qty "+qty);
	}

	@Override
	public boolean save(IMiniTable miniTable, String trxName) {
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
				pp = (KeyNamePair)miniTable.getValueAt(i, 4);
				M_RequisitionLine_ID = pp.getKey();
				MRequisitionLine reqLine = new MRequisitionLine(order.getCtx(), M_RequisitionLine_ID, order.get_TrxName());
				
				MOrderLine line = new MOrderLine(order);
				line.setM_Product_ID(M_Product_ID);
				line.setQty(Qty);
				line.setC_UOM_ID(C_UOM_ID);
				line.setPrice();
				if(reqLine.get_ValueAsInt("RelatedProduct_ID")>0)
					line.set_ValueOfColumn("RelatedProduct_ID", reqLine.get_Value("RelatedProduct_ID"));
				line.set_ValueOfColumn("QtyPack", reqLine.get_Value("QtyPack"));
				line.setM_AttributeSetInstance_ID(0);
				if(M_Locator_ID>0)
					line.set_ValueOfColumn("M_Locator_ID", M_Locator_ID);
				line.saveEx();
				
				reqLine.setC_OrderLine_ID(line.getC_OrderLine_ID());
				reqLine.saveEx();
			}
		}
		return true;
	}

}
