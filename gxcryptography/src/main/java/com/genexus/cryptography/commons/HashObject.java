package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class HashObject extends SecurityAPIObject {

	public HashObject() {
		super();
	}

	public abstract String doHash(String hashAlgorithm, String txtToHash);
}
