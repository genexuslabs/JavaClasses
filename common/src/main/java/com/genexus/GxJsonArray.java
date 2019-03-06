package com.genexus;

public class  GxJsonArray
{
	public GxJsonArray(String jsonSTR)
	{
		this.jsonSTR = jsonSTR;
	}
	
	private String jsonSTR;
	
	public String toJson()
	{
		return jsonSTR;
	}
	
	public void fromJson(String jsonSTR)
	{
		this.jsonSTR = jsonSTR;
	}
}