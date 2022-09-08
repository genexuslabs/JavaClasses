package com.genexus.internet;

import java.util.*;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import com.genexus.internet.IGxJSONAble;
import com.genexus.internet.IGxGeoJSONSerializable;
import com.genexus.xml.GXXMLSerializable;
import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.*;

public class GXRestAPIClient{

	private HttpClient httpClient;
	
	private String name;
	private Location location;
	private String protocol = "REST";
	private String httpMethod = "GET";
	private int  statusCode;
	private int  errorCode;	
	private String errorMessage;
	private Integer responseCode;
	private String responseMessage;
	
	private String contentType = "application/json; charset=utf-8";
	private String queryString = "";
	private String bodyString = "";

	private JSONObject jsonResponse;
	private HashMap<String, String> queryVars = new HashMap<String, String>();
	private HashMap<String, String> bodyVars = new HashMap<String, String>();
	//private HashMap<String, String> pathVars = new HashMap<String, String>();
	private HashMap<String, String> responseData = new HashMap<String, String>();

	public GXRestAPIClient()
	{
		responseCode = 0;
		responseMessage = "";
		httpClient = new HttpClient();
		location = new Location();
		location.setBaseURL("api");
		location.setHost( "www.example.com");
		location.setResourceName("service");
		location.setPort(80);
	}

	/* Gets */

	public String getName() {
		return name;
	}

