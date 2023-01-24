package com.idempierecloud.bpr.form;

import static org.compiere.model.SystemIDs.COLUMN_C_PERIOD_AD_ORG_ID;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MPayment;
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

public class CompleteARReceipt extends CustomForm implements ValueChangeListener, WTableModelListener {


	public DecimalFormat format = DisplayType.getNumberFormat(DisplayType.Amount);
	private static final long serialVersionUID = 7401287714718817073L;
	public static final CLogger log = CLogger.getCLogger(CompleteARReceipt.class);
	private Panel southPanel = new Panel();
	
	private Borderlayout mainLayout = new Borderlayout();
	private Panel allocationPanel = new Panel();
	private Grid allocationLayout = GridFactory.newGridLayout();
	private Panel parameterPanel = new Panel();
	private Grid parameterLayout = GridFactory.newGridLayout();
	private WListbox ARTable = ListboxFactory.newDataTable();
    protected Label BankAccountARLabel = new Label();
	protected Listbox BankAccountARField = ListboxFactory.newDropdownListbox();
	private Label organizationLabel = new Label();
	private WTableDirEditor organizationPick;
	private Label TotalLabel = new Label();
	private Textbox TotalField = new Textbox();
	private int m_AD_Org_ID;
	private int m_C_BankAccount_ID;
	BigDecimal totalPay = Env.ZERO;
	int totalpaymt = 0;

	private Button processBtn = new Button();

	@Override
	protected void initForm() {
		dynInit();
		zkInit();		
		loadARReceipt();
		calculate();
	}
	
	private void dynInit() {
		// Organization filter selection
		int AD_Column_ID = COLUMN_C_PERIOD_AD_ORG_ID; //C_Period.AD_Org_ID (needed to allow org 0)
		MLookup lookupOrg = MLookupFactory.get(Env.getCtx(), getWindowNo(), 0, AD_Column_ID, DisplayType.TableDir);
		organizationPick = new WTableDirEditor("AD_Org_ID", true, false, true, lookupOrg);
		m_AD_Org_ID = Env.getAD_Org_ID(Env.getCtx());
		organizationPick.setValue(m_AD_Org_ID);
		organizationPick.addValueChangeListener(this);
		
		initBankAccount();
		
		processBtn.setEnabled(false);
		loadBankAccount();
	}

