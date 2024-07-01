package com.idempierecloud.bpr.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.BPartnerException;
import org.compiere.model.MBPGroup;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.model.MSalesRegion;
import org.compiere.model.MUser;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.idempierecloud.bpr.base.CustomProcess;
import com.idempierecloud.bpr.util.RestService;

public class SyncBP extends CustomProcess {

	private String value = null;
	private Timestamp created = null;
	private boolean isSalesRep = false;
	private int C_BPartner_ID = 0;
	private int C_BP_Group_ID = 0;
	private int C_SalesRegion_ID = 0;
	
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
			else if (name.equals("IsSalesRep")) 
				isSalesRep = para[i].getParameterAsBoolean();
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
			params.add("updated gt '"+sdf.format(created)+"'");
		}
		if(isSalesRep) {
			params.add("issalesrep eq true");
		}
		String filter = "$expand=SalesRep_ID,AD_User,C_BPartner_Location($expand=C_Location_ID)";
		if(params.size()>0) {
			String query = String.join(" and ", params);
			query = RestService.encodeQuery(query);
			filter += "&$filter="+query;
		}
		
		JsonObject result = rest.get(url, filter);
		log.warning(result.toString());
		
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
			    C_BPartner_ID = bpartner.getC_BPartner_ID();
			    bpartner.setValue(bp.get("Value").getAsString());
			    bpartner.setName(bp.get("Name").getAsString());
			    if(bp.has("Name2"))
			    	bpartner.setName2(bp.get("Name2").getAsString());
			    if(bp.has("TaxID"))
			    	bpartner.setTaxID(bp.get("TaxID").getAsString());
			    if(bp.has("KtpID"))
			    	bpartner.set_ValueOfColumn("KtpID", bp.get("KtpID").getAsString());
			    if(bp.has("IsNpwp"))
			    	bpartner.set_ValueOfColumn("IsNpwp", bp.get("IsNpwp").getAsBoolean());
			    if(bp.has("IsProspect"))
			    	bpartner.setIsProspect(bp.get("IsProspect").getAsBoolean());
			    if(bp.has("C_BP_Group_ID"))
			    	bpartner.setC_BP_Group_ID(cekC_BP_Group(bp));
			    if(bp.has("C_Greeting_ID"))
			    	bpartner.setC_Greeting_ID(findId(bp, "C_Greeting_ID"));
			    if(bp.has("C_PaymentTerm_ID"))
			    	bpartner.setC_PaymentTerm_ID(findId(bp, "C_PaymentTerm_ID"));
			    if(bp.has("SalesRep_ID")) {
			    	int SalesRep_ID = findSalesRep(bp);
			    	if(SalesRep_ID>0)
			    		bpartner.setSalesRep_ID(SalesRep_ID);
			    }
			    if(bp.has("C_SalesRegion_ID"))
			    	bpartner.set_ValueOfColumn("C_SalesRegion_ID", checkSalesRegion(bp));
			    
			    bpartner.setIsActive(bp.get("IsActive").getAsBoolean());
			    bpartner.setIsEmployee(bp.get("IsEmployee").getAsBoolean());
			    bpartner.setIsVendor(bp.get("IsVendor").getAsBoolean());
			    bpartner.setIsCustomer(bp.get("IsCustomer").getAsBoolean());
			    bpartner.setIsSalesRep(bp.get("IsSalesRep").getAsBoolean());
			    bpartner.saveEx();

			    if(bp.has("AD_User")) {
			    	JsonArray users = bp.getAsJsonArray("AD_User");
					for (JsonElement row : users) {
						JsonObject user = row.getAsJsonObject();
						
						MUser mUser = new Query(Env.getCtx(), MUser.Table_Name, "c_bpartner_id=? and ad_userref_id=?", get_TrxName())
					    		.setParameters(bpartner.getC_BPartner_ID(), user.get("id").getAsInt())
					    		.first();
						if(mUser==null) {
							mUser = new MUser(bpartner);
						}
						if(user.has("Name"))
							mUser.setName(user.get("Name").getAsString());
						if(user.has("Value"))
							mUser.setValue(user.get("Value").getAsString());
						if(user.has("EMail"))
							mUser.setEMail(user.get("EMail").getAsString());
						if(user.has("Phone"))
							mUser.setPhone(user.get("Phone").getAsString());
						 if(user.has("C_Greeting_ID"))
						    mUser.setC_Greeting_ID(findId(user, "C_Greeting_ID"));
						mUser.saveEx();
					}
			    }
			    
			    if(bp.has("C_BPartner_Location")) {
					JsonArray bplocations = bp.getAsJsonArray("C_BPartner_Location");
					for (JsonElement location : bplocations) {
						JsonObject bplocation = location.getAsJsonObject();;
						String bpLocationName = bplocation.get("Name").getAsString();
						MBPartnerLocation bpartnerLocation = new Query(Env.getCtx(), MBPartnerLocation.Table_Name, "c_bpartner_id=? and C_BPartner_LocationRef_ID=?", get_TrxName())
					    		.setParameters(bpartner.getC_BPartner_ID(), bplocation.get("id").getAsInt())
					    		.first();
						if(bpartnerLocation==null) {
							bpartnerLocation = new Query(Env.getCtx(), MBPartnerLocation.Table_Name, "c_bpartner_id=? and name=?", get_TrxName())
						    		.setParameters(bpartner.getC_BPartner_ID(), bpLocationName)
						    		.first();
						}
						
						JsonObject address = bplocation.getAsJsonObject("C_Location_ID");
						MLocation mAddress = null;
						
						if(bpartnerLocation!=null && bpartnerLocation.getC_Location_ID()>0) {
							mAddress = (MLocation) bpartnerLocation.getC_Location();
						}else {
							mAddress = new MLocation(Env.getCtx(), 0, get_TrxName());
						}
						if(address.has("C_Country_ID"))
							mAddress.setC_Country_ID(findId(address, "C_Country_ID"));
						if(address.has("Address1"))
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

						if(bplocation.has("BPR_Village_ID"))
							bpartnerLocation.set_ValueOfColumn("BPR_Village_ID", findId(bplocation, "BPR_Village_ID"));

						if(bplocation.has("Phone"))
							bpartnerLocation.setPhone(bplocation.get("Phone").getAsString());
						
						bpartnerLocation.setIsPreserveCustomName(bplocation.get("IsPreserveCustomName").getAsBoolean());
						bpartnerLocation.setIsShipTo(bplocation.get("IsShipTo").getAsBoolean());
						bpartnerLocation.setIsActive(bplocation.get("IsActive").getAsBoolean());
						bpartnerLocation.set_ValueOfColumn("C_BPartner_LocationRef_ID", BigDecimal.valueOf(bplocation.get("id").getAsInt()));
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

	private int checkSalesRegion(JsonObject bp) {
		JsonObject salesRegion = bp.getAsJsonObject("C_SalesRegion_ID");
		String identifier = String.valueOf(salesRegion.get("identifier"));
		String cleanedIdentifier = identifier.replace("\"", "");
		C_SalesRegion_ID = DB.getSQLValue(get_TrxName(), "select C_SalesRegion_ID from C_SalesRegion where Name = ?", cleanedIdentifier);
		if(C_SalesRegion_ID<=0) {
			MSalesRegion sr = new MSalesRegion(getCtx(), 0, get_TrxName());
			sr.setName(cleanedIdentifier);
			sr.setValue(cleanedIdentifier);
			sr.saveEx();
			C_SalesRegion_ID = sr.getC_SalesRegion_ID();
		}
		return C_SalesRegion_ID;
	}
	
	private int cekC_BP_Group(JsonObject bp) {
		JsonObject BP_Group = bp.getAsJsonObject("C_BP_Group_ID");
		String identifier = String.valueOf(BP_Group.get("identifier"));
		String cleanedIdentifier = identifier.replace("\"", "");
		C_BP_Group_ID = DB.getSQLValue(get_TrxName(), "select C_BP_Group_ID from C_BP_Group where Name = ?", cleanedIdentifier);
		if(C_BP_Group_ID<=0) {			
			MBPGroup bpg = new MBPGroup(getCtx(), 0, get_TrxName());
			bpg.setName(cleanedIdentifier);
			bpg.setValue(cleanedIdentifier);
			bpg.saveEx();
			C_BP_Group_ID = bpg.getC_BP_Group_ID();
		}
		return C_BP_Group_ID;
	}

	private int findSalesRep(JsonObject bp) {
		
		JsonObject SalesRep = bp.getAsJsonObject("SalesRep_ID");
		int a =SalesRep.get("id").getAsInt();
		MUser sales = new Query(Env.getCtx(), MUser.Table_Name, " AD_UserRef_ID=?", get_TrxName())
	    		.setParameters(SalesRep.get("id").getAsInt())
	    		.first();
		if(sales!=null) {
			sales.setName(SalesRep.get("Name").getAsString());
			if(SalesRep.has("Value")) 
				sales.setValue(SalesRep.get("Value").getAsString());
			if(SalesRep.has("EMail"))
				sales.setEMail(SalesRep.get("EMail").getAsString());
			if(SalesRep.has("Phone"))
				sales.setPhone(SalesRep.get("Phone").getAsString());
			 if(SalesRep.has("C_Greeting_ID"))
				 sales.setC_Greeting_ID(findId(SalesRep, "C_Greeting_ID"));
			sales.saveEx();
			return sales.getAD_User_ID();
		}
	
		MUser user = new MUser(getCtx(),0,get_TrxName());
		user.setName(SalesRep.get("Name").getAsString());
		user.set_ValueOfColumn("AD_UserRef_ID", BigDecimal.valueOf(SalesRep.get("id").getAsInt()));
		if(SalesRep.has("Value")) 
			user.setValue(SalesRep.get("Value").getAsString());
		if(SalesRep.has("EMail"))
			user.setEMail(SalesRep.get("EMail").getAsString());
		if(SalesRep.has("Phone"))
			user.setPhone(SalesRep.get("Phone").getAsString());
		 if(SalesRep.has("C_Greeting_ID"))
			 user.setC_Greeting_ID(findId(SalesRep, "C_Greeting_ID"));
		user.saveEx();
		
		return user.getAD_User_ID();
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