	public Location getLocation() {
		return location;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	/* Sets */

	public void setName( String value) {
		name = value;
	}

	public void setLocation( Location value) {
		location = value;
	}

	public void setProtocol( String value) {
		protocol = value;
	}

	public void setHttpMethod( String value) {
		httpMethod =  value;
	}

	public void setStatusCode( int value) {
		statusCode = value;
	}
	
	public void setErrorCode( int value) {
		errorCode = value;
	}
	
	public void setErrorMessage( String value) {
		errorMessage = value;
	}


	public void addQueryVar(String varName, String varValue)
	{
		queryVars.put(varName, varValue);
	}

	public void addQueryVar(String varName, int varValue)
	{
		queryVars.put(varName, Integer.toString(varValue));
	}

	public void addQueryVar(String varName, short varValue)
	{
		queryVars.put(varName, String.valueOf(varValue));
	}
		
	public void addQueryVar(String varName, double varValue)
	{
		queryVars.put(varName, Double.toString(varValue));
	}
	
	public void addQueryVar(String varName, Date varValue)
	{		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		queryVars.put(varName,  df.format(varValue));
	}

	public void addQueryVar(String varName, Date varValue, boolean hasMilliseconds)
	{
		String fmt = "yyyy-MM-dd'T'HH:mm:ss";
		if (hasMilliseconds)
			fmt = "yyyy-MM-dd'T'HH:mm:ss.SSS";
		SimpleDateFormat df = new SimpleDateFormat(fmt);
		queryVars.put(varName,  df.format(varValue));
	}

	public void addQueryVar(String varName, java.util.UUID varValue)
	{
		queryVars.put(varName, varValue.toString());
	}
	
	public void addQueryVar(String varName, IGxGeoJSONSerializable varValue)
	{
		queryVars.put(varName, varValue.toJSonString());
	}
	
	public void addQueryVar(String varName, Boolean varValue)
	{
		queryVars.put(varName, varValue.toString());
	}

	public void addQueryVar(String varName, java.math.BigDecimal varValue)
	{
		queryVars.put(varName, varValue.toString());
	}


	private String quoteString(String value)
	{
		return "\"" + value + "\"";
	}

	public <T extends GxSilentTrnSdt> void addBodyVarBC(String varName, GXBaseCollection<T> varValue)
	{
		if ( varValue != null)
		{
			bodyVars.put(varName, varValue.toJSonString(false));
		}
	}

	public <T extends GxUserType> void addBodyVar(String varName, GXBaseCollection<T> varValue)
	{
		if ( varValue != null)
		{
			bodyVars.put(varName, varValue.toJSonString(false));
		}
	}
	
	public void addBodyVar(String varName, GxSilentTrnSdt varValue)
	{
		if ( varValue != null)
		{
			bodyVars.put(varName, varValue.toJSonString(false));
		}			
	}
	
	public void addBodyVar(String varName, GxUserType varValue)
	{
		if ( varValue != null)
		{
			bodyVars.put(varName, varValue.toJSonString(false));
		}
	}

	public void addBodyVar(String varName, String varValue)
	{
		bodyVars.put( varName, quoteString(varValue));			
	}

	public void addBodyVar(String varName, double varValue)
	{
		bodyVars.put(varName, quoteString(Double.toString(varValue)));
	}

	public void addBodyVar(String varName, Date varValue)
	{		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		bodyVars.put(varName,  quoteString(df.format(varValue)));
	}

	public void addBodyVar(String varName, Date varValue, boolean hasMilliseconds)
	{
		String fmt = "yyyy-MM-dd'T'HH:mm:ss";
		if (hasMilliseconds)
			fmt = "yyyy-MM-dd'T'HH:mm:ss.SSS";
		SimpleDateFormat df = new SimpleDateFormat(fmt);
		bodyVars.put( varName, quoteString(df.format(varValue)));
	}

	public void addBodyVar(String varName, int varValue)
	{
		bodyVars.put( varName, Integer.toString(varValue));
	}

	public void addBodyVar(String varName, Boolean varValue)
	{
		bodyVars.put( varName, varValue.toString());
	}

	public void addBodyVar(String varName, java.math.BigDecimal varValue)
	{
		bodyVars.put( varName, varValue.toString());
	}

	public void addBodyVar(String varName, java.util.UUID varValue)
	{
		bodyVars.put( varName, quoteString(varValue.toString()));
	}

	public void addBodyVar(String varName, IGxGeoJSONSerializable varValue)
	{
		bodyVars.put( varName, quoteString(varValue.toJSonString()));
	}

	public String getBodyString(String varName)
	{
		return  getJsonStr(varName);
	}

	public Date getBodyDate(String varName)
	{	
		try{
			return new SimpleDateFormat("yyyy-MM-dd").parse(getJsonStr(varName));
		}
		catch (ParseException e)
		{
		    return CommonUtil.newNullDate();
		}
	}

	public Date getBodyDateTime(String varName, Boolean hasMilliseconds)
	{	
		try{
			String fmt = "yyyy-MM-dd'T'HH:mm:ss";
			if (hasMilliseconds)
				fmt = "yyyy-MM-dd'T'HH:mm:ss.SSS";
			return new SimpleDateFormat(fmt).parse(getJsonStr(varName));
		}
		catch (ParseException e)
		{
		    return CommonUtil.newNullDate();
		}
	}

	public boolean getBodyBool(String varName)
	{			
		return  Boolean.parseBoolean(getJsonStr(varName));
	}

	public java.util.UUID getBodyGuid(String varName)
	{			
		return java.util.UUID.fromString(getJsonStr(varName));
	}

	public < T extends IGxGeoJSONSerializable> T getBodyGeospatial(String varName, Class<T> sdtClass)
	{			
		T sdt;
		try {
            sdt = sdtClass.newInstance();
			sdt.fromJSonString(getJsonStr(varName));
			return sdt;
        }
		catch (IllegalAccessException e) {
			return null;
		}
		catch (InstantiationException e) {
            return null;
		}	
	}

	public Double getBodyNum(String varName)
	{
		return Double.parseDouble(getJsonStr(varName));
	}

	public int getBodyInt(String varName)
	{
		return Integer.parseInt(getJsonStr(varName));
	}

	public short getBodyShort(String varName)
	{
		return Short.parseShort(getJsonStr(varName));
	}

	public String getJsonStr(String varName)
	{
		String jsonstr = "";
		try{
			if (jsonResponse != null) {
				if (jsonResponse.has(varName))
					jsonstr = jsonResponse.getString(varName);
				else if (jsonResponse.length() == 1 && jsonResponse.has(""))
					jsonstr = jsonResponse.getString("");								
			}
			else {
				errorCode = 1;
				errorMessage = "Invalid response";				
			}
		}
		catch( JSONException e)
		{
			errorCode = 1;
			errorMessage = "Invalid response";						
		}
		return jsonstr;	
	}

	public <T extends GxSilentTrnSdt> T getBodySdtTrn(String varName, Class<T> sdtClass)
	{	
		T sdt;
		try {
            sdt = sdtClass.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
		try {
			if (jsonResponse != null) {
				if (jsonResponse.has(varName)) {
					sdt.fromJSonString(jsonResponse.getString(varName), null);
				} 
				else if (jsonResponse.length() == 1 && jsonResponse.has("")) {
					sdt.fromJSonString(jsonResponse.getString(""), null);
				} 
				else if (jsonResponse.length()>= 1 && !jsonResponse.has(varName))
				{
					sdt.fromJSonString(httpClient.getString(), null);
				}
			}
			else {
				errorCode = 1;
				errorMessage = "Invalid response";
				return null;
			}
		} 
		catch (json.org.json.JSONException e) {
			errorCode = 1;
			errorMessage = "Invalid response";				
			return null;
		}
		return sdt;
	}

	public <T extends GxUserType> T getBodySdt(String varName, Class<T> sdtClass)
	{	
		T sdt;
		try {
            sdt = sdtClass.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
		try {
			if (jsonResponse != null) {
				if (jsonResponse.has(varName)) {
					sdt.fromJSonString(jsonResponse.getString(varName), null);
				} 
				else if (jsonResponse.length() == 1 && jsonResponse.has("")) {
					sdt.fromJSonString(jsonResponse.getString(""), null);
				} 
				else if (jsonResponse.length()>= 1 && !jsonResponse.has(varName))
				{
					sdt.fromJSonString(httpClient.getString(), null);
				}
			}
			else {
				errorCode = 1;
				errorMessage = "Invalid response";
				return null;
			}
		} 
		catch (json.org.json.JSONException e) {
			errorCode = 1;
			errorMessage = "Invalid response";				
			return null;
		}
		return sdt;
	}

	
	public <T extends GXXMLSerializable> T getBodyObj(String varName, Class<T> sdtClass)
	{	
		T sdt;
		try {
            sdt = sdtClass.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
		try {
			if (jsonResponse != null) {
				if (jsonResponse.has(varName)) {
					sdt.fromJSonString(jsonResponse.getString(varName), null);
				} 
				else if (jsonResponse.length() == 1 && jsonResponse.has("")) {
					sdt.fromJSonString(jsonResponse.getString(""), null);
				} 
				else if (jsonResponse.length()>= 1 && !jsonResponse.has(varName))
				{
					sdt.fromJSonString(httpClient.getString(), null);
				}
			}
			else {
				errorCode = 1;
				errorMessage = "Invalid response";
				return null;
			}
		} 
		catch (json.org.json.JSONException e) {
			errorCode = 1;
			errorMessage = "Invalid response";				
			return null;
		}
		return sdt;
	}

	public <T extends GxSilentTrnSdt> GXBaseCollection<T> getBodySdtTrnCollection(String varName, Class<T> elementClasss)
	{			
		JSONArray jsonarr = new JSONArray();
		GXBaseCollection<T> col = new GXBaseCollection<T>();  
		try {			
			if (jsonResponse.has(varName))
				jsonarr = jsonResponse.getJSONArray(varName);
			else if (jsonResponse.length() == 1 && jsonResponse.has(""))
				jsonarr = jsonResponse.getJSONArray("");

			if (jsonarr != null) {
				for (int i=0; i < jsonarr.length(); i++) {
    				JSONObject o = jsonarr.getJSONObject(i);
					T sdt = elementClasss.newInstance();
					sdt.fromJSonString(o.toString(),null);
					col.add(sdt);
				}
			}
			else {
				errorCode = 1;
				errorMessage = "Invalid response";
			}	
		} 
		catch (json.org.json.JSONException e)
		{
			errorCode = 1;
			errorMessage = "Invalid response" +  e.toString();
		}
		catch (Exception e) {
			errorCode = 1;
			errorMessage = "Invalid response" + e.toString();
		}
		return col;
	}

	public <T extends GxUserType> GXBaseCollection<T> getBodySdtCollection(String varName, Class<T> elementClasss)
	{			
		JSONArray jsonarr = new JSONArray();
		GXBaseCollection<T> col = new GXBaseCollection<T>();  
		try {			
			if (jsonResponse.has(varName))
				jsonarr = jsonResponse.getJSONArray(varName);
			else if (jsonResponse.length() == 1 && jsonResponse.has(""))
				jsonarr = jsonResponse.getJSONArray("");

			if (jsonarr != null) {
				for (int i=0; i < jsonarr.length(); i++) {
    				JSONObject o = jsonarr.getJSONObject(i);
					T sdt = elementClasss.newInstance();
					sdt.fromJSonString(o.toString(),null);
					col.add(sdt);
				}
			}
			else {
				errorCode = 1;
				errorMessage = "Invalid response";
			}	
		} 
		catch (json.org.json.JSONException e)
		{
			errorCode = 1;
			errorMessage = "Invalid response" +  e.toString();
		}
		catch (Exception e) {
			errorCode = 1;
			errorMessage = "Invalid response" + e.toString();
		}
		return col;
	}

	public <T extends GXXMLSerializable> GXBaseCollection<T> getBodyObjCollection(String varName, Class<T> elementClasss)
	{			
		JSONArray jsonarr = new JSONArray();
		GXBaseCollection<T> col = new GXBaseCollection<T>();  
		try {			
			if (jsonResponse.has(varName))
				jsonarr = jsonResponse.getJSONArray(varName);
			else if (jsonResponse.length() == 1 && jsonResponse.has(""))
				jsonarr = jsonResponse.getJSONArray("");

			if (jsonarr != null) {
				for (int i=0; i < jsonarr.length(); i++) {
    				JSONObject o = jsonarr.getJSONObject(i);
					T sdt = elementClasss.newInstance();
					sdt.fromJSonString(o.toString(),null);
					col.add(sdt);
				}
			}
			else {
				errorCode = 1;
				errorMessage = "Invalid response";
			}	
		} 
		catch (json.org.json.JSONException e)
		{
			errorCode = 1;
			errorMessage = "Invalid response" +  e.toString();
		}
		catch (Exception e) {
			errorCode = 1;
			errorMessage = "Invalid response" + e.toString();
		}
		return col;
	}


	public <T extends Object> GXSimpleCollection<T> getBodyCollection(String varName, Class<T> elementClasss)
	{	
		GXSimpleCollection coll;
		try {
            coll = new GXSimpleCollection<T>();
        } 
		catch (Exception e) {
            return null;
        } 		
		try {
			if (jsonResponse.has(varName)) {
				coll.fromJSonString(jsonResponse.getString(varName), null);
			} 
			else if (jsonResponse.length() == 1 && jsonResponse.has("")) {
				coll.fromJSonString(jsonResponse.getString(varName), null);
			} 
		} 
		catch (json.org.json.JSONException e)
		{
			System.err.println( e.toString());
			return null;
		}
		return coll;
	}

	public void addUploadFile(String filePath, String fileName)
	{		
		httpClient.addFile(filePath, fileName);
		String mimeType = SpecificImplementation.Application.getContentType(filePath);
		contentType = mimeType;
	}
	
	public void RestExecute()
	{
		String separator = "";
		queryString = "";
		if (queryVars.size() > 0)
		{
			separator = "?";
			for( Map.Entry<String, String> entry : queryVars.entrySet()) 
			{				
				queryString += String.format("%s%s=%s", separator, entry.getKey(), entry.getValue());
				separator = "&";
			}
		}
		bodyString = "";
		if (bodyVars.size() > 0)
		{
			separator = "";
			for( Map.Entry<String, String> entry : bodyVars.entrySet()) {
    			bodyString +=  separator + "\"" + entry.getKey() + "\":" + entry.getValue() + "";
				separator = ",";
			}
		}
		if (bodyString.length() > 0)
		{
			bodyString = "{" + bodyString + "}";
			httpClient.addString( bodyString);
			httpClient.addHeader( "Content-Type", contentType);
		}
		else
		{
			if (this.httpMethod == "POST" || this.httpMethod == "PUT")
			{
				bodyString = "{}";
				httpClient.addString(bodyString);
				httpClient.addHeader("Content-Type", contentType);
			}
		}
		String serviceuri = ((this.location.getSecure() > 0) ? "https" : "http") + "://" + this.location.getHost();
		serviceuri += (this.location.getPort() != 80) ? ":" + Integer.toString(this.location.getPort()): "";
		serviceuri += "/" + this.location.getBaseURL() + "/" + this.location.getResourceName();
		serviceuri += queryString;			

		httpClient.execute( this.httpMethod, serviceuri);

		if (httpClient.getStatusCode() >= 300 || httpClient.getErrCode() > 0)
		{	

			errorCode = (httpClient.getErrCode() == 0)? 1 : httpClient.getErrCode();
			errorMessage = httpClient.getErrDescription();				
			statusCode = httpClient.getStatusCode();
		}
		else
		{
			statusCode = httpClient.getStatusCode();
			try{
				jsonResponse = new JSONObject(httpClient.getString());
			}
			catch(JSONException e)
			{
				System.out.println( e.toString());
				jsonResponse = new JSONObject();
			}
		}

	}
}
