package com.genexus.db.service;

public class VarValue
{
	public String name;
	public Object value;
	public GXType type;

	public VarValue(String name, GXType type, Object value)
	{
		this.name = name;
		this.type = type;
		this.value = value;
	}
}
