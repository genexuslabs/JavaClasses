package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

/**
 * @author sgrampone
 *
 */
public abstract class SymmetricBlockCipherObject extends SecurityAPIObject {

	/**
	 * SymmetricBlockCipherObject constructor
	 */
	public SymmetricBlockCipherObject() {
		super();
	}

	/**
	 * @param symmetricBlockAlgorithm
	 *            String SymmetricBlockAlgorithm enum, symmetric block algorithm
	 *            name
	 * @param symmetricBlockMode
	 *            String SymmetricBlockModes enum, symmetric block mode name
	 * @param symmetricBlockPadding
	 *            String SymmetricBlockPadding enum, symmetric block padding name
	 * @param key
	 *            String Hexa key for the algorithm excecution
	 * @param IV
	 *            String IV for the algorithm execution, must be the same length as
	 *            the blockSize
	 * @param plainText
	 *            String UTF-8 plain text to encrypt
	 * @return String Base64 encrypted text with the given algorithm and parameters
	 */
	public abstract String doEncrypt(String symmetricBlockAlgorithm, String symmetricBlockMode,
									 String symmetricBlockPadding, String key, String IV, String plainText);

	/**
	 * @param symmetricBlockAlgorithm
	 *            String SymmetricBlockAlgorithm enum, symmetric block algorithm
	 *            name
	 * @param symmetricBlockMode
	 *            String SymmetricBlockModes enum, symmetric block mode name
	 * @param symmetricBlockPadding
	 *            String SymmetricBlockPadding enum, symmetric block padding name
	 * @param key
	 *            String Hexa key for the algorithm excecution
	 * @param IV
	 *            String IV for the algorithm execution, must be the same length as
	 *            the blockSize
	 * @param encryptedInput
	 *            String Base64 text to decrypt
	 * @return String plain text UTF-8 with the given algorithm and parameters
	 */
	public abstract String doDecrypt(String symmetricBlockAlgorithm, String symmetricBlockMode,
									 String symmetricBlockPadding, String key, String IV, String encryptedInput);
}
