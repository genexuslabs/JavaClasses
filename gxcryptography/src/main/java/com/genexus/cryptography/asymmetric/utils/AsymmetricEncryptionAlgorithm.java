package com.genexus.cryptography.asymmetric.utils;

import com.genexus.securityapicommons.commons.Error;

/**
 * @author sgrampone
 *
 */
public enum AsymmetricEncryptionAlgorithm {

	RSA,;

	/**
	 * Mapping between String name and AsymmetricEncryptionAlgorithm enum
	 * representation
	 *
	 * @param asymmetricEncryptionAlgorithm
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return AsymmetricEncryptionAlgorithm enum representation
	 */
	public static AsymmetricEncryptionAlgorithm getAsymmetricEncryptionAlgorithm(String asymmetricEncryptionAlgorithm,
																				 Error error) {
		switch (asymmetricEncryptionAlgorithm.toUpperCase().trim()) {
			case "RSA":
				return AsymmetricEncryptionAlgorithm.RSA;
			default:
				error.setError("AE001", "Unrecognized AsymmetricEncryptionAlgorithm");
				return null;
		}
	}

	/**
	 * @param asymmetricEncryptionAlgorithm
	 *            AsymmetricEncryptionAlgorithm enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return String asymmetricEncryptionAlgorithm name
	 */
	public static String valueOf(AsymmetricEncryptionAlgorithm asymmetricEncryptionAlgorithm, Error error) {
		switch (asymmetricEncryptionAlgorithm) {
			case RSA:
				return "RSA";
			default:
				error.setError("AE002", "Unrecognized AsymmetricEncryptionAlgorithm");
				return "";
		}
	}

}