	private void zkInit() {
		Div div = new Div();
		div.setStyle("height: 100%; width: 100%; overflow: auto;");
		div.appendChild(mainLayout);
		appendChild(div);
		ZKUpdateUtil.setWidth(mainLayout, "100%");
		
		BankAccountARLabel.setText(Msg.translate(Env.getCtx(), "Bank Account AR"));
		TotalLabel.setText(Msg.translate(Env.getCtx(), "Total Amount"));
		organizationLabel.setText(Msg.translate(Env.getCtx(), "Organization"));
		processBtn.setLabel(Util.cleanAmp(Msg.getMsg(Env.getCtx(), "Process")));
		processBtn.addActionListener(this);
		
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
		column.setWidth("12%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("12%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("12%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("12%");	
		columns.appendChild(column);
		column = new Column();
		column.setWidth("13%");	
		columns.appendChild(column);
		column = new Column();
		column.setWidth("13%");	
		columns.appendChild(column);
		column = new Column();
		column.setWidth("13%");	
		columns.appendChild(column);
		column = new Column();
		column.setWidth("13%");	
		columns.appendChild(column);
		parameterLayout.appendChild(columns);
		
		Rows rows = parameterLayout.newRows();
		Row row = rows.newRow();
		row.appendCellChild(organizationLabel.rightAlign());
		ZKUpdateUtil.setHflex(organizationPick.getComponent(), "true");
		row.appendCellChild(organizationPick.getComponent(),2);
		organizationPick.showMenu();
		row.appendChild(BankAccountARLabel.rightAlign());
		row.appendChild(BankAccountARField);
		BankAccountARField.setHflex("2");
		
		
		Center center = new Center();
		mainLayout.appendChild(center);
		center.appendChild(ARTable);
		
		South south = new South();
		south.setStyle("border: none");
		mainLayout.appendChild(south);
		south.appendChild(southPanel);
		southPanel.appendChild(allocationPanel);
		allocationPanel.appendChild(allocationLayout);
		rows = allocationLayout.newRows();
		row = rows.newRow();
		row.appendCellChild(TotalLabel.rightAlign());
		TotalField.setHflex("true");
		row.appendCellChild(TotalField);
		row.appendCellChild(processBtn);
		
	}
	
	protected ArrayList<KeyNamePair> loadBankAccount(){
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		StringBuffer sql = new StringBuffer("select distinct cb.c_bankaccount_id, cb.name from c_payment cp ");
		sql.append(" join c_bankaccount cb on cp.c_bankaccount_id = cb.c_bankaccount_id ");
		sql.append(" where cp.docstatus in ('CO')");
		sql.append(" ORDER BY cb.name");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return list;
	}  
	
	
	
	public Vector<String> getColumnNames()
	{
		//  Header Info
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add("No Document");
		columnNames.add("Business Partner");
		columnNames.add("Transaction Date");
		columnNames.add("Bank Account");
		columnNames.add("Payment Amount");
		columnNames.add("No Invoice");
		columnNames.add("Created By");
		
		return columnNames;
	}
	
	public void setColumnClass(IMiniTable invoiceTable)
	{
		int i = 0;
		invoiceTable.setColumnClass(i++, Boolean.class, false);         //  0-Selection
		invoiceTable.setColumnClass(i++, String.class, true);           //  1-No Document
		invoiceTable.setColumnClass(i++, String.class, true);           //  2-Business Partner
		invoiceTable.setColumnClass(i++, Timestamp.class, true);        //  3-Transaction Date
		invoiceTable.setColumnClass(i++, String.class, true);           //  4-Bank Account
		invoiceTable.setColumnClass(i++, String.class, true);           //  5-Payment Amount
		invoiceTable.setColumnClass(i++, String.class, true);           //  6-No Invoice
		invoiceTable.setColumnClass(i++, String.class, true);           //  7-Created By
		invoiceTable.autoSize();
	}
	
	private void initBankAccount(){
		BankAccountARField.removeActionListener(this);
		BankAccountARField.removeAllItems();
	    //  None
	    KeyNamePair pp = new KeyNamePair(0,"");
	    BankAccountARField.addItem(pp);
	    
	    ArrayList<KeyNamePair> list = loadBankAccount();
		for(KeyNamePair knp : list)
			BankAccountARField.addItem(knp);
		
		BankAccountARField.setSelectedIndex(0);
		BankAccountARField.addActionListener(this);
	}
	
	private void loadARReceipt() {
		processBtn.setEnabled(false);
		Vector<Vector<Object>> data = getPayment();
		Vector<String> columnNames = getColumnNames();
		
		ARTable.clear();
		
		//  Remove previous listeners
		ARTable.getModel().removeTableModelListener(this);
		
		//  Set Model
		ListModelTable modelI = new ListModelTable(data);
		modelI.addTableModelListener(this);
		ARTable.setData(modelI, columnNames);
		setColumnClass(ARTable);
		
	}
	
	private Vector<Vector<Object>> getPayment() {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuilder sql = new StringBuilder("select cp.c_payment_id, cp.documentno, cb.c_bpartner_id, concat(cb.value,'_',cb.name) as valuebp, cp.datetrx, "
				+ " cb2.c_bankaccount_id, cb2.name, cp.payamt, ci.c_invoice_id, ci.documentno, au.ad_user_id, au.name "
				+ " from c_payment cp  "
				+ " join c_bpartner cb on cp.c_bpartner_id = cb.c_bpartner_id "
				+ " join c_bankaccount cb2 on cp.c_bankaccount_id = cb2.c_bankaccount_id "
				+ " left join c_invoice ci on cp.c_invoice_id = ci.c_invoice_id "
				+ " join ad_user au on cp.createdby = au.ad_user_id "
				+ " where cp.docstatus in ('CO') and cp.c_doctype_id = 1000008");
		
		if(m_AD_Org_ID>0)
			sql.append(" AND cp.ad_org_id = ?");
		if(m_C_BankAccount_ID>0)
			sql.append(" AND cp.C_BankAccount_ID = ?");
		
		sql.append(" order by cp.documentno");
		if (log.isLoggable(Level.FINE)) log.fine("SQL=" + sql.toString());
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			if(m_AD_Org_ID>0) {
				pstmt.setInt(1,m_AD_Org_ID);
			}				
			if(m_C_BankAccount_ID>0) {
				if(m_AD_Org_ID>0) {
					pstmt.setInt(2, m_C_BankAccount_ID);
				}else {
					pstmt.setInt(1, m_C_BankAccount_ID);
				}
			}
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
				pp = new KeyNamePair(rs.getInt(6), rs.getString(7));
				line.add(pp);                       //  4-BankAccount
				line.add(rs.getBigDecimal(8));      //  5-PayAmt
				pp = new KeyNamePair(rs.getInt(9), rs.getString(10));
				line.add(pp);                       //  6-Invoice
				pp = new KeyNamePair(rs.getInt(11), rs.getString(12));
				line.add(pp);                       //  7-Created By
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
						completeARReceipt(trxName);
					}
				});
			}catch(Exception ex) {
				FDialog.error(getWindowNo(), this, "Error", ex.getLocalizedMessage());
				return;
			}

			loadARReceipt();
		}else if (e.getTarget().equals(BankAccountARField)){
			KeyNamePair pp = BankAccountARField.getSelectedItem().toKeyNamePair();
			if (pp!=null)
				m_C_BankAccount_ID = pp.getKey();
			else
				m_C_BankAccount_ID = 0;

			loadARReceipt();
		}
		
	}
	private void completeARReceipt(String trxName) {	
		
		for (int i = 0; i < ARTable.getRowCount(); i++)
		{
			if (((Boolean)ARTable.getValueAt(i, 0)).booleanValue())
			{
				KeyNamePair pp = (KeyNamePair)ARTable.getValueAt(i, 1);
				int C_Payment_ID = pp.getKey();
				MPayment payment = new MPayment(Env.getCtx(), C_Payment_ID, trxName);
				if(!payment.processIt(MPayment.ACTION_Complete)) {
					FDialog.error(getWindowNo(), this, "Error", payment.getProcessMsg());
					return;
				}
				
				payment.saveEx();
			}
		}
		FDialog.info(getWindowNo(), this, "Berhasil complete");
	}
	
	@Override
	public void tableChanged(WTableModelEvent event) {
		boolean isUpdate = (event.getType() == WTableModelEvent.CONTENTS_CHANGED);
		//  Not a table update
		if (!isUpdate)
		{
			calculate();
			return;
		}
		calculate();
		int count = 0;
		for (int i = 0; i < ARTable.getRowCount(); i++)
		{
			if (((Boolean)ARTable.getValueAt(i, 0)).booleanValue())
				count++;
		}
		
		processBtn.setEnabled(count>0);	
	}

	private void calculate() {
		calculatePayment(ARTable);
		TotalField.setText(format.format(totalPay));	
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
			
			loadARReceipt();
		}
		
	}
	
	public String calculatePayment(IMiniTable payment)
	{
		log.config("");

		//  Payment
		totalPay = Env.ZERO;
		totalpaymt = 0;
		int rows = payment.getRowCount();
		for (int i = 0; i < rows; i++)
		{
			if (((Boolean)payment.getValueAt(i, 0)).booleanValue())
			{
				BigDecimal bd = (BigDecimal)payment.getValueAt(i, 5);
				totalPay = totalPay.add(bd);  //  Applied Pay
				totalpaymt++;
				if (log.isLoggable(Level.FINE)) log.fine("Payment_" + i + " = " + bd + " - Total=" + totalPay);
			}
		}
		return String.valueOf(totalpaymt) + " - "
			+ Msg.getMsg(Env.getCtx(), "Sum") + "  " + format.format(totalPay) + " ";
	}

	

}
