package com.idempierecloud.bpr.form;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.apps.form.WCreateFromWindow;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.compiere.apps.IStatusBar;
import org.compiere.grid.CreateFrom;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Vlayout;

import com.idempierecloud.bpr.model.MBPRMaterialRequestLine;

public class CreateFromMaterialMovement extends CreateFrom  implements EventListener<Event> {

	private Integer AD_Client_ID;
	private Integer AD_Org_ID;
	private Integer M_Warehouse_ID;
	private Integer M_WarehouseTo_ID;
	private Integer BPR_MaterialRequest_ID;
	private WCreateFromWindow window;
	private boolean m_actionActive = false;
	

    protected Label orgLabel = new Label();
    protected Listbox orgField = ListboxFactory.newDropdownListbox();
    protected Label requestLabel = new Label();
    protected Listbox requestField = ListboxFactory.newDropdownListbox();

	public CreateFromMaterialMovement(GridTab mTab) {
		super(mTab);
		AD_Client_ID = (Integer)mTab.getValue("AD_Client_ID");
		AD_Org_ID = (Integer)mTab.getValue("AD_Org_ID");
		M_Warehouse_ID = (Integer)mTab.getValue("M_Warehouse_ID");
		M_WarehouseTo_ID = (Integer)mTab.getValue("M_WarehouseTo_ID");
		log.info(getGridTab().toString());
		window = new WCreateFromWindow(this, getGridTab().getWindowNo());
		
		try
		{
			if (!dynInit())
				return;
			zkInit();
			setInitOK(true);
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "", e);
			setInitOK(false);
			throw new AdempiereException(e.getMessage());
		}
		AEnv.showWindow(window);
	}

	private void zkInit() throws Exception{
		orgLabel.setText(Msg.translate(Env.getCtx(), "AD_Org_ID"));
		requestLabel.setText(Msg.translate(Env.getCtx(), "BPR_MaterialRequest_ID"));

		Vlayout vlayout = new Vlayout();
		vlayout.setVflex("1");
		vlayout.setWidth("60%");
    	Panel parameterPanel = window.getParameterPanel();
		parameterPanel.appendChild(vlayout);

		Grid parameterStdLayout = GridFactory.newGridLayout();
    	vlayout.appendChild(parameterStdLayout);
		
		Rows rows = (Rows) parameterStdLayout.newRows();
		Row row = null;
		
		row = rows.newRow();
		row.appendChild(orgLabel.rightAlign());
		row.appendChild(orgField);
		orgField.setHflex("1");

		row.appendChild(requestLabel.rightAlign());
		row.appendChild(requestField);
		requestField.setHflex("1");
	}
	
	private void initOrgData(){
		orgField.removeActionListener(this);
		orgField.removeAllItems();
		
		KeyNamePair pp = new KeyNamePair(0, "");
		orgField.addItem(pp);
		
		int idxSelected = 0;
		int idx = 0;
		ArrayList<KeyNamePair> list = loadOrganizationData();
		for (KeyNamePair knp : list){
			orgField.addItem(knp);
			idx++;
			if (knp.getKey()==AD_Org_ID)
				idxSelected = idx;
		}
		if(idxSelected>0) {
			initRequestData();
		}
		
		orgField.setSelectedIndex(idxSelected);
		orgField.setEnabled(false);
	}
	
	private void initRequestData(){
		window.getWListbox().clear();
		
		requestField.removeActionListener(this);
		requestField.removeAllItems();
		
		KeyNamePair pp = new KeyNamePair(0, "");
		requestField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadRequestData();
		for (KeyNamePair knp : list){
			requestField.addItem(knp);
		}
		
		requestField.addActionListener(this);
	}
	
	protected ArrayList<KeyNamePair> loadRequestData() {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append(" select r.BPR_MaterialRequest_ID, r.documentNo ")
			.append(" from BPR_MaterialRequest r")
			.append(" join bpr_materialrequestline bm on r.bpr_materialrequest_id = bm.bpr_materialrequest_id")
			.append(" where r.AD_Client_ID=? ")
			.append(" and r.M_Warehouse_ID=? ")
			.append(" and r.M_WarehouseTo_ID=? ")
			.append(" and r.DocStatus='CO' ")
			.append(" and bm.movementqty -(select coalesce(sum(mm3.movementqty),0)from M_MovementLine mm3 ")
			.append(" join m_movement mm4 on mm3.m_movement_id = mm4.m_movement_id where mm4.docstatus not in ('VO','RE') ")
			.append(" and mm3.bpr_materialrequestline_id=bm.bpr_materialrequestline_id )>0")
			;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setInt(2, M_Warehouse_ID);
			pstmt.setInt(3, M_WarehouseTo_ID);
				
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

	@Override
	public Object getWindow() {
		return window;
	}

	@Override
	public boolean dynInit() throws Exception {
		log.config("");
		
		window.setTitle(getTitle());
		
		initOrgData();
		return true;
	}
	
	/**
	 *  Load Forecast data into Table
	 *  @param data data
	 */
	protected void loadTableOIS (Vector<?> data)
	{
		window.getWListbox().clear();
		
		//  Remove previous listeners
		window.getWListbox().getModel().removeTableModelListener(window);
		//  Set Model
		ListModelTable model = new ListModelTable(data);
		model.addTableModelListener(window);
		window.getWListbox().setData(model, getOISColumnNames());
		//
		
		configureMiniTable(window.getWListbox());
	}	
	
	protected void configureMiniTable (IMiniTable miniTable)
	{
		miniTable.setColumnClass(0, Boolean.class, false);     //  Selection
		miniTable.setColumnClass(1, BigDecimal.class, false);  //  Qty
		miniTable.setColumnClass(2, String.class, true);       //  UOM
		miniTable.setColumnClass(3, String.class, true);   	   //  Product Value
		miniTable.setColumnClass(4, String.class, true);       //  Product Name
		
		//  Table UI
		miniTable.autoSize();		
	}
	
	/**
	 *  Load Data - Requisition
	 */
	protected void loadRequest()
	{
		loadTableOIS(getRequestLines());
	}
	
	@Override
	public void onEvent(Event e) throws Exception {
		if (m_actionActive)
			return;
		m_actionActive = true;
		
		if (e.getTarget().equals(orgField)){
			KeyNamePair pp = orgField.getSelectedItem().toKeyNamePair();
			if (pp!=null)
				AD_Org_ID = pp.getKey();
			else 
				AD_Org_ID = 0;
			initRequestData();
		}else if (e.getTarget().equals(requestField)){
			KeyNamePair pp = requestField.getSelectedItem().toKeyNamePair();
			if (pp!=null)
				BPR_MaterialRequest_ID = pp.getKey();
			else
				BPR_MaterialRequest_ID = 0;

			loadRequest();
		}
		
		m_actionActive = false;				
	}
	
	protected Vector<Vector<Object>> getRequestLines()
	{
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    StringBuffer sqlStmt = new StringBuffer();
	    sqlStmt.append(" select rl.bpr_materialrequestline_id, r.documentno,");
	    sqlStmt.append(" rl.movementqty - (select coalesce(sum(mm3.movementqty),0)from M_MovementLine mm3 join m_movement mm4 on mm3.m_movement_id = mm4.m_movement_id");
	    sqlStmt.append(" where mm4.docstatus not in ('VO','RE') and mm3.bpr_materialrequestline_id=rl.bpr_materialrequestline_id ) as qty ,");
	    sqlStmt.append(" p.name as productname, w.value as locator");
	    sqlStmt.append(" from bpr_materialrequestline rl");
	    sqlStmt.append(" join bpr_materialrequest r on rl.bpr_materialrequest_id=r.bpr_materialrequest_id");
	    sqlStmt.append(" join m_product p on rl.m_product_id=p.m_product_id");
	    sqlStmt.append(" join m_locator w on rl.m_locatortoalias_id = w.m_locator_id");
	    sqlStmt.append(" where r.m_warehouse_id=? and r.m_warehouseto_id=? ");
	    sqlStmt.append(" and rl.movementqty -(select coalesce(sum(mm2.movementqty),0)from M_MovementLine mm2 ");
	    sqlStmt.append(" join m_movement mm on mm2.m_movement_id = mm.m_movement_id where mm.docstatus not in ('VO','RE') and mm2.bpr_materialrequestline_id=rl.bpr_materialrequestline_id )>0 ");
	    if(BPR_MaterialRequest_ID>0)
	    	sqlStmt.append(" and r.bpr_materialrequest_id=?");
	    
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;	    
	    try{
	    	pstmt = DB.prepareStatement(sqlStmt.toString(), null);
	    	pstmt.setInt(1, M_Warehouse_ID);
	    	pstmt.setInt(2, M_WarehouseTo_ID);
	    	if(BPR_MaterialRequest_ID>0)
		    	pstmt.setInt(3, BPR_MaterialRequest_ID);
	    		
		    rs = pstmt.executeQuery();
		    while (rs.next()){
		    	Vector<Object> line = new Vector<Object>(13);
	    		line.add(false);   	// 0-Selection
	    		line.add(rs.getBigDecimal("Qty"));  // 1-Qty
	    		line.add(rs.getString("productname")); // 2-Product
	    		line.add(rs.getString("locator")); // 3-locator
	    		KeyNamePair kp = new KeyNamePair(rs.getInt("bpr_materialrequestline_id"), rs.getString("documentno"));
	    		line.add(kp); //4-request
	    		
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
	
	protected Vector<String> getOISColumnNames()
	{
		//  Header Info
	    Vector<String> columnNames = new Vector<String>(14);
	    columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
	    columnNames.add(Msg.translate(Env.getCtx(), "Quantity"));
	    columnNames.add("Product");
	    columnNames.add("Locator");
	    columnNames.add("Request");
	    
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
		
		statusBar.setStatusLine("Qty  "+qty);
	}

	@Override
	public boolean save(IMiniTable miniTable, String trxName) {
		int M_Movement_ID = ((Integer) getGridTab().getValue("M_Movement_ID")).intValue();
		MMovement movement = new MMovement(Env.getCtx(), M_Movement_ID, trxName);
		if (log.isLoggable(Level.CONFIG)) log.config(movement.toString());
		int BPR_MaterialRequestLine_ID = 0;

		// Lines
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue()) {
				BigDecimal Qty = (BigDecimal)miniTable.getValueAt(i, 1);
				KeyNamePair pp = (KeyNamePair)miniTable.getValueAt(i, 4);
				BPR_MaterialRequestLine_ID = pp.getKey();
				MBPRMaterialRequestLine requestLine = new MBPRMaterialRequestLine(movement.getCtx(), BPR_MaterialRequestLine_ID, movement.get_TrxName());
				
				MMovementLine line = new MMovementLine(movement.getCtx(), 0, movement.get_TrxName());
				line.setM_Movement_ID(movement.getM_Movement_ID());
				line.setAD_Org_ID(movement.getAD_Org_ID());
				line.setM_Product_ID(requestLine.getM_Product_ID());
				int m_locator_ID = DB.getSQLValue(movement.get_TrxName(), "select m_locator_id from m_locator ml where m_locatortype_id in (select m_locatortype_id from m_locatortype ml2 where name='Customer Shipment') and m_warehouse_id = ?", M_Warehouse_ID);
				line.setM_Locator_ID(m_locator_ID);
				int m_locatorTo_ID = DB.getSQLValue(movement.get_TrxName(), "select m_locator_id from m_locator ml where m_locatortype_id in (select m_locatortype_id from m_locatortype ml2 where name='INTRANSIT') and m_warehouse_id = ?", M_Warehouse_ID);
				line.setM_LocatorTo_ID(m_locatorTo_ID);
				line.set_ValueOfColumn("M_LocatorToAlias_ID", requestLine.getM_LocatorToAlias_ID());
				line.setMovementQty(Qty);
				line.set_ValueOfColumn("bpr_materialrequestline_id", requestLine.getBPR_MaterialRequestLine_ID());
				line.saveEx();
				requestLine.saveEx();
				
			}
		}
				
		return true;
	}

}
