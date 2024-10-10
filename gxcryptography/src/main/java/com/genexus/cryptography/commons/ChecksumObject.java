package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class ChecksumObject extends SecurityAPIObject {

	public ChecksumObject() {
		super();
	}

	public abstract String generateChecksum(String input, String inputType, String checksumAlgorithm);

	public abstract boolean verifyChecksum(String input, String inputType, String checksumAlgorithm, String digest);
}
