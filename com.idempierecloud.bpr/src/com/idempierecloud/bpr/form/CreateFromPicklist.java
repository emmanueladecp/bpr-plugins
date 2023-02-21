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
import org.compiere.model.MInOutLine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Vlayout;

import com.idempierecloud.bpr.model.MBPRPicklistLine;
import com.idempierecloud.bpr.model.X_BPR_Picklist;

public class CreateFromPicklist extends CreateFrom  implements EventListener<Event> {

	private Integer AD_Client_ID;
	private Integer AD_Org_ID;
	protected int C_BPartner_ID = 0;
	private WCreateFromWindow window;
	private boolean m_actionActive = false;
	private int M_InOut_ID;
	

    protected Label orgLabel = new Label();
    protected Listbox orgField = ListboxFactory.newDropdownListbox();
    protected Label shipmentLabel = new Label();
    protected Listbox shipmentField = ListboxFactory.newDropdownListbox();
    protected Label bpartnerLabel = new Label();
    protected Listbox bpartnerField = ListboxFactory.newDropdownListbox();

	public CreateFromPicklist(GridTab mTab) {
		super(mTab);
		AD_Client_ID = (Integer)mTab.getValue("AD_Client_ID");
		AD_Org_ID = (Integer)mTab.getValue("AD_Org_ID");
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
		shipmentLabel.setText(Msg.translate(Env.getCtx(), "M_InOut_ID"));
		bpartnerLabel.setText(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
		
		Vlayout vlayout = new Vlayout();
		vlayout.setVflex("1");
		vlayout.setWidth("100%");
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

		row.appendChild(shipmentLabel.rightAlign());
		row.appendChild(shipmentField);
		shipmentField.setHflex("1");
		
		row.appendChild(bpartnerLabel.rightAlign());
		row.appendChild(bpartnerField);
		bpartnerField.setHflex("1");
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
			initShipmentData();
		}
		
		orgField.setSelectedIndex(idxSelected);
		orgField.setEnabled(false);
	}
	
	private void initShipmentData(){
		window.getWListbox().clear();
		
		shipmentField.removeActionListener(this);
		shipmentField.removeAllItems();
		
		KeyNamePair pp = new KeyNamePair(0, "");
		shipmentField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadShipmentData();
		for (KeyNamePair knp : list){
			shipmentField.addItem(knp);
		}
		
		shipmentField.addActionListener(this);
	}
	
	protected void initBPartner (){
		window.getWListbox().clear();
		
		bpartnerField.removeActionListener(this);
		bpartnerField.removeAllItems();
		
		KeyNamePair pp = new KeyNamePair(0, "");
		bpartnerField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadBPShipment();
		for (KeyNamePair knp : list){
			bpartnerField.addItem(knp);
		}
		
		bpartnerField.addActionListener(this);
	}
	
