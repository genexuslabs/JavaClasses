package com.genexus.cryptography.commons;


import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class CmacObject extends SecurityAPIObject {

	public CmacObject() {
		super();
	}

	public abstract String calculate(String plainText, String key, String algorithm, int macSize);

	public abstract boolean verify(String plainText, String key, String mac, String algorithm, int macSize);
}
