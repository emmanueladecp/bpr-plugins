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
import org.compiere.model.GridTab;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Space;
import org.zkoss.zul.Vlayout;

public class WCreateFromOrder extends CreateFromOrder implements EventListener<Event> {

	private WCreateFromWindow window;
	private Integer AD_Client_ID = 0;
	private Integer AD_Org_ID = 0;
	private boolean m_actionActive = false;
	
	/** Window No               */
	//private int p_WindowNo;
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(getClass());

    /** Labels and Fields */
    protected Label orgLabel = new Label();
    protected Listbox orgField = ListboxFactory.newDropdownListbox();
	
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
		row.appendChild(new Space());
	}	
	
	public boolean dynInit() throws Exception
	{
		log.config("");
		
		super.dynInit();
		window.setTitle(getTitle());
		
		initOrgData();
		
		if(!isSOTrx)
			loadRequisition();
		
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
	
	/**
	 *  Load Data - Requisition
	 */
	protected void loadRequisition()
	{
		loadTableOIS(getRequisitionData(AD_Client_ID, AD_Org_ID));
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
		orgField.addActionListener(this);
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
			
			loadRequisition();
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
	
}
