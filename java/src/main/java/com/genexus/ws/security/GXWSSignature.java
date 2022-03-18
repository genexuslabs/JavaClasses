package com.genexus.ws.security;

import com.genexus.common.interfaces.IGXWSSignature;
import com.genexus.common.interfaces.IGXWSSecurityKeyStore;

public class GXWSSignature implements IGXWSSignature
{
	private IGXWSSecurityKeyStore keystore;
	private String alias;
	private int keyIdentifierType;
	private String canonicalizationAlgorithm;
	private String digest;
	private String signatureAlgorithm;

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

	public String getCanonicalizationalgorithm() {return canonicalizationAlgorithm;}

	public void setCanonicalizationalgorithm(String algorithm) {this.canonicalizationAlgorithm = algorithm;}

	public String getDigest() {return digest;}

	public void setDigest(String digest) {this.digest = digest;}

	public String getSignaturealgorithm() { return signatureAlgorithm;}

	public void setSignaturealgorithm(String algorithm) {this.signatureAlgorithm = algorithm;}
}

