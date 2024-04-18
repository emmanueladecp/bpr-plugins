package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCity;
import org.compiere.model.MLocation;
import org.compiere.model.MRegion;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;
import com.idempierecloud.bpr.model.X_BPR_District;
import com.idempierecloud.bpr.model.X_BPR_Village;

public class CBPartnerLocationEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(CBPartnerLocationEvent.class);
	
	private MBPartnerLocation bpLocation = null;

	@Override
	protected void doHandleEvent(PO po, Event event) {
		log.fine("bp location Event : "+event.getTopic());
		
		bpLocation = (MBPartnerLocation) po;
		if(event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			updateLocation();
		}else if(event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)) {
			updateLocation();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_NEW)) {
			setLocation();
		}else if(event.getTopic().equals(IEventTopics.PO_BEFORE_CHANGE)) {
			setLocation();
		}
	}
	
	private void setLocation() {
		int c_location_id = DB.getSQLValue(bpLocation.get_TrxName(), "select c_location_id from c_location cl "
				+ " where isactive = 'Y' and address1 like ? and C_Region_ID = ? and C_City_ID = ? and bpr_district_id =?",
				 bpLocation.getName(),bpLocation.get_ValueAsInt("C_Region_ID"),bpLocation.get_ValueAsInt("C_City_ID"), bpLocation.get_ValueAsInt("bpr_district_id"));
		if(c_location_id<=0) {
			MRegion region = new MRegion(bpLocation.getCtx(), bpLocation.get_ValueAsInt("C_Region_ID"), bpLocation.get_TrxName());
			MCity city = new MCity(bpLocation.getCtx(), bpLocation.get_ValueAsInt("C_City_ID"), bpLocation.get_TrxName());
			X_BPR_District district = new X_BPR_District(bpLocation.getCtx(), bpLocation.get_ValueAsInt("bpr_district_id"), bpLocation.get_TrxName());			
			MLocation loc = new MLocation(bpLocation.getCtx(), 0, bpLocation.get_TrxName());			
			loc.setAddress1(bpLocation.getName());
			loc.setAddress2(district.getName());
			loc.setAddress4(city.getName());
			loc.setAddress5(region.getName());
			loc.setC_Country_ID(bpLocation.get_ValueAsInt("C_Country_ID"));
			loc.setC_Region_ID(bpLocation.get_ValueAsInt("C_Region_ID"));
			loc.setC_City_ID(bpLocation.get_ValueAsInt("C_City_ID"));
			if(bpLocation.get_ValueAsInt("BPR_Village_ID")>0) {
				X_BPR_Village village = new X_BPR_Village(bpLocation.getCtx(), bpLocation.get_ValueAsInt("bpr_village_id"), bpLocation.get_TrxName());
				loc.setAddress3(village.getName());
				loc.set_ValueOfColumn("BPR_Village_ID", village.getBPR_Village_ID());
			}	
			loc.set_ValueOfColumn("BPR_District_ID", bpLocation.get_ValueAsInt("BPR_District_ID"));
			loc.setIsActive(true);
			loc.save();
			c_location_id = loc.getC_Location_ID();
		}
		bpLocation.setC_Location_ID(c_location_id);
	}

	private void updateLocation() {
		if(bpLocation.getC_Location_ID()==0)
			return;
		
		MLocation location = (MLocation) bpLocation.getC_Location();
		location.setAddress1(bpLocation.getName());
		location.saveEx();
	}
	
	

	@Override
	protected void doHandleEvent() {
		
	}

}
