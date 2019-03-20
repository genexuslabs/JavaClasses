package com.genexus.ws.security;

import com.genexus.common.interfaces.IGXWSSecurityKeyStore;

public class GXWSSecurityKeyStore implements IGXWSSecurityKeyStore
{
	private String type;
	private String password;
	private String source;

	public GXWSSecurityKeyStore()
	{
		type = "";
		password = "";
		source = "";
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type.trim();
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password.trim();
	}
	
	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source.trim();
	}	
}

