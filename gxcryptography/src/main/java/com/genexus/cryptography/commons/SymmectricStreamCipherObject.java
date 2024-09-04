package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

/**
 * @author sgrampone
 *
 */
public abstract class SymmectricStreamCipherObject extends SecurityAPIObject {

	/**
	 * SymmectricStreamCipherObject constructor
	 */
	public SymmectricStreamCipherObject() {
		super();
	}

	/**
	 * @param symmetricStreamAlgorithm
	 *            String SymmetrcStreamAlgorithm enum, algorithm name
	 * @param symmetricBlockMode
	 *            String SymmetricBlockMode enum, mode name
	 * @param key
	 *            String Hexa key for the algorithm excecution
	 * @param IV
	 *            String Hexa IV (nonce) for those algorithms that uses, ignored if
	 *            not
	 * @param plainText
	 *            String UTF-8 plain text to encrypt
	 * @return String Base64 encrypted text with the given algorithm and parameters
	 */
	public abstract String doEncrypt(String symmetricStreamAlgorithm, String key, String IV, String plainText);

	/**
	 * @param symmetricStreamAlgorithm
	 *            String SymmetrcStreamAlgorithm enum, algorithm name
	 * @param symmetricBlockMode
	 *            String SymmetricBlockMode enum, mode name
	 * @param key
	 *            String Hexa key for the algorithm excecution
	 * @param IV
	 *            String Hexa IV (nonce) for those algorithms that uses, ignored if
	 *            not
	 * @param encryptedInput
	 *            String Base64 encrypted text with the given algorithm and
	 *            parameters
	 * @return String plain text UTF-8 with the given algorithm and parameters
	 */
	public abstract String doDecrypt(String symmetricStreamAlgorithm, String key, String IV, String encryptedInput);
}
