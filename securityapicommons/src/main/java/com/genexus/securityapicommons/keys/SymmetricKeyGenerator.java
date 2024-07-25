package com.genexus.securityapicommons.keys;

import java.security.SecureRandom;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

/**
 * @author sgrampone
 *
 */
public class SymmetricKeyGenerator extends SecurityAPIObject {
	/**
	 * SymmetricKeyGenerator class constructor
	 */
	public SymmetricKeyGenerator() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	/**
	 * @param symmetricKeyType String
	 * @param length           result key length
	 * @return String Hexa fixed length secure random generated key
	 */
	public String doGenerateKey(String symmetricKeyType, int length) {
		SymmetricKeyType sKeyType = SymmetricKeyType.getSymmetricKeyType(symmetricKeyType, this.error);
		if (sKeyType == SymmetricKeyType.GENERICRANDOM) {
			return genericKeyGenerator(length);
		}
		this.error.setError("SS003", "Unrecognized SymmetricKeyType");
		return "";
	}

	/**
	 * @param symmetricKeyType String
	 * @param length           result IV length
	 *
	 * @return String Hexa fixed length secure random generated IV
	 */
	public String doGenerateIV(String symmetricKeyType, int length) {

		return doGenerateKey(symmetricKeyType, length);
	}

	/**
	 * @param symmetricKeyType String
	 * @param length           result nonce length
	 * @return String Hexa fixed length secure random generated nonce
	 */
	public String doGenerateNonce(String symmetricKeyType, int length) {
		return doGenerateKey(symmetricKeyType, length);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	/**
	 * @param length int bits result key length on bits
	 * @return String Hexa fixed length secure random generated key
	 */
	private String genericKeyGenerator(int length) {

		SecureRandom random = null;
		try {
			random = new SecureRandom();
		} catch (Exception e) {
			this.error.setError("SK004", "Key generation error");
			e.printStackTrace();
		}
		byte[] values = new byte[length / 8];
		random.nextBytes(values);
		StringBuilder sb = new StringBuilder();
		for (byte b : values) {
			sb.append(String.format("%02x", b));
		}
		String result = sb.toString().replaceAll("\\s", "");
		if (result == null || result.length() == 0) {
			this.error.setError("SK005", "Error encoding hexa");
			return "";
		}
		this.error.cleanError();
		return result;
	}

}
