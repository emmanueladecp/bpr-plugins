package com.idempierecloud.bpr.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MSysConfig;
import org.compiere.util.CLogger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RestService {
	
	private JsonObject payload = null;
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
	
	public JsonObject get(String endpoint, String query)
	{
		this.endpoint = endpoint;
//		try {
//			query = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
		this.endpoint += "?"+query;
		return run("GET");
	}
	
	public JsonObject run()
	{
		return run("POST");
	}
	
	public JsonObject run(String method)
	{
		HttpURLConnection connection = null;
		
		if(payload!=null)
			log.info("Payload : "+payload.toString());

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
			if(payload!=null) {
				DataOutputStream wr = new DataOutputStream (
				    connection.getOutputStream());
				wr.writeBytes(payload.toString());
				wr.close();
			}
			
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
			    return  new Gson().fromJson(response.toString(), JsonObject.class);
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
	
	public static String encodeQuery(String query)
	{
		try {
			return URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
