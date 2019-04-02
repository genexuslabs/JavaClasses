package com.genexus.ws.security;

import com.genexus.common.interfaces.IGXWSSignature;
import com.genexus.common.interfaces.IGXWSSecurityKeyStore;

public class GXWSSignature implements IGXWSSignature
{
	private IGXWSSecurityKeyStore keystore;
	private String alias;
	private int keyIdentifierType;

	public GXWSSignature()
	{
		alias = "";
		keystore = new GXWSSecurityKeyStore();
	}

	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias.trim();
	}
	
	public IGXWSSecurityKeyStore getKeystore()
	{
		return keystore;
	}

	public void setKeystore(IGXWSSecurityKeyStore keystore)
	{
		this.keystore = keystore;
	}	
	
	public int getKeyIdentifierType()
	{
		return keyIdentifierType;
	}

	public void setKeyIdentifierType(int keyIdentifierType)
	{
		this.keyIdentifierType = keyIdentifierType;
	}
}

