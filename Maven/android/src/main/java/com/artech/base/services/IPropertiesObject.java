package com.artech.base.services;

public interface IPropertiesObject
{
	public Object getProperty(String name);
	public boolean setProperty(String name, Object value);
	public String optStringProperty(String name);
}
