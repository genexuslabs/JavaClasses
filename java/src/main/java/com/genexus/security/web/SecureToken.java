package com.genexus.security.web;

import com.genexus.internet.IGxJSONAble;

import json.org.json.IJsonFormattable;

public abstract class SecureToken implements IGxJSONAble{	
	public void tojson()
	{
    	throw new UnsupportedOperationException();
    }
    public void AddObjectProperty(String name, Object prop)
    {
    	throw new UnsupportedOperationException();
    }
    
    public Object GetJSONObject()
    {
    	throw new UnsupportedOperationException();
    }
    
    public Object GetJSONObject(boolean includeState)
    {
    	throw new UnsupportedOperationException();
    }
    
    public void FromJSONObject(IJsonFormattable obj)
    {
    	throw new UnsupportedOperationException();
    }
    
    public String ToJavascriptSource()
    {
    	throw new UnsupportedOperationException();
    }     
}
