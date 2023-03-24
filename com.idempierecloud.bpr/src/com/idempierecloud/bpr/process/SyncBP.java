package com.idempierecloud.bpr.process;

import java.sql.Timestamp;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import com.idempierecloud.bpr.util.RestService;

public class SyncBP extends SvrProcess {

	private String endpoint = null;
	private String token = null;
	private String value = null;
	private Timestamp created = null;
	
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("endpoint")) 
				endpoint = para[i].getParameterAsString();
			else if (name.equals("token")) 
				token = para[i].getParameterAsString();
			else if (name.equals("value")) 
				value = para[i].getParameterAsString();
			else if (name.equals("Created")) 
				created = para[i].getParameterAsTimestamp();
		}
	}

	@Override
	protected String doIt() throws Exception {
		if(endpoint==null || token==null)
			return "No Rest Info Provided";
		
		RestService rest = new RestService(endpoint, token);
		
		
		return "Processed";
	}

}
