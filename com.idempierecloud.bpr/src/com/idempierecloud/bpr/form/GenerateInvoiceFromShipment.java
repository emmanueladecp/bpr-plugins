package com.idempierecloud.bpr.form;

import static org.compiere.model.SystemIDs.COLUMN_C_PERIOD_AD_ORG_ID;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.editor.WDateEditor;
import org.adempiere.webui.editor.WDatetimeEditor;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
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
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Div;
import org.zkoss.zul.North;
import org.zkoss.zul.South;

import com.idempierecloud.bpr.base.CustomForm;

public class GenerateInvoiceFromShipment extends CustomForm implements ValueChangeListener, WTableModelListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3454393021962227911L;
	public static final CLogger log = CLogger.getCLogger(GenerateInvoiceFromShipment.class);

	private Borderlayout mainLayout = new Borderlayout();
	private Panel parameterPanel = new Panel();
	private Grid parameterLayout = GridFactory.newGridLayout();
	private WListbox shipmentTable = ListboxFactory.newDataTable();
	
	private Label dateShipmentLabel = new Label();
	private WDateEditor dateShipmentField = new WDateEditor();
	private Label organizationLabel = new Label();
	private WTableDirEditor organizationPick;
    protected Label DocActionLabel = new Label();
    protected Listbox DocActionField = ListboxFactory.newDropdownListbox();
    private Label DocTypeLabel = new Label();
	protected Listbox DocTypeField = ListboxFactory.newDropdownListbox();
	private Label dateInvoiceLabel = new Label();
	private WDatetimeEditor dateInvoiceField = new WDatetimeEditor();

	private int m_AD_Org_ID;
	private int m_C_DocType_ID;

	private Button processBtn = new Button();

	private Timestamp m_MovementDate;
	private Timestamp m_InvoiceDate;
	
	
	@Override
	protected void initForm() {
		dynInit();
		zkInit();
		
		loadShipment();
	}
	
	private void loadShipment() {
		processBtn.setEnabled(false);
		
		Vector<Vector<Object>> data = getShipment();
		Vector<String> columnNames = getColumnNames();
		
		shipmentTable.clear();
		
		//  Remove previous listeners
		shipmentTable.getModel().removeTableModelListener(this);
		
		//  Set Model
		ListModelTable modelI = new ListModelTable(data);
		modelI.addTableModelListener(this);
		shipmentTable.setData(modelI, columnNames);
		setColumnClass(shipmentTable);
	}
	public Vector<String> getColumnNames()
	{
		//  Header Info
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add("Organization");
		columnNames.add("Document Type");
		columnNames.add("Shipment");
		columnNames.add("Business Partner");
		columnNames.add("Movement Date");
		columnNames.add("Picklist");
		
		return columnNames;
	}
	
	public void setColumnClass(IMiniTable invoiceTable)
	{
		int i = 0;
		invoiceTable.setColumnClass(i++, Boolean.class, false);         //  0-Selection
		invoiceTable.setColumnClass(i++, String.class, true);           //  1-Organization
		invoiceTable.setColumnClass(i++, String.class, true);           //  2-Document Type
		invoiceTable.setColumnClass(i++, String.class, true);           //  3-Shipment
		invoiceTable.setColumnClass(i++, String.class, true);           //  4-Business Partner
		invoiceTable.setColumnClass(i++, Timestamp.class, true);        //  5-Movement Date
		invoiceTable.setColumnClass(i++, String.class, true);           //  6-Picklist
		invoiceTable.autoSize();
	}

	private void initDocTypeDetails()
	{
		DocTypeField.removeActionListener(this);
		DocTypeField.removeAllItems();
	    //  None
	    KeyNamePair pp = new KeyNamePair(0,"");
	    DocTypeField.addItem(pp);
	    
	    ArrayList<KeyNamePair> list = loadDocType(Env.getAD_Client_ID(Env.getCtx()));
		for(KeyNamePair knp : list)
			DocTypeField.addItem(knp);
		
		DocTypeField.setSelectedIndex(0);
		DocTypeField.addActionListener(this);
	}
	
	protected ArrayList<KeyNamePair> loadDocType(int AD_Client_ID) {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		
		//boolean isSOTrx = (Boolean) getGridTab().getValue("IsSOTrx");
		String sqlStmt = " select c_doctype_id, name from C_DocType where DocBaseType IN ('MMS') AND IsSOTrx='Y' AND AD_Client_ID = ? ";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sqlStmt, null);
			pstmt.setInt(1, AD_Client_ID);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sqlStmt.toString(), e);
		} finally{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		return list;
	}
	
	private Vector<Vector<Object>> getShipment() {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuilder sql = new StringBuilder(""
			+ " with a as(select m_inout_id from m_inout mi where isactive = 'Y' "
			+ "  	and ad_client_id = 1000003 and docstatus in ('CO','CL')), "
			+ "  b as(select mi2.m_inout_id from c_invoiceline ci "
			+ "  	join m_inoutline mi on ci.m_inoutline_id = mi.m_inoutline_id "
			+ "  	join m_inout mi2 on mi.m_inout_id = mi2.m_inout_id "
			+ "	where mi2.m_inout_id in (select m_inout_id from a)"
			+ "  )"
			+ "  select mi.ad_org_id, ao.name, mi.c_doctype_id, cd.name, mi.m_inout_id, mi.documentno, mi.c_bpartner_id, "
			+ "   concat(cb.value,'_',cb.name) as valuebp, 	mi.movementdate, bp2.bpr_picklist_id, bp2.documentno "
			+ "   from m_inout mi left join bpr_picklistline bp on mi.m_inout_id = bp.m_inout_id  "
			+ "   left join bpr_picklist bp2 on bp.bpr_picklist_id = bp2.bpr_picklist_id  "
			+ "   join ad_org ao ON ao.ad_org_id = mi.ad_org_id "
			+ "   join c_doctype cd on cd.c_doctype_id = mi.c_doctype_id  "
			+ "   join c_bpartner cb ON cb.c_bpartner_id = mi.c_bpartner_id "
			+ "   where mi.docstatus in ('CO','CL') and bp2.docstatus in ('CO','CL') and mi.issotrx ='Y'"
			+ "   and mi.m_inout_id not in (select m_inout_id from b) ");
		if(m_AD_Org_ID > 0)
			sql.append(" and mi.ad_org_id=" +m_AD_Org_ID);
		if(m_MovementDate!=null)
			sql.append(" AND mi.movementDate="+m_MovementDate);
		if(m_C_DocType_ID > 0)
			sql.append(" and mi.c_doctype_id="+m_C_DocType_ID);
		
		sql.append(" ORDER BY mi.documentno");
		if (log.isLoggable(Level.FINE)) log.fine("SQL=" + sql.toString());
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				Vector<Object> line = new Vector<Object>();
				line.add(Boolean.FALSE);       //  0-Selection
				KeyNamePair pp = new KeyNamePair(rs.getInt(1), rs.getString(2));
				line.add(pp);                       //  1-Organization
				pp = new KeyNamePair(rs.getInt(3), rs.getString(4));
				line.add(pp);                       //  2-Document Type
				pp = new KeyNamePair(rs.getInt(5), rs.getString(6));
				line.add(pp);                       //  3-Shipment
				pp = new KeyNamePair(rs.getInt(7), rs.getString(8));
				line.add(pp);                       //  4-Business Partner
				line.add(rs.getTimestamp(9));       //  5-TrxDate
				pp = new KeyNamePair(rs.getInt(10), rs.getString(11));
				line.add(pp);                       //  6-Picklist
				
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
		
		dateShipmentField.addValueChangeListener(this);
		dateInvoiceField.addValueChangeListener(this);
		initDocTypeDetails();
		
		processBtn.setEnabled(false);
	}
	
	private void zkInit() {
		Div div = new Div();
		div.setStyle("height: 100%; width: 100%; overflow: auto;");
		div.appendChild(mainLayout);
		appendChild(div);
		ZKUpdateUtil.setWidth(mainLayout, "100%");
		
		organizationLabel.setText(Msg.translate(Env.getCtx(), "AD_Org_ID"));
		dateShipmentLabel.setText(Msg.translate(Env.getCtx(), "Movement Date"));
		DocTypeLabel.setText(Msg.translate(Env.getCtx(), "Document Type"));
		dateInvoiceLabel.setText(Msg.translate(Env.getCtx(), "Date Invoced"));
		
		processBtn.setLabel(Util.cleanAmp(Msg.getMsg(Env.getCtx(), "Process")));
		processBtn.addActionListener(this);
		
		dateInvoiceLabel.setMandatory(true);
		
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
		column.setWidth("14%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("14%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("14%");
		columns.appendChild(column);
		column = new Column();
		column.setWidth("14%");	
		columns.appendChild(column);
		column = new Column();
		column.setWidth("15%");	
		columns.appendChild(column);
		column = new Column();
		column.setWidth("14%");	
		columns.appendChild(column);
		column = new Column();
		column.setWidth("15%");	
		columns.appendChild(column);
		parameterLayout.appendChild(columns);
		
		Rows rows = parameterLayout.newRows();
		Row row = rows.newRow();
		row.appendCellChild(organizationLabel.rightAlign());
		ZKUpdateUtil.setHflex(organizationPick.getComponent(), "true");
		row.appendCellChild(organizationPick.getComponent(),1);
		organizationPick.showMenu();
		row.appendChild(dateShipmentLabel.rightAlign());
		row.appendChild(dateShipmentField.getComponent());
		
		row = rows.newRow();
		row.appendChild(DocTypeLabel.rightAlign());
		row.appendChild(DocTypeField);
		DocTypeField.setHflex("1");
		row.appendChild(dateInvoiceLabel.rightAlign());
		row.appendChild(dateInvoiceField.getComponent());
		dateInvoiceField.setMandatory(true);
		
		Center center = new Center();
		mainLayout.appendChild(center);
		center.appendChild(shipmentTable);
		
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
		if (name == "Date" && value == null) {
			m_MovementDate = null;
			loadShipment();
			return;
		}
		if (name == "AD_Org_ID" && value == null) {
			m_AD_Org_ID = 0;
			loadShipment();
			return;
		}
		if (value == null) {
			return;
		}
			
		
		// Organization
		if (name.equals("AD_Org_ID"))
		{
			m_AD_Org_ID = ((Integer) value).intValue();
		}else if (name.equals("Date")){
			m_MovementDate = (Timestamp) value;
		}else if (name.equals("Datetime")){
			m_InvoiceDate = (Timestamp) value;
		}
		loadShipment();
		
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
						generateShipment(trxName);
					}
				});
			}catch(Exception ex) {
				FDialog.error(getWindowNo(), this, "Error", ex.getLocalizedMessage());
				return;
			}

			loadShipment();
		}else if (e.getTarget().equals(DocTypeField)){
			KeyNamePair pp = DocTypeField.getSelectedItem().toKeyNamePair();
			if (pp!=null)
				m_C_DocType_ID = pp.getKey();
			else
				m_C_DocType_ID = 0;

			loadShipment();
		}
	}
	@Override
	public void tableChanged(WTableModelEvent event) {
		int count = 0;
		for (int i = 0; i < shipmentTable.getRowCount(); i++)
		{
			if (((Boolean)shipmentTable.getValueAt(i, 0)).booleanValue())
				count++;
		}
		
		processBtn.setEnabled(count>0);
	}
	
	private String generateShipment(String trxName) {
		String Success = null;
		for (int i = 0; i < shipmentTable.getRowCount(); i++)
		{
			if (((Boolean)shipmentTable.getValueAt(i, 0)).booleanValue())
			{
				KeyNamePair pp = (KeyNamePair)shipmentTable.getValueAt(i, 3);
				int M_InOut_ID = pp.getKey();
				pp = (KeyNamePair)shipmentTable.getValueAt(i, 1);
				int AD_Org_ID = pp.getKey();
				if(m_InvoiceDate==null)
					throw new AdempiereException("Tolong masukan date invoice");
				MInOut inOut = new MInOut(Env.getCtx(), M_InOut_ID, trxName);
				MInvoice invoice = new MInvoice(Env.getCtx(), 0, trxName);
				invoice.setAD_Org_ID(AD_Org_ID);
				invoice.setIsSOTrx(inOut.isSOTrx());
				invoice.setC_BPartner_ID(inOut.getC_BPartner_ID());
				invoice.setC_BPartner_Location_ID(inOut.getC_BPartner_Location_ID());
				invoice.setDateInvoiced(m_InvoiceDate);
				invoice.setDateAcct(m_InvoiceDate);
				invoice.setC_DocTypeTarget_ID(1000002);//DocType AR Receipt
				MOrder order = (MOrder)inOut.getC_Order();
				invoice.setPaymentRule(order.getPaymentRule());
				invoice.setM_PriceList_ID(order.getM_PriceList_ID());
				invoice.setC_PaymentTerm_ID(order.getC_PaymentTerm_ID());
				invoice.saveEx();
				
				for(MInOutLine line : inOut.getLines(true)) {
					MOrderLine oline = (MOrderLine) line.getC_OrderLine();	
				
					MInvoiceLine iLine = new MInvoiceLine(invoice.getCtx(), 0, trxName);
					iLine.setAD_Org_ID(invoice.getAD_Org_ID());
					iLine.setC_Invoice_ID(invoice.get_ID());
					iLine.setM_InOutLine_ID(line.getM_InOutLine_ID());
					iLine.setM_Product_ID(line.getM_Product_ID());
					iLine.setQtyEntered(line.getQtyEntered());
					iLine.setQtyInvoiced(line.getMovementQty());
					if(order.get_ValueAsInt("C_Tax_ID")>0) 
						iLine.setC_Tax_ID(order.get_ValueAsInt("C_Tax_ID"));
					else 
						iLine.setC_Tax_ID(1000000);//C_Tax_ID Bebas_PPN
					iLine.setPriceActual(oline.getPriceActual());
					iLine.setPriceEntered(oline.getPriceEntered());
					iLine.setPriceList(oline.getPriceList());
					if(oline.getC_Charge_ID()>0)
						iLine.setC_Charge_ID(oline.getC_Charge_ID());
					iLine.set_ValueOfColumn("OngkosAngkut", oline.get_Value("OngkosAngkut"));
					iLine.set_ValueOfColumn("SubsidiAmt", oline.get_Value("SubsidiAmt"));
					iLine.saveEx();
				}
				if(invoice.getC_Invoice_ID()>0) {
					FDialog.info(getWindowNo(), this, "Success", "Success! invoice Document No"+invoice.getDocumentNo());
				}
			}
		}
		return Success;
		
	}

	

}
