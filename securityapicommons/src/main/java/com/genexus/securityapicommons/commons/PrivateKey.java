package com.genexus.securityapicommons.commons;

public abstract class PrivateKey extends Key{

	public PrivateKey() {
		super();
	}

	public abstract boolean loadEncrypted(String privateKeyPath, String encryptionPassword);
}
