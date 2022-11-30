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
import org.compiere.model.MInvoice;
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

public class TandaTerimaFakturUI extends CustomForm implements ValueChangeListener, WTableModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1953050531417564062L;

	public static final CLogger log = CLogger.getCLogger(Allocation.class);

	private Borderlayout mainLayout = new Borderlayout();
	private Panel parameterPanel = new Panel();
	private Grid parameterLayout = GridFactory.newGridLayout();
	private WListbox invoiceTable = ListboxFactory.newDataTable();

	private Label dateLabel = new Label();
	private WDateEditor dateField = new WDateEditor();
	private Label organizationLabel = new Label();
	private WTableDirEditor organizationPick;

	private int m_AD_Org_ID;

	private Button processBtn = new Button();
	
	@Override
	protected void initForm() {
		dynInit();
		zkInit();
		
		loadInvoice();
	}

	private void loadInvoice() {
		Vector<Vector<Object>> data = getInvoiceData();
		Vector<String> columnNames = getInvoiceColumnNames();
		
		invoiceTable.clear();
		
		//  Remove previous listeners
		invoiceTable.getModel().removeTableModelListener(this);
		
		//  Set Model
		ListModelTable modelI = new ListModelTable(data);
		modelI.addTableModelListener(this);
		invoiceTable.setData(modelI, columnNames);
		setInvoiceColumnClass(invoiceTable);
	}

	public Vector<String> getInvoiceColumnNames()
	{
		//  Header Info
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add(Msg.getElement(Env.getCtx(), "DateInvoiced"));
		columnNames.add(Msg.getElement(Env.getCtx(), "DocumentNo"));
		columnNames.add(Msg.getElement(Env.getCtx(), "C_BPartner_ID"));
		columnNames.add(Msg.getElement(Env.getCtx(), "GrandTotal"));
		
		return columnNames;
	}
	
	public void setInvoiceColumnClass(IMiniTable invoiceTable)
	{
		int i = 0;
		invoiceTable.setColumnClass(i++, Boolean.class, false);         //  0-Selection
		invoiceTable.setColumnClass(i++, Timestamp.class, true);        //  1-TrxDate
		invoiceTable.setColumnClass(i++, String.class, true);           //  2-DocumentNo
		invoiceTable.setColumnClass(i++, String.class, true);           //  2-BPartner
		invoiceTable.setColumnClass(i++, String.class, true);           //  2-GrandTotal
		invoiceTable.autoSize();
	}

	private Vector<Vector<Object>> getInvoiceData() {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuilder sql = new StringBuilder("SELECT i.DateInvoiced,o.name||' - '||i.DocumentNo as documentno,i.C_Invoice_ID," //  1..3
			+ "bp.value || ' - ' || bp.name as bpartner, i.c_bpartner_id, "                            //  4..5 BP
			+ "i.GrandTotal "                            //  6 GrandTotal
			+ "FROM C_Invoice i"		//  corrected for CM/Split
			+ " INNER JOIN AD_Org o ON (i.AD_Org_ID=o.AD_Org_ID) "
			+ " INNER JOIN C_BPartner bp ON i.C_BPartner_ID=bp.C_BPartner_ID "
			+ "WHERE i.IsTTF='Y' AND i.DateTTF is null AND i.issotrx='Y' AND DocStatus='CO'"
			+ " AND i.AD_Org_ID=?");                                            //  #7
		sql.append(" ORDER BY i.DateInvoiced, i.DocumentNo");
		if (log.isLoggable(Level.FINE)) log.fine("InvSQL=" + sql.toString());
		
		// role security
		sql = new StringBuilder( MRole.getDefault(Env.getCtx(), false).addAccessSQL( sql.toString(), "i", MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO ) );
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1,m_AD_Org_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				Vector<Object> line = new Vector<Object>();
				line.add(Boolean.FALSE);       //  0-Selection
				line.add(rs.getTimestamp(1));       //  1-TrxDate
				KeyNamePair pp = new KeyNamePair(rs.getInt(3), rs.getString(2));
				line.add(pp);                       //  2-DocumentNo
				pp = new KeyNamePair(rs.getInt(5), rs.getString(4));
				line.add(pp);                       //  3-BPartner
				line.add(rs.getBigDecimal(6)); // 4-GrandTotal
				
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
	}

	private void zkInit() {
		Div div = new Div();
		div.setStyle("height: 100%; width: 100%; overflow: auto;");
		div.appendChild(mainLayout);
		appendChild(div);
		ZKUpdateUtil.setWidth(mainLayout, "100%");
		
		mainLayout.setStyle("min-height: 600px");
		

		dateLabel.setText(Msg.translate(Env.getCtx(), "TTF Date"));
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
		center.appendChild(invoiceTable);
		ZKUpdateUtil.setWidth(invoiceTable, "100%");
		ZKUpdateUtil.setVflex(invoiceTable, "1");
		
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
		if (value == null && (!name.equals("AD_Org_ID")))
			return;
		
		// Organization
		if (name.equals("AD_Org_ID"))
		{
			m_AD_Org_ID = ((Integer) value).intValue();
			
			loadInvoice();
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
						updateInvoice(trxName);
					}
				});
			}catch(Exception ex) {
				FDialog.error(getWindowNo(), this, "Error", ex.getLocalizedMessage());
				processBtn.setEnabled(true);
				return;
			}
			
			FDialog.info(getWindowNo(), this, "Success", "Invoice Updated");
			loadInvoice();
			processBtn.setEnabled(true);
		}
		
	}

	private void updateInvoice(String trxName) {
		Timestamp dateTTF = (Timestamp) dateField.getValue();
		if(dateTTF==null)
			throw new AdempiereException("Date TTF required");	
			
		for (int i = 0; i < invoiceTable.getRowCount(); i++)
		{
			if (((Boolean)invoiceTable.getValueAt(i, 0)).booleanValue())
			{
				KeyNamePair pp = (KeyNamePair)invoiceTable.getValueAt(i, 2);   //  Value
				int C_Invoice_ID = pp.getKey();
				MInvoice invoice = new MInvoice(Env.getCtx(), C_Invoice_ID, trxName);
				invoice.set_ValueOfColumn("DateTTF", dateTTF);
				invoice.saveEx();
			}
		}
	}

	@Override
	public void tableChanged(WTableModelEvent event) {
		// TODO Auto-generated method stub
		
	}

}
