package com.genexus.securityapicommons.commons;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class Key extends SecurityAPIObject {

	public Key() {
		super();
	}

	protected String algorithm;

	public String getAlgorithm()
	{
		return this.algorithm;
	}

	public boolean load(String path) {return false; }
	public boolean loadPKCS12(String path, String alias, String password) { return false; }
	public boolean fromBase64(String base64) { return false; }
	public String toBase64() { return ""; }
	protected void setAlgorithm() {}
	public AsymmetricKeyParameter getAsymmetricKeyParameter() {return null; }
}
