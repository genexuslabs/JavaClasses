package com.genexus.internet;

import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.text.ParseException;

import org.json.JSONArray;
import com.genexus.internet.IGxJSONAble;
import com.genexus.xml.GXXMLSerializable;
import com.genexus.internet.IGxJSONSerializable;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class GXRestAPIClient {

	public static final ILogger logger = LogManager.getLogger(GXRestAPIClient.class);
	private HttpClient httpClient;
	
	private String name;
	private Location location;
	private int protocol = 1;
	private String httpMethod = "GET";
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
	private HashMap<String, String> responseData = new HashMap<String, String>();

	static final String DATE_FMT = "yyyy-MM-dd";
	static final String DATETIME_FMT = "yyyy-MM-dd'T'HH:mm:ss";
	static final String DATETIME_FMT_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	static final String DATE_NULL = "0000-00-00";
	static final String DATETIME_NULL = "0000-00-00T00:00:00";

	/* Error constants */
   	static final int RESPONSE_ERROR_CODE = 2;
	static final int PARSING_ERROR_CODE = 3;
	static final int DESERIALIZING_ERROR_CODE = 4;
	
	static final String RESPONSE_ERROR_MSG = "Invalid response";
	static final String PARSING_ERROR_MSG = "Error parsing response";
	static final String DESERIALIZING_ERROR_MSG = "Error serializing/deserializing object";
			
	public GXRestAPIClient() { 
		responseCode = 0;
		responseMessage = "";
		httpClient = new HttpClient();
		location = new Location();		
	}

	/* Gets */

	public String getName() {
		return name;
	}

	public Location getLocation() {
		return location;
	}

	public int getProtocol() {
		return protocol;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public int getStatusCode() {
		return responseCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getStatusMessage() {
		return responseMessage;
	}

	/* Sets */

	public void setName( String value) {
		name = value;
	}

	public void setLocation( Location value) {
		location = value;
	}

	public void setProtocol( int value) {
		protocol = value;
	}

	public void setHttpMethod( String value) {
		httpMethod =  value;
	}

	public void setStatusCode( int value) {
		responseCode = value;
	}

	public void setStatusMessage(String value) {
		responseMessage = value;
	}

	public void setErrorCode( int value) {
		errorCode = value;
	}
	
	public void setErrorMessage( String value) {
		errorMessage = value;
	}


	public void addQueryVar(String varName, String varValue) {
		queryVars.put(varName, GXutil.URLEncode(varValue));
	}

	public void addQueryVar(String varName, int varValue) {
		queryVars.put(varName, Integer.toString(varValue));
	}

	public void addQueryVar(String varName, short varValue)	{
		queryVars.put(varName, String.valueOf(varValue));
	}
		
	public void addQueryVar(String varName, long varValue)	{
		queryVars.put(varName, String.valueOf(varValue));
	}

	public void addQueryVar(String varName, double varValue) {
		queryVars.put(varName, Double.toString(varValue));
	}
	
	public void addQueryVar(String varName, Date varValue) {		
		SimpleDateFormat df = new SimpleDateFormat(DATE_FMT);
		queryVars.put(varName,  df.format(varValue));
	}

	public void addQueryVar(String varName, Date varValue, boolean hasMilliseconds) {
		String fmt = DATETIME_FMT;
		if (hasMilliseconds)
			fmt = DATETIME_FMT_MS;
		SimpleDateFormat df = new SimpleDateFormat(fmt);
		queryVars.put(varName,  df.format(varValue));
	}

	public void addQueryVar(String varName, java.util.UUID varValue) {
		queryVars.put(varName, varValue.toString());
	}
	
	public void addQueryVar(String varName, Boolean varValue) {
		queryVars.put(varName, varValue.toString());
	}

	public void addQueryVar(String varName, BigDecimal varValue) {
		queryVars.put(varName, varValue.toString());
	}

	public void addQueryVar(String varName, GXXMLSerializable varValue) {
		if ( varValue != null) {
			queryVars.put(varName, varValue.toJSonString(false));
		}			
	}
	
	public void addQueryVar(String varName, IGxJSONSerializable varValue) {
		queryVars.put(varName, GXutil.URLEncode(varValue.toJSonString()));
	}

	private String quoteString(String value) {
		return "\"" + value + "\"";
	}

	public <T extends GXXMLSerializable> void addBodyVar(String varName, GXBaseCollection<T> varValue) {
		if ( varValue != null) {
			bodyVars.put(varName, varValue.toJSonString(false));
		}
	}

	public void addBodyVar(String varName, GXXMLSerializable varValue) {
		if ( varValue != null) {
			bodyVars.put(varName, varValue.toJSonString(false));
		}
	}
	
	public void addBodyVar(String varName, String varValue) {
		bodyVars.put( varName, quoteString(varValue));			
	}

	public void addBodyVar(String varName, double varValue) {
		bodyVars.put(varName, quoteString(Double.toString(varValue)));
	}

	public void addBodyVar(String varName, Date varValue) {		
		SimpleDateFormat df = new SimpleDateFormat(DATE_FMT);
		bodyVars.put(varName,  quoteString(df.format(varValue)));
	}

	public void addBodyVar(String varName, Date varValue, boolean hasMilliseconds) {
		String fmt = DATETIME_FMT;
		if (hasMilliseconds)
			fmt = DATETIME_FMT_MS;
		SimpleDateFormat df = new SimpleDateFormat(fmt);
		bodyVars.put( varName, quoteString(df.format(varValue)));
	}

	public void addBodyVar(String varName, short varValue) {
		bodyVars.put( varName, Short.toString(varValue));
	}

	public void addBodyVar(String varName, int varValue) {
		bodyVars.put( varName, Integer.toString(varValue));
	}

	public void addBodyVar(String varName, long varValue) {
		bodyVars.put( varName, Long.toString(varValue));
	}

	public void addBodyVar(String varName, Boolean varValue) {
		bodyVars.put( varName, varValue.toString());
	}

	public void addBodyVar(String varName, BigDecimal varValue) {
		bodyVars.put( varName, varValue.toString());
	}

	public void addBodyVar(String varName, java.util.UUID varValue) {
		bodyVars.put( varName, quoteString(varValue.toString()));
	}

	public void addBodyVar(String varName, IGxJSONSerializable varValue) {
		bodyVars.put( varName, quoteString(varValue.toJSonString()));
	}

	public String getBodyString(String varName) {
		return  getJsonStr(varName);
	}

	public Date getBodyDate(String varName) {	
		try {
			String val = getJsonStr(varName);
			if (val.startsWith(DATE_NULL))
				return CommonUtil.newNullDate();
			else	
			return new SimpleDateFormat(DATE_FMT).parse(val);
		}
		catch (ParseException e) {
		    return CommonUtil.newNullDate();
		}
	}

	public Date getBodyDateTime(String varName, Boolean hasMilliseconds) {	
		try{
			String val = getJsonStr(varName);
			String fmt = DATETIME_FMT;
			if (hasMilliseconds)
				fmt = DATETIME_FMT_MS;
			
			if (val.startsWith(DATETIME_NULL))
				return CommonUtil.newNullDate();
			else	
				return new SimpleDateFormat(fmt).parse(val);
		}
		catch (ParseException e) {
		    return CommonUtil.newNullDate();
		}
	}

	public boolean getBodyBool(String varName) {			
		return  Boolean.parseBoolean(getJsonStr(varName));
	}

	public java.util.UUID getBodyGuid(String varName) {			
		return java.util.UUID.fromString(getJsonStr(varName));
	}

	public < T extends IGxJSONSerializable> T getBodyGeospatial(String varName, Class<T> sdtClass) {			
		T sdt;
		try {
            sdt = sdtClass.newInstance();
			sdt.fromJSonString(getJsonStr(varName));
			return sdt;
        }
		catch (Exception e) {
			errorCode = DESERIALIZING_ERROR_CODE;
			errorMessage = DESERIALIZING_ERROR_MSG;
			logError(DESERIALIZING_ERROR_CODE, DESERIALIZING_ERROR_MSG + " " + sdtClass, e);
			return null;
		}		
	}

	public Double getBodyDouble(String varName) {
		return Double.parseDouble(getJsonStr(varName));
	}

	public BigDecimal	getBodyNum(String varName) {
		return new BigDecimal(getJsonStr(varName));
	}

	public long getBodyLong(String varName) {
		long value =0;
		try{
			value =  Long.parseLong(getJsonStr(varName));
		}
		catch(NumberFormatException ex)
		{
			value = Double.valueOf(getJsonStr(varName)).longValue();
		}
		return value;
	}

	public int getBodyInt(String varName) {
		return Integer.parseInt(getJsonStr(varName));
	}

	public short getBodyShort(String varName) {
		return Short.parseShort(getJsonStr(varName));
	}

	public String getJsonStr(String varName) {
		String jsonstr = "";
		try {
			if (jsonResponse != null) {
				if (jsonResponse.has(varName))
					jsonstr = jsonResponse.getString(varName);
				else if (jsonResponse.length() == 1 && jsonResponse.has(""))
					jsonstr = jsonResponse.getString("");								
			}
			else {
				errorCode = RESPONSE_ERROR_CODE;
				errorMessage = RESPONSE_ERROR_MSG;
				logError(RESPONSE_ERROR_CODE, RESPONSE_ERROR_MSG);
			}
		}
		catch( JSONException e) {
			errorCode = PARSING_ERROR_CODE;
			errorMessage = PARSING_ERROR_MSG;
			logError(PARSING_ERROR_CODE, PARSING_ERROR_MSG, e);
		}
		return jsonstr;	
	}

	public <T extends GXXMLSerializable> T getBodyObj(String varName, Class<T> sdtClass) {	
		T sdt;
		try {
            sdt = sdtClass.newInstance();
        } 
		catch (InstantiationException e) {
            return null;
        } 
		catch (IllegalAccessException e) {
            return null;
        }
		try {
			
			if (jsonResponse != null) {
				Boolean dSuccess = false;
				if (jsonResponse.has(varName) && jsonResponse.length() == 1) {
					dSuccess = sdt.fromJSonString(jsonResponse.getString(varName), null);
				} 
				else if (jsonResponse.length() == 1 && jsonResponse.has("")) {
					dSuccess = sdt.fromJSonString(jsonResponse.getString(""), null);
				} 
				else if (jsonResponse.length()>= 1) {
					dSuccess = sdt.fromJSonString(httpClient.getString(), null);			
				}
				if (!dSuccess)
				{
					errorCode = RESPONSE_ERROR_CODE;
					errorMessage = RESPONSE_ERROR_MSG;
					logError( RESPONSE_ERROR_CODE, RESPONSE_ERROR_MSG + " " + sdtClass);
					return null;
				}
			}
			else {
				errorCode = RESPONSE_ERROR_CODE;
				errorMessage = RESPONSE_ERROR_MSG;
				logError( RESPONSE_ERROR_CODE,RESPONSE_ERROR_MSG + " " + sdtClass);
				return null;
			}
		} 
		catch (JSONException e) {
			errorCode = PARSING_ERROR_CODE;
			errorMessage = PARSING_ERROR_MSG;
			logError(PARSING_ERROR_CODE, PARSING_ERROR_MSG + " " + sdtClass, e);
			return null;
		}
		return sdt;
	}

	public <T extends GxSilentTrnSdt> GXBCCollection<T> getBodyBCCollection(String varName, Class<T> elementClass) {
		GXBCCollection<T> col = new GXBCCollection<T>();
		fillCollection(varName,elementClass, col);
		return col;
	}

	public <T extends GXXMLSerializable> GXBaseCollection<T> getBodyObjCollection(String varName, Class<T> elementClass) {
		GXBaseCollection<T> col = new GXBaseCollection<T>();
		fillCollection(varName, elementClass, col);
		return col;
	}
	private <T extends GXXMLSerializable>  void fillCollection(String varName, Class<T> elementClass, GXBaseCollection col) {
		JSONArray jsonarr = new JSONArray();
		try {			
			if (jsonResponse.has(varName))
				jsonarr = jsonResponse.getJSONArray(varName);
			else if (jsonResponse.length() == 1 && jsonResponse.has(""))
				jsonarr = jsonResponse.getJSONArray("");

			if (jsonarr != null) {
				for (int i=0; i < jsonarr.length(); i++) {
					JSONObject o = jsonarr.getJSONObject(i);
					T sdt = elementClass.newInstance();
					sdt.fromJSonString(o.toString(),null);
					col.add(sdt);
				}
			}
			else {
				errorCode = RESPONSE_ERROR_CODE;
				errorMessage = RESPONSE_ERROR_MSG;
				logError(RESPONSE_ERROR_CODE,RESPONSE_ERROR_MSG + " " + elementClass);
			}	
		} 
		catch (JSONException e) {
			errorCode = PARSING_ERROR_CODE;
			errorMessage = PARSING_ERROR_MSG;
			logError(PARSING_ERROR_CODE,PARSING_ERROR_MSG + " " + elementClass ,e );
		}
		catch (Exception e) {
			errorCode = DESERIALIZING_ERROR_CODE;
			errorMessage = DESERIALIZING_ERROR_MSG;
			logError(DESERIALIZING_ERROR_CODE, DESERIALIZING_ERROR_MSG + " " + elementClass, e);
		}
	}

	public <T extends Object> GXSimpleCollection<T> getBodyCollection(String varName, Class<T> elementClasss) {	
		GXSimpleCollection<T> coll;
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
		catch (JSONException e) {
			errorCode = PARSING_ERROR_CODE;
			errorMessage = PARSING_ERROR_MSG;
			logError(PARSING_ERROR_CODE,PARSING_ERROR_MSG + " " + elementClasss, e);
		}
		return coll;
	}

	public void addUploadFile(String filePath, String fileName) {		
		httpClient.addFile(filePath, fileName);
		String mimeType = SpecificImplementation.Application.getContentType(filePath);
		contentType = mimeType;
	}
	
	public void RestExecute() {
		String separator = "";
		queryString = "";
		if (queryVars.size() > 0) {
			separator = "?";
			for( Map.Entry<String, String> entry : queryVars.entrySet()) {
				queryString += String.format("%s%s=%s", separator, entry.getKey(), entry.getValue());
				separator = "&";
			}
		}
		bodyString = "";
		if (bodyVars.size() > 0) {
			separator = "";
			for( Map.Entry<String, String> entry : bodyVars.entrySet()) {
    			bodyString +=  separator + "\"" + entry.getKey() + "\":" + entry.getValue() + "";
				separator = ",";
			}
		}
		if (bodyString.length() > 0) {
			bodyString = "{" + bodyString + "}";
			httpClient.addString( bodyString);
			httpClient.addHeader( "Content-Type", contentType);
		}
		else {
			if (httpMethod == "POST" || httpMethod == "PUT") {
				bodyString = "{}";
				httpClient.addString(bodyString);
				httpClient.addHeader("Content-Type", contentType);
			}
		}
		if (location.getAuthenticationMethod() == 4 && location.getAccessToken() != null &&  ! location.getAccessToken().trim().isEmpty())  {
			httpClient.addHeader("Authorization", location.getAccessToken());
		}
		String serviceuri = ((location.getSecure() > 0) ? "https" : "http") + "://" + location.getHost();
		serviceuri += (location.getPort() != 80) ? ":" + Integer.toString(location.getPort()): "";
		serviceuri += "/" + location.getBaseURL() + "/" + location.getResourceName();
		serviceuri += queryString;
		httpClient.execute( this.httpMethod, serviceuri);
		responseCode = httpClient.getStatusCode();
		responseMessage = httpClient.getReasonLine();
		errorCode = 0;
		errorMessage = "";
		if (responseCode >= 300 || httpClient.getErrCode() > 0) {
			errorCode =  httpClient.getErrCode();
			errorMessage = httpClient.getErrDescription();
		}
		else {
			try {
				String response = httpClient.getString();
				if (response.trim().startsWith("["))
				{
					// unwrapped list response
					response = "{\"\":" + response + "}";
				}
				jsonResponse = new JSONObject(response);
			}
			catch( JSONException e) {
				errorCode = PARSING_ERROR_CODE;
				errorMessage = PARSING_ERROR_MSG;
				logError(PARSING_ERROR_CODE, PARSING_ERROR_MSG, e);
				jsonResponse = new JSONObject();
			}
		}
	}

	private void logError(int code, String msg)	{
		logger.error("Error: " + Integer.toString(code) + " " + msg);
	}
	private void logError(int code, String msg, Exception e)	{
		logger.error("Error: " + Integer.toString(code) + " " + msg, e);
	}
}
