package com.idempierecloud.bpr.process;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.util.RestService;

public class SyncBP extends CustomProcess {

	private String value = null;
	private Timestamp created = null;
	
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("Value")) 
				value = para[i].getParameterAsString();
			else if (name.equals("Created")) 
				created = para[i].getParameterAsTimestamp();
		}
	}

	@Override
	protected String doIt() throws Exception {
		
		RestService rest = new RestService();
		
		String url = "models/C_BPartner";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		List<String> params = new ArrayList<String>();
		if(value!=null)
			params.add("value eq '"+value+"'");
		if(created!=null) {
			params.add("created gt '"+sdf.format(created)+"'");
		}
		String filter = "$expand=C_BPartner_Location";
		if(params.size()>0) {
			String query = String.join(" and ", params);
			query = RestService.encodeQuery(query);
			filter += "&$filter="+query;
		}
		
		JsonObject result = rest.get(url, filter);
		
		if(rest.isError())
			throw new AdempiereException(rest.getMessage());
		
		int insert = 0;
		int update = 0;
		int error = 0;
		JsonArray records = result.getAsJsonArray("records");
		for (JsonElement record : records) {

		    boolean isUpdate = true;
			try {
			    JsonObject bp = record.getAsJsonObject();
			    String bpCode = bp.get("Value").getAsString();
			    MBPartner bpartner = new Query(Env.getCtx(), MBPartner.Table_Name, "value=?", get_TrxName())
			    		.setParameters(bpCode)
			    		.first();
			    
			    if(bpartner==null) {
			    	log.warning("No BP Found "+bpCode);
			    	bpartner = new MBPartner(getCtx(), 0, get_TrxName());
			    	isUpdate = false;
			    }
			    
			    bpartner.setValue(bp.get("Value").getAsString());
			    bpartner.setName(bp.get("Name").getAsString());
			    if(bp.has("C_BP_Group_ID"))
			    	bpartner.setC_BP_Group_ID(findId(bp, "C_BP_Group_ID"));
			    if(bp.has("C_Greeting_ID"))
			    	bpartner.setC_Greeting_ID(findId(bp, "C_Greeting_ID"));
			    if(bp.has("C_PaymentTerm_ID"))
			    	bpartner.setC_PaymentTerm_ID(findId(bp, "C_PaymentTerm_ID"));
//			    if(bp.has("SalesRep_ID"))
//			    	bpartner.setSalesRep_ID(findId(bp, "SalesRep_ID"));
			    if(bp.has("C_SalesRegion_ID"))
			    	bpartner.set_ValueOfColumn("C_SalesRegion_ID", findId(bp, "C_SalesRegion_ID"));
			    
			    bpartner.setIsActive(bp.get("IsActive").getAsBoolean());
			    bpartner.setIsEmployee(bp.get("IsEmployee").getAsBoolean());
			    bpartner.setIsVendor(bp.get("IsVendor").getAsBoolean());
			    bpartner.setIsCustomer(bp.get("IsCustomer").getAsBoolean());
			    bpartner.setIsSalesRep(bp.get("IsSalesRep").getAsBoolean());
			    bpartner.saveEx();
			    
			    if(bp.has("C_BPartner_Location")) {
					JsonArray bplocations = bp.getAsJsonArray("C_BPartner_Location");
					for (JsonElement location : bplocations) {
						JsonObject bplocation = location.getAsJsonObject();
						
	
						int location_id = findId(bplocation, "C_Location_ID");
						JsonObject address = rest.get("models/C_Location/"+location_id, null);
						MLocation mAddress = new Query(Env.getCtx(), MLocation.Table_Name, "C_Country_ID=? AND Address1=?", get_TrxName())
					    		.setParameters(findId(address, "C_Country_ID"), address.get("Address1").getAsString())
					    		.first();
						
						if(mAddress==null) {
							mAddress = new MLocation(Env.getCtx(), 0, get_TrxName());
						}
						mAddress.setC_Country_ID(findId(address, "C_Country_ID"));
						mAddress.setAddress1(address.get("Address1").getAsString());
						if(address.has("Address2"))
						mAddress.setAddress2(address.get("Address2").getAsString());
						if(address.has("Address3"))
						mAddress.setAddress3(address.get("Address3").getAsString());
						if(address.has("Address4"))
						mAddress.setAddress4(address.get("Address4").getAsString());
						if(address.has("Address5"))
						mAddress.setAddress5(address.get("Address5").getAsString());
						if(address.has("City"))
						mAddress.setCity(address.get("City").getAsString());
						if(address.has("C_City_ID"))
						mAddress.setC_City_ID(findId(address, "C_City_ID"));
						mAddress.saveEx();
						
						String bpLocationName = bplocation.get("Name").getAsString();
						MBPartnerLocation bpartnerLocation = new Query(Env.getCtx(), MBPartnerLocation.Table_Name, "c_bpartner_id=? and name=?", get_TrxName())
					    		.setParameters(bpartner.getC_BPartner_ID(), bpLocationName)
					    		.first();
						
						if(bpartnerLocation==null) {
					    	log.warning("No BP Location Found "+bpLocationName);
					    	bpartnerLocation = new MBPartnerLocation(bpartner);
						}
						bpartnerLocation.setC_Location_ID(mAddress.getC_Location_ID());
						if(bplocation.has("C_Country_ID"))
							bpartnerLocation.set_ValueOfColumn("C_Country_ID", findId(bplocation, "C_Country_ID"));
	
						if(bplocation.has("C_Region_ID"))
							bpartnerLocation.set_ValueOfColumn("C_Region_ID", findId(bplocation, "C_Region_ID"));
	
						if(bplocation.has("C_City_ID"))
							bpartnerLocation.set_ValueOfColumn("C_City_ID", findId(bplocation, "C_City_ID"));
	
						if(bplocation.has("BPR_District_ID"))
							bpartnerLocation.set_ValueOfColumn("BPR_District_ID", findId(bplocation, "BPR_District_ID"));
						
						bpartnerLocation.setIsBillTo(bplocation.get("IsBillTo").getAsBoolean());
						bpartnerLocation.setIsShipTo(bplocation.get("IsShipTo").getAsBoolean());
						bpartnerLocation.setIsActive(bplocation.get("IsActive").getAsBoolean());
						bpartnerLocation.saveEx();
					}
				}
			}catch(AdempiereException ex) {
				log.warning(ex.getMessage());
				error++;
				continue;
			}
			
			if(isUpdate)
				update++;
			else
				insert++;
		}
		
		return "Processed Insert: "+insert+", updated: "+update+", error: "+error;
	}

	private int findId(JsonObject bp, String column) {
		JsonObject data = bp.getAsJsonObject(column);
		
		if(data.has("id")) {
			return data.get("id").getAsInt();
		}
			
		log.warning("No Column "+column);
		return 0;
	}

}
