package com.idempierecloud.bpr.form;

import static org.compiere.model.SystemIDs.COLUMN_C_PERIOD_AD_ORG_ID;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.editor.WDateEditor;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.MInOutConfirm;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MRole;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.util.TrxRunnable;
import org.compiere.util.Util;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Div;
import org.zkoss.zul.North;
import org.zkoss.zul.South;

import com.idempierecloud.bpr.base.CustomForm;

public class CompleteMovementConfirmUI extends CustomForm implements ValueChangeListener, WTableModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1953050531417564062L;

	public static final CLogger log = CLogger.getCLogger(Allocation.class);

	private Borderlayout mainLayout = new Borderlayout();
	private Panel parameterPanel = new Panel();
	private Grid parameterLayout = GridFactory.newGridLayout();
	private WListbox confirmTable = ListboxFactory.newDataTable();

	private Label dateLabel = new Label();
	private WDateEditor dateField = new WDateEditor();
	private Label organizationLabel = new Label();
	private WTableDirEditor organizationPick;

	private int m_AD_Org_ID;

	private Button processBtn = new Button();

	private Timestamp m_MovementDate;
	
	@Override
	protected void initForm() {
		dynInit();
		zkInit();
		
		loadShipmentConfirm();
	}

	private void loadShipmentConfirm() {
		processBtn.setEnabled(false);
		
		Vector<Vector<Object>> data = getShipmentConfirm();
		Vector<String> columnNames = getColumnNames();
		
		confirmTable.clear();
		
		//  Remove previous listeners
		confirmTable.getModel().removeTableModelListener(this);
		
		//  Set Model
		ListModelTable modelI = new ListModelTable(data);
		modelI.addTableModelListener(this);
		confirmTable.setData(modelI, columnNames);
		setColumnClass(confirmTable);
	}

	public Vector<String> getColumnNames()
	{
		//  Header Info
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add("Shipment Confirm");
		columnNames.add("Shipment");
		columnNames.add("Movement Date");
		
		return columnNames;
	}
	
	public void setColumnClass(IMiniTable invoiceTable)
	{
		int i = 0;
		invoiceTable.setColumnClass(i++, Boolean.class, false);         //  0-Selection
		invoiceTable.setColumnClass(i++, String.class, true);           //  1-Shipment Confirm
		invoiceTable.setColumnClass(i++, String.class, true);           //  2-Shipment
		invoiceTable.setColumnClass(i++, Timestamp.class, true);        //  3-Movement Date
		invoiceTable.autoSize();
	}

	private Vector<Vector<Object>> getShipmentConfirm() {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuilder sql = new StringBuilder(""
			+ " select  c.m_inoutconfirm_id , c.documentno as confirmno,"
			+ " s.m_inout_id , s.documentno as shipmentno, s.movementdate"
			+ " from m_inoutconfirm c"
			+ " join m_inout s on c.m_inout_id =s.m_inout_id"
			+ " where c.docstatus in ('DR')"
			+ " AND c.AD_Org_ID=?");
		
		if(m_MovementDate!=null)
			sql.append(" AND s.movementDate=?");
		
		sql.append(" ORDER BY c.documentno");
		if (log.isLoggable(Level.FINE)) log.fine("SQL=" + sql.toString());
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1,m_AD_Org_ID);
			if(m_MovementDate!=null)
				pstmt.setTimestamp(2, m_MovementDate);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				Vector<Object> line = new Vector<Object>();
				line.add(Boolean.FALSE);       //  0-Selection
				KeyNamePair pp = new KeyNamePair(rs.getInt(1), rs.getString(2));
				line.add(pp);                       //  1-DocumentNo
				pp = new KeyNamePair(rs.getInt(3), rs.getString(4));
				line.add(pp);                       //  2-BPartner
				line.add(rs.getTimestamp(5));       //  3-TrxDate
				
				data.add(line);
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
		}
		
		return data;
	}

	private void dynInit() {
		// Organization filter selection
		int AD_Column_ID = COLUMN_C_PERIOD_AD_ORG_ID; //C_Period.AD_Org_ID (needed to allow org 0)
		MLookup lookupOrg = MLookupFactory.get(Env.getCtx(), getWindowNo(), 0, AD_Column_ID, DisplayType.TableDir);
		organizationPick = new WTableDirEditor("AD_Org_ID", true, false, true, lookupOrg);
		m_AD_Org_ID = Env.getAD_Org_ID(Env.getCtx());
		organizationPick.setValue(m_AD_Org_ID);
		organizationPick.addValueChangeListener(this);
		
		dateField.addValueChangeListener(this);
		
		processBtn.setEnabled(false);
	}

	private void zkInit() {
		Div div = new Div();
		div.setStyle("height: 100%; width: 100%; overflow: auto;");
		div.appendChild(mainLayout);
		appendChild(div);
		ZKUpdateUtil.setWidth(mainLayout, "100%");
		

		dateLabel.setText(Msg.translate(Env.getCtx(), "Movement Date"));
		organizationLabel.setText(Msg.translate(Env.getCtx(), "AD_Org_ID"));
		processBtn.setLabel(Util.cleanAmp(Msg.getMsg(Env.getCtx(), "Process")));
		processBtn.addActionListener(this);
		
		dateField.setMandatory(true);
		
		parameterPanel.appendChild(parameterLayout);
		
		// parameters layout
		North north = new North();
		north.setBorder("none");
		north.setSplittable(true);
		north.setCollapsible(true);
		mainLayout.appendChild(north);
		north.appendChild(parameterPanel);

		Columns columns = new Columns();
		Column column = new Column();
		column.setWidth("25%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("25%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("25%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("25%");
		columns.appendChild(column);
		parameterLayout.appendChild(columns);
		
		Rows rows = parameterLayout.newRows();
		Row row = rows.newRow();
		row.appendCellChild(organizationLabel.rightAlign());
		ZKUpdateUtil.setHflex(organizationPick.getComponent(), "true");
		row.appendCellChild(organizationPick.getComponent(),1);
		organizationPick.showMenu();
		row.appendChild(dateLabel.rightAlign());
		row.appendChild(dateField.getComponent());
		
		Center center = new Center();
		mainLayout.appendChild(center);
		center.appendChild(confirmTable);
		
		South south = new South();
		south.setBorder("none");
		mainLayout.appendChild(south);
		south.appendChild(processBtn);
	}

	@Override
	public void valueChange(ValueChangeEvent e) {
		String name = e.getPropertyName();
		Object value = e.getNewValue();
		if (log.isLoggable(Level.CONFIG)) log.config(name + "=" + value);
		if (value == null)
			return;
		
		// Organization
		if (name.equals("AD_Org_ID"))
		{
			m_AD_Org_ID = ((Integer) value).intValue();
			
			loadShipmentConfirm();
		}
		else if (name.equals("Date"))
		{
			m_MovementDate = (Timestamp) value;
			
			loadShipmentConfirm();
		}
	}
	
	public void onEvent(Event e)
	{
		log.config("");
		if (e.getTarget().equals(processBtn))
		{
			processBtn.setEnabled(false);
			try{
				Trx.run(new TrxRunnable() 
				{
					public void run(String trxName)
					{
						completeConfirm(trxName);
					}
				});
			}catch(Exception ex) {
				FDialog.error(getWindowNo(), this, "Error", ex.getLocalizedMessage());
				return;
			}

			loadShipmentConfirm();
		}
		
	}

	private void completeConfirm(String trxName) {	
			
		for (int i = 0; i < confirmTable.getRowCount(); i++)
		{
			if (((Boolean)confirmTable.getValueAt(i, 0)).booleanValue())
			{
				KeyNamePair pp = (KeyNamePair)confirmTable.getValueAt(i, 1);
				int M_InOutConfirm_ID = pp.getKey();
				MInOutConfirm confirm = new MInOutConfirm(Env.getCtx(), M_InOutConfirm_ID, trxName);
				if(!confirm.processIt(MInOutConfirm.ACTION_Complete)) {
					FDialog.error(getWindowNo(), this, "Error", confirm.getProcessMsg());
					return;
				}
				
				confirm.saveEx();
			}
		}
	}

	@Override
	public void tableChanged(WTableModelEvent event) {
		int count = 0;
		for (int i = 0; i < confirmTable.getRowCount(); i++)
		{
			if (((Boolean)confirmTable.getValueAt(i, 0)).booleanValue())
				count++;
		}
		
		processBtn.setEnabled(count>0);
	}

}
