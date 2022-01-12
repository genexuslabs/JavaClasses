package com.genexus.util;

public class GXProperty
{
    public String name;
    public String value;
	
	public String getKey()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}
	
	public void setKey(String name)
	{
		this.name = name;
	}

	public void setValue(String value)
	{
		this.value = value;
	}	
	
}
