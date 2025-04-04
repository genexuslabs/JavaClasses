package com.genexus.securityapicommons.commons;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class Key extends SecurityAPIObject {

	public Key() {
		super();
	}

	protected String algorithm;

	protected static  final String[] pkcs8_extensions = new String[] {"pkcs8", "key", "pem"};
	protected static final String[] pkcs12_extensions = new String[] {"pfx", "p12", "jks", "pkcs12"};
	protected static final String[] der_extentions = new String[] {"cer", "crt"};

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
