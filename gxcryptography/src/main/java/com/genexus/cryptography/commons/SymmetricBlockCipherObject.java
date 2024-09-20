package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class SymmetricBlockCipherObject extends SecurityAPIObject {

	public SymmetricBlockCipherObject() {
		super();
	}

	public abstract String doEncrypt(String symmetricBlockAlgorithm, String symmetricBlockMode,
									 String symmetricBlockPadding, String key, String IV, String plainText);

	public abstract String doDecrypt(String symmetricBlockAlgorithm, String symmetricBlockMode,
									 String symmetricBlockPadding, String key, String IV, String encryptedInput);
}
