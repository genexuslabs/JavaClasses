package com.genexus.securityapicommons.commons;

public abstract class Certificate extends PublicKey {

	public Certificate() {
		super();
	}

	public abstract boolean load(String path);
	public abstract boolean loadPKCS12(String path, String alias, String password);
	public abstract boolean fromBase64(String base64Data);
	public abstract String toBase64();
}
