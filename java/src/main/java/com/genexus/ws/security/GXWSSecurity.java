package com.genexus.ws.security;

import com.genexus.common.interfaces.IGXWSSecurity;
import com.genexus.common.interfaces.IGXWSSignature;
import com.genexus.common.interfaces.IGXWSEncryption;

public class GXWSSecurity implements IGXWSSecurity
{
	private IGXWSSignature signature;
	private IGXWSEncryption encryption;
	private int expirationTimeout;

	public GXWSSecurity()
	{
		signature = new GXWSSignature();
		encryption = new GXWSEncryption();
	}

	public IGXWSSignature getSignature()
	{
		return signature;
	}

	public void setSignature(IGXWSSignature signature)
	{
		this.signature = signature;
	}
	
	public IGXWSEncryption getEncryption()
	{
		return encryption;
	}

	public void setEncryption(IGXWSEncryption encryption)
	{
		this.encryption = encryption;
	}

	public int getExpirationTimeout()
	{
		return expirationTimeout;
	}

	public void setExpirationTimeout(int expirationTimeout)
	{
		this.expirationTimeout = expirationTimeout;
	}
}

