package com.idempierecloud.bpr.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MSysConfig;
import org.compiere.util.CLogger;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.json.parser.JSONParser;

public class RestService {
	
	private JSONObject payload = null;
	private String endpoint = "";
	private String message = "";
	private boolean isError = false;
	private CLogger log;
	private String rest_host,rest_token;
	
	public RestService()
	{
		log = CLogger.getCLogger(RestService.class);
		rest_host = MSysConfig.getValue("REST_HOST");
		rest_token = MSysConfig.getValue("REST_AUTH_TOKEN");
		if(rest_host==null || rest_token==null)
			throw new AdempiereException("No Rest Service Value on Sys Config");
	}
	

	public RestService(String endpoint, String token)
	{
		log = CLogger.getCLogger(RestService.class);
		rest_host = endpoint;
		rest_token = token;
		if(rest_host==null || rest_token==null)
			throw new AdempiereException("No Rest Service Value on Sys Config");
	}
	
	public boolean isError()
	{
		return isError;
	}
	
	public String getMessage()
	{
		return message;
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
		
		payload = new JSONObject();
		payload.put("ModelCRUDRequest", request);
		
		endpoint = "model_adservice/create_data";
	}
	
	public JSONObject get(String endpoint, JSONObject payload)
	{
		this.payload = payload;
		this.endpoint += "/api/v1/"+endpoint;
		return run("GET");
	}
	
	public JSONObject run()
	{
		return run("POST");
	}
	
	public JSONObject run(String method)
	{
		HttpURLConnection connection = null;
		
		log.info("Payload : "+payload.toJSONString());

		try {
		    //Create connection
			URL url = new URL(rest_host+"/api/v1/"+endpoint);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setRequestProperty("Content-Type", 
			"application/json");
			connection.setRequestProperty("Accept", 
			"application/json");
			connection.setRequestProperty("Authorization","Bearer "+rest_token);
			
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
			    return (JSONObject) parser.parse(response.toString());
			  } catch (Exception e) {
			    e.printStackTrace();
			    isError = true;
			    message = e.getLocalizedMessage();
			  } finally {
			    if (connection != null) {
			      connection.disconnect();
			  }
		}
		return null;
	}

}
