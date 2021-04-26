package com.genexus.util;

public class GXService
{
	private String name;
	private String type;
	private String className;
	private boolean allowMultiple;
	private GXProperties properties;
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}	
	
	public String getType()
	{
		return type;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getClassName()
	{
		return className;
	}
	
	public void setClassName(String className)
	{
		this.className = className;
	}

	public boolean getAllowMultiple()
	{
		return allowMultiple;
	}
	
	public void setAllowMultiple(boolean allowMultiple)
	{
		this.allowMultiple = allowMultiple;
	}
	
	public GXProperties getProperties()
	{
		return properties;
	}
	
	public void setProperties(GXProperties properties)
	{
		this.properties = properties;
	}
}
