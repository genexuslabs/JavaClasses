package com.genexus.security.web;
import org.apache.commons.lang.NotImplementedException;

import com.genexus.internet.IGxJSONAble;

import json.org.json.IJsonFormattable;

public abstract class SecureToken implements IGxJSONAble{	
	public void tojson()
	{
    	throw new NotImplementedException();
    }
    public void AddObjectProperty(String name, Object prop)
    {
    	throw new NotImplementedException();
    }
    
    public Object GetJSONObject()
    {
    	throw new NotImplementedException();
    }
    
    public Object GetJSONObject(boolean includeState)
    {
    	throw new NotImplementedException();
    }
    
    public void FromJSONObject(IJsonFormattable obj)
    {
    	throw new NotImplementedException();
    }
    
    public String ToJavascriptSource()
    {
    	throw new NotImplementedException();
    }     
}
