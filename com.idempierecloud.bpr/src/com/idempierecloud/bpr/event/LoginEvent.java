package com.idempierecloud.bpr.event;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MRole;
import org.compiere.model.MRoleOrgAccess;
import org.compiere.model.MUserOrgAccess;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import com.idempierecloud.bpr.base.CustomEvent;

public class LoginEvent extends CustomEvent {

	private static CLogger log = CLogger.getCLogger(LoginEvent.class);

	@Override
	protected void doHandleEvent(Event event) {
		if(event.getTopic().equals(IEventTopics.AFTER_LOGIN)) {
			log.info("BPR Plugin Activated");
			
			MRole role = new MRole(Env.getCtx(), Env.getAD_Role_ID(Env.getCtx()),null);
			if(role.isAccessAllOrgs())
				return;
			
			if(role.isUseUserOrgAccess()) {
				MUserOrgAccess orgAccess = new Query(Env.getCtx(), MUserOrgAccess.Table_Name, "AD_User_ID=? AND AD_Org_ID=?", null)
						.setParameters(Env.getAD_User_ID(Env.getCtx()), Env.getAD_Org_ID(Env.getCtx()))
						.first();
				if(orgAccess==null || orgAccess.isReadOnly())
					throw new AdempiereException("Anda tidak bisa login menggunakan org ini");
				
				return;
			}
			MRoleOrgAccess orgAccess = new Query(Env.getCtx(), MRoleOrgAccess.Table_Name, "AD_Role_ID=? AND AD_Org_ID=?", null)
					.setParameters(Env.getAD_Role_ID(Env.getCtx()), Env.getAD_Org_ID(Env.getCtx()))
					.first();
			
			if(orgAccess==null || orgAccess.isReadOnly())
				throw new AdempiereException("Anda tidak bisa login menggunakan org ini");
		}
	}

	@Override
	protected void doHandleEvent() {
		
	}

}
