package com.genexus.ws.security;

import com.genexus.common.interfaces.IGXWSEncryption;
import com.genexus.common.interfaces.IGXWSSecurityKeyStore;

public class GXWSEncryption implements IGXWSEncryption
{
	private IGXWSSecurityKeyStore keystore;
	private String alias;
	private int keyIdentifierType;

	public GXWSEncryption()
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

