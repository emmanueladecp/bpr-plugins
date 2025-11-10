package com.idempierecloud.bpr.form;

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
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.editor.WStringEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.compiere.model.GridTab;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MOrder;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Vlayout;

public class WCreateFromOrder extends CreateFromOrder implements EventListener<Event>, ValueChangeListener {

	private WCreateFromWindow window;
	private boolean m_actionActive = false;
	
	/** Window No               */
	//private int p_WindowNo;
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(getClass());

    /** Labels and Fields */
    protected Label orgLabel = new Label();
    protected Listbox orgField = ListboxFactory.newDropdownListbox();
    protected Label reqLabel = new Label();
    protected Listbox reqField = ListboxFactory.newDropdownListbox();
    protected Label bpartnerLabel = new Label();
    protected WSearchEditor bpartnerField = null;
    protected Label timbanganLabel = new Label();
    protected WStringEditor timbanganField = null;
    protected Label locatorLabel = new Label();
    protected Listbox locatorField = ListboxFactory.newDropdownListbox();
	private int p_WindowNo;
	
	int C_Order_ID = ((Integer) getGridTab().getValue("C_Order_ID")).intValue();
	MOrder order = new MOrder(Env.getCtx(), C_Order_ID, null);	
    
	
	public WCreateFromOrder(GridTab mTab) {
		super(mTab);
		AD_Client_ID = (Integer)mTab.getValue("AD_Client_ID");
		AD_Org_ID = (Integer)mTab.getValue("AD_Org_ID");
		log.info(getGridTab().toString());
		
		window = new WCreateFromWindow(this, getGridTab().getWindowNo());		
		//p_WindowNo = getGridTab().getWindowNo();
		
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

	protected void zkInit() throws Exception
	{
		orgLabel.setText(Msg.translate(Env.getCtx(), "AD_Org_ID"));
		reqLabel.setText(Msg.translate(Env.getCtx(), "M_Requisition_ID"));
		if(isturus.equals("Y")) {
			bpartnerLabel.setText(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
			locatorLabel.setText(Msg.translate(Env.getCtx(), "M_Locator_ID"));
		}
		timbanganLabel.setText(Msg.translate(Env.getCtx(), "Nota Timbangan"));

		Vlayout vlayout = new Vlayout();
		vlayout.setVflex("1");
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
		
		if(isturus.equals("Y")) {
			row.appendChild(bpartnerLabel.rightAlign());
			row.appendChild(bpartnerField.getComponent());
			reqField.setHflex("1");
		}
		
		row = rows.newRow();
		row.appendChild(timbanganLabel.rightAlign());
		row.appendChild(timbanganField.getComponent());
		timbanganLabel.setHflex("1");

		row.appendChild(reqLabel.rightAlign());
		row.appendChild(reqField);
		reqField.setHflex("1");

		if(isturus.equals("Y")) {
			row = rows.newRow();
			row.appendChild(locatorLabel.rightAlign());
			row.appendChild(locatorField);
			locatorField.setHflex("1");
		}
	}	
	
	public boolean dynInit() throws Exception
	{
		log.config("");
		
		super.dynInit();
		window.setTitle(getTitle());
		p_WindowNo = getGridTab().getWindowNo();
		
		timbanganField = new WStringEditor("timbangan", false, false, true, 20, 20, null, null);
		timbanganField.addValueChangeListener(this);
		
		initOrgData();
		if(isturus.equals("Y")) {
			initBPartner();
		}
		initRequisitionData();
		
		//loadRequisition();
		
		return true;
	}
	
	protected void initBPartner () throws Exception
	{
		//  load BPartner
		int AD_Column_ID = 2762;        //  C_Order.C_BPartner_ID
		MLookup lookup = MLookupFactory.get (Env.getCtx(), p_WindowNo, 0, AD_Column_ID, DisplayType.Search);
		bpartnerField = new WSearchEditor ("C_BPartner_ID", true, false, true, lookup);
		//
		C_BPartner_ID = Env.getContextAsInt(Env.getCtx(), p_WindowNo, "C_BPartner_ID");
		bpartnerField.setValue(Integer.valueOf(C_BPartner_ID));
		bpartnerField.addValueChangeListener(this);
	}   //  initBPartner
	
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
	
	/**
	 *  Load Data - Requisition
	 */
	protected void loadRequisition()
	{
		loadTableOIS(getRequisitionData());
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
		
		orgField.setSelectedIndex(idxSelected);
		orgField.setEnabled(true);
		orgField.addActionListener(this);
		
		initLocatorData();
	}
	
	private void initRequisitionData(){
		window.getWListbox().clear();
		
		reqField.removeActionListener(this);
		reqField.removeAllItems();
		
		KeyNamePair pp = new KeyNamePair(0, "");
		reqField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadRequisitionData();
		for (KeyNamePair knp : list){
			reqField.addItem(knp);
		}
		
		reqField.addActionListener(this);
	}
	
	private void initLocatorData(){
		window.getWListbox().clear();
		
		locatorField.removeActionListener(this);
		locatorField.removeAllItems();
		
		KeyNamePair pp = new KeyNamePair(0, "");
		locatorField.addItem(pp);
		
		ArrayList<KeyNamePair> list = loadLocatorData();
		for (KeyNamePair knp : list){
			locatorField.addItem(knp);
		}
		
		locatorField.addActionListener(this);	
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
			initLocatorData();
			initRequisitionData();
		}else if (e.getTarget().equals(reqField)){
			KeyNamePair pp = reqField.getSelectedItem().toKeyNamePair();
			if (pp!=null)
				M_Requisition_ID = pp.getKey();
			else
				M_Requisition_ID = 0;

			loadRequisition();
		}else if (e.getTarget().equals(locatorField)){
			KeyNamePair pp = locatorField.getSelectedItem().toKeyNamePair();
			if (pp!=null)
				M_Locator_ID = pp.getKey();
			else
				M_Locator_ID = 0;
		}
		
		m_actionActive = false;				
	}
	
	public void showWindow()
	{
		window.setVisible(true);
	}
	
	public void closeWindow()
	{
		window.dispose();
	}

	@Override
	public Object getWindow() {
		return window;
	}

	@Override
	public void valueChange(ValueChangeEvent e) {
		if (log.isLoggable(Level.CONFIG)) log.config(e.getPropertyName() + "=" + e.getNewValue());

		//  BPartner - load Order/Invoice/Shipment
		if (e.getPropertyName().equals("timbangan"))
		{
			NotaTimbangan = (String) e.getNewValue();
			initRequisitionData();
			loadRequisition();
			
		}else if (e.getPropertyName().equals("C_BPartner_ID"))
		{
			Integer newBpValue = (Integer)e.getNewValue();
			C_BPartner_ID = newBpValue == null?0:newBpValue.intValue();
			initRequisitionData();
			loadRequisition();
			
		} 
		window.tableChanged(null);
	}	
	
}
