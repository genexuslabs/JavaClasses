package com.genexus.security.web;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import json.org.json.IJsonFormattable;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public class WebSecureToken extends SecureToken {
	private static int TOKEN_DAYS_EXPIRATION = 15;
	private static String JSON_ISSUER_NAME = "gx-issuer";
	private static String JSON_EXPIRATION_NAME = "gx-exp";
	private static String JSON_PGMNAME_NAME = "gx-pgm";
	private static String JSON_VALUE_NAME = "gx-val";
	
	private String _issuer = "";
	private String _pgmName = "";
	private String _value = "";
	private Date _expiration;
	
	private JSONObject _jObj = new JSONObject();
	
	public WebSecureToken(){			
		_expiration = org.apache.commons.lang.time.DateUtils.addDays(new Date(), TOKEN_DAYS_EXPIRATION);
	}
	
	public WebSecureToken(String pgmName, String issuer, String value) {
		_expiration = org.apache.commons.lang.time.DateUtils.addDays(new Date(), TOKEN_DAYS_EXPIRATION);
		_pgmName = pgmName;
		_value = value;
		_issuer = issuer;
	}

	public String get_issuer() {
		return _issuer;
	}
	public void set_issuer(String _issuer) {
		this._issuer = _issuer;
	}
	public String get_pgmName() {
		return _pgmName;
	}
	public void set_pgmName(String _pgmName) {
		this._pgmName = _pgmName;
	}
	public String get_value() {
		return _value;
	}
	public void set_value(String _value) {
		this._value = _value;
	}
	public Date get_expiration() {
		return _expiration;
	}
	public void set_expiration(Date _expiration) {
		this._expiration = _expiration;
	}
	
	public void tojson()
	{
		AddObjectProperty(WebSecureToken.JSON_ISSUER_NAME, _issuer);
		AddObjectProperty(WebSecureToken.JSON_PGMNAME_NAME, _pgmName);
    	AddObjectProperty(WebSecureToken.JSON_VALUE_NAME, _value);
    	AddObjectProperty(WebSecureToken.JSON_EXPIRATION_NAME, Long.toString(_expiration.getTime()));
    }
	
    public void AddObjectProperty(String name, Object prop)
    {
    	String ptyValue = ((String)prop);
    	if (!StringUtils.isBlank(name) && !StringUtils.isBlank(ptyValue))
    	{
    		try {
    			_jObj.put(name, ptyValue);
			} 
    		catch (JSONException e) {
			}
    	}
    }
    
    public Object GetJSONObject()
    {
    	tojson();
    	return _jObj;
    }
    
    public Object GetJSONObject(boolean includeState)
    {
    	return GetJSONObject();
    }
    
    public void FromJSONObject(IJsonFormattable obj)
    {
    	JSONObject jObj = (JSONObject) obj;			
		this._issuer = getJsonPtyValueString(jObj, WebSecureToken.JSON_ISSUER_NAME);
		this._value = getJsonPtyValueString(jObj, WebSecureToken.JSON_VALUE_NAME);
		this._pgmName = getJsonPtyValueString(jObj, WebSecureToken.JSON_PGMNAME_NAME);
		String expLong = getJsonPtyValueString(jObj, WebSecureToken.JSON_EXPIRATION_NAME);
		if (!StringUtils.isBlank(expLong)){
			this._expiration.setTime(Long.parseLong(expLong));
		}
    }
    
    public String ToJavascriptSource()
    {
    	return GetJSONObject().toString();
    }   
    
    private String getJsonPtyValueString(JSONObject jObj, String name)
    {
    	if (jObj.has(name))
			try {
				return jObj.getString(name);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	return "";    		
    }      
}
