package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

/**
 * @author sgrampone
 */
public abstract class SymmectricStreamCipherObject extends SecurityAPIObject {

	public SymmectricStreamCipherObject() {
		super();
	}

	public abstract String doEncrypt(String symmetricStreamAlgorithm, String key, String IV, String plainText);

	public abstract String doDecrypt(String symmetricStreamAlgorithm, String key, String IV, String encryptedInput);
}
