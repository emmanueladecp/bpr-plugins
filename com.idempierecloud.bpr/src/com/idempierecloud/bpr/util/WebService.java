package com.idempierecloud.bpr.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.compiere.util.CLogger;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.json.parser.JSONParser;

public class WebService {
	
	private JSONObject payload = null;
	private String endpoint = "";
	private String message = "";
	private boolean isError = false;
	private CLogger log;
	
	public WebService()
	{
		log = CLogger.getCLogger(WebService.class);
	}
	
	public boolean isError()
	{
		return isError;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	private JSONObject getLogin()
	{
		JSONObject login = new JSONObject();
		login.put("user", "belitangAdmin");
		login.put("pass", "belitangAdmin");
		login.put("lang", "en_US");
		login.put("ClientID", "1000003");
		login.put("RoleID", "1000006");
		login.put("OrgID", "0");
		login.put("WarehouseID", "0");
		login.put("stage", "9");
		
		return login;
	}
	
	public void createData(String serviceType, String tableName, String[] columns, String[] values)
	{
		JSONArray fields = new JSONArray();
		for(int i = 0;i<columns.length; i++) {
			JSONObject field = new JSONObject();
			field.put("@column", columns[i]);
			field.put("val", values[i]);
			fields.add(field);
		}
		
		JSONObject field = new JSONObject();
		field.put("field", fields);
		
		
		JSONObject crud = new JSONObject();
		crud.put("serviceType", serviceType);
		crud.put("TableName", tableName);
		crud.put("Action", "Create");
		crud.put("DataRow", field);
		
		JSONObject request = new JSONObject();
		request.put("ModelCRUD", crud);
		request.put("ADLoginRequest", getLogin());
		
		payload = new JSONObject();
		payload.put("ModelCRUDRequest", request);
		
		endpoint = "model_adservice/create_data";
	}
	
	public String run()
	{
		HttpURLConnection connection = null;
		
		log.info("Payload : "+payload.toJSONString());

		try {
		    //Create connection
			URL url = new URL("http://testing.sep-food.com:8080/ADInterface/services/rest/"+endpoint);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", 
			"application/json");
			connection.setRequestProperty("Accept", 
			"application/json");
			
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			
			//Send request
			DataOutputStream wr = new DataOutputStream (
			    connection.getOutputStream());
			wr.writeBytes(payload.toJSONString());
			wr.close();
			
			//Get Response  
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
			  response.append(line);
			  response.append('\r');
			    }
			    rd.close();
			    
			    log.info("Result "+response.toString());
			    JSONParser parser = new JSONParser();
			    JSONObject result = (JSONObject) parser.parse(response.toString());
			    if(result.containsKey("StandardResponse")) {
			    	JSONObject standardResponse = (JSONObject) result.get("StandardResponse");
			    	if(standardResponse.containsKey("@IsError")) {
			    		message = (String) standardResponse.get("Error");
			    		isError = true;
			    	}

		    		return null;
			    }
			  } catch (Exception e) {
			    e.printStackTrace();
			    return e.getLocalizedMessage();
			  } finally {
			    if (connection != null) {
			      connection.disconnect();
			  }
		}
		return null;
	}

}