	protected ArrayList<KeyNamePair> loadShipmentData() {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append(" select r.M_InOut_ID, r.documentNo ")
			.append(" from M_InOut r")
			.append(" join c_order o on r.c_order_id=o.c_order_id")
			.append(" join c_doctype dts on r.c_doctype_id=dts.c_doctype_id")
			.append(" where r.AD_Client_ID=? ")
			.append(" and r.AD_Org_ID=? ")
			.append(" and R.c_doctype_id <> 1000011 ")
			.append(" and r.isSoTrx='Y' ")
			.append(" and dts.ispicklist='Y' ")
			.append(" and case when dts.isshipconfirm='Y' then r.DocStatus in ('IP')")
			.append(" else r.DocStatus in ('CO', 'CL') end")
		    .append(" and exists(select 1 from M_InOutLine rl")
		    .append(" where r.M_InOut_ID=rl.M_InOut_ID")
		    .append(" and not exists(select 1 from BPR_PicklistLine ol")
		    .append(" left join BPR_Picklist o on")
			.append(" ol.BPR_Picklist_ID = o.BPR_Picklist_ID")
			.append(" where rl.M_InOut_ID = ol.M_InOut_ID")
			.append(" and rl.M_Product_ID = ol.M_Product_ID and o.docstatus not in ('VO','RE')))");
		
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
	
	protected ArrayList<KeyNamePair> loadBPShipment() {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append(" select distinct cb.c_bpartner_id, cb.name ")
			.append(" from M_InOut r")
			.append(" join c_order o on r.c_order_id=o.c_order_id")
			.append(" join c_doctype dts on r.c_doctype_id=dts.c_doctype_id")
			.append(" join c_bpartner cb on r.c_bpartner_id = cb.c_bpartner_id")
			.append(" where r.AD_Client_ID=? ")
			.append(" and r.AD_Org_ID=? ")
			.append(" and R.c_doctype_id <> 1000011 ")
			.append(" and r.isSoTrx='Y' ")
			.append(" and dts.ispicklist='Y' ")
			.append(" and case when dts.isshipconfirm='Y' then r.DocStatus in ('IP')")
			.append(" else r.DocStatus in ('CO', 'CL') end")
		    .append(" and exists(select 1 from M_InOutLine rl")
		    .append(" where r.M_InOut_ID=rl.M_InOut_ID")
		    .append(" and not exists(select 1 from BPR_PicklistLine ol")
		    .append(" left join BPR_Picklist o on")
			.append(" ol.BPR_Picklist_ID = o.BPR_Picklist_ID")
			.append(" where rl.M_InOut_ID = ol.M_InOut_ID")
			.append(" and rl.M_Product_ID = ol.M_Product_ID and o.docstatus not in ('VO','RE')))");
		
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
		initBPartner();
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
	protected void loadShipment()
	{
		loadTableOIS(getShipmentLines());
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
			initShipmentData();
		}else if (e.getTarget().equals(shipmentField)){
			KeyNamePair pp = shipmentField.getSelectedItem().toKeyNamePair();
			if (pp!=null)
				M_InOut_ID = pp.getKey();
			else
				M_InOut_ID = 0;

			loadShipment();
		}else if (e.getTarget().equals(bpartnerField)){
			KeyNamePair pp = bpartnerField.getSelectedItem().toKeyNamePair();
			if (pp!=null)
				C_BPartner_ID = pp.getKey();
			else
				C_BPartner_ID = 0;
			loadShipment();
		}
		
		m_actionActive = false;				
	}
	
	protected Vector<Vector<Object>> getShipmentLines()
	{
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    StringBuffer sqlStmt = new StringBuffer();
	    sqlStmt.append(" select rl.m_inoutline_id, r.documentno, rl.qtyentered as qtyentered,");
	    sqlStmt.append(" p.m_product_id, p.value as productvalue, p.name as productname,");
	    sqlStmt.append(" uom.c_uom_id, uom.name as UOMName, rl.movementqty as qty");
	    sqlStmt.append(" from m_inoutline rl");
	    sqlStmt.append(" join m_product p on rl.m_product_id=p.m_product_id");
	    sqlStmt.append(" join c_uom uom on rl.c_uom_id=uom.c_uom_id");
	    sqlStmt.append(" join m_inout r on rl.m_inout_id=r.m_inout_id");
	    sqlStmt.append(" join c_bpartner cb on r.c_bpartner_id = cb.c_bpartner_id");
	    sqlStmt.append(" where r.M_InOut_ID=rl.M_InOut_ID and r.ad_client_id = 1000003");
	    sqlStmt.append(" and not exists(select 1 from BPR_PicklistLine ol");
	    sqlStmt.append(" left join BPR_Picklist o");
		sqlStmt.append(" on ol.BPR_Picklist_ID = o.BPR_Picklist_ID");
		sqlStmt.append(" where rl.M_InOut_ID = ol.M_InOut_ID");
		sqlStmt.append(" and rl.M_Product_ID = ol.M_Product_ID and o.docstatus not in ('VO','RE'))");
		
		if(C_BPartner_ID>0)
			sqlStmt.append(" and r.c_bpartner_id = ?");
		if(M_InOut_ID>0) 
			sqlStmt.append(" and r.m_inout_id=?");
			
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;	    
	    try{
	    	pstmt = DB.prepareStatement(sqlStmt.toString(), null);
	    	if(M_InOut_ID>0)
	    		pstmt.setInt(1, M_InOut_ID);
		    if(M_InOut_ID > 0 && C_BPartner_ID>0) {
		    	pstmt.setInt(2, C_BPartner_ID);
		    }else if(M_InOut_ID == 0 && C_BPartner_ID > 0) {
		    	pstmt.setInt(1, C_BPartner_ID);
		    }
		    rs = pstmt.executeQuery();
		    while (rs.next()){
		    	Vector<Object> line = new Vector<Object>(13);
	    		line.add(false);   	// 0-Selection
	    		line.add(rs.getBigDecimal("Qtyentered"));
	    		KeyNamePair pp = new KeyNamePair(rs.getInt("C_UOM_ID"), rs.getString("UOMName")); // 2-UOM
	    		line.add(pp);
	    		pp = new KeyNamePair(rs.getInt("M_Product_ID"), rs.getString("ProductValue")); //3-Product
	    		line.add(pp);
	    		pp = new KeyNamePair(rs.getInt("M_InOutLine_ID"), rs.getString("ProductName")); //4-RequisitionLine
	    		line.add(pp);
	    		line.add(rs.getBigDecimal("Qty")); 
	    		
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
	    columnNames.add(Msg.translate(Env.getCtx(), "Quantity Entered"));
	    columnNames.add(Msg.translate(Env.getCtx(), "C_UOM_ID"));
	    columnNames.add("Product Key");
	    columnNames.add("Product Name");
	    columnNames.add(Msg.translate(Env.getCtx(), "Quantity"));
	    
	    
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
		int BPR_Picklist_ID = ((Integer) getGridTab().getValue("BPR_Picklist_ID")).intValue();
		X_BPR_Picklist picklist = new X_BPR_Picklist(Env.getCtx(), BPR_Picklist_ID, trxName);
		if (log.isLoggable(Level.CONFIG)) log.config(picklist.toString());
		int M_InOutLine_ID = 0;

		// Lines
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue()) {
				BigDecimal QtyEntered = (BigDecimal)miniTable.getValueAt(i, 1);
				KeyNamePair pp = (KeyNamePair)miniTable.getValueAt(i, 2);
				//int C_UOM_ID = pp.getKey();
				pp = (KeyNamePair)miniTable.getValueAt(i, 3);
				int M_Product_ID = pp.getKey();
				pp = (KeyNamePair)miniTable.getValueAt(i, 4);
				M_InOutLine_ID = pp.getKey();
				BigDecimal Qty = (BigDecimal)miniTable.getValueAt(i, 5);
				MInOutLine shipmentLine = new MInOutLine(picklist.getCtx(), M_InOutLine_ID, picklist.get_TrxName());
				
				MBPRPicklistLine line = new MBPRPicklistLine(picklist.getCtx(), 0, picklist.get_TrxName());
				line.setAD_Org_ID(picklist.getAD_Org_ID());
				line.setLineNo(shipmentLine.getLine());
				line.setM_InOut_ID(shipmentLine.getM_InOut_ID());
				line.setBPR_Picklist_ID(BPR_Picklist_ID);
				line.setM_Product_ID(M_Product_ID);
				line.setMovementQty(Qty);
				line.set_ValueOfColumn("QtyEntered", QtyEntered);
				line.saveEx();
			}
		}
				
		return true;
	}

}
