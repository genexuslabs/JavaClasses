package com.artech.base.services;

public interface IAndroidSession
{
	public void setValue(String key, String value);
	public String getValue(String key);
	public void clear();
	public void destroy();
	public void remove(String key);
	
	//new for store values as Objects
	public void setObject(String key, Object value);
	public Object getObject(String key);
	
	
}
