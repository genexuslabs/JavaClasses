package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class HmacObject extends SecurityAPIObject {

	public HmacObject() {
		super();
	}

	public abstract String calculate(String plainText, String password, String algorithm);

	public abstract boolean verify(String plainText, String password, String mac, String algorithm);

}
