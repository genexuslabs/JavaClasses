package com.genexus.security;

import java.util.HashMap;

public class OutData extends HashMap<String, Object>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String jsonString;

	public String getjsonString()
	{
		return jsonString;
	}

	public void setjsonString(String jsonString)
	{
		this.jsonString = jsonString;
	}

}
