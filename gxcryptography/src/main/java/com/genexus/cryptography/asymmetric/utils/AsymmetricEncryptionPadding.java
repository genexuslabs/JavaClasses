package com.genexus.cryptography.asymmetric.utils;

import com.genexus.securityapicommons.commons.Error;

/**
 * @author sgrampone
 *
 */
public enum AsymmetricEncryptionPadding {
	NOPADDING, OAEPPADDING, PCKS1PADDING, ISO97961PADDING,;

	/**
	 * Mapping between String name and AsymmetricEncryptionPadding enum
	 * representation
	 *
	 * @param asymmetricEncryptionPadding
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return AsymmetricEncryptionPadding enum representation
	 */
	public static AsymmetricEncryptionPadding getAsymmetricEncryptionPadding(String asymmetricEncryptionPadding,
																			 Error error) {
		switch (asymmetricEncryptionPadding.toUpperCase().trim()) {
			case "NOPADDING":
				return AsymmetricEncryptionPadding.NOPADDING;
			case "OAEPPADDING":
				return AsymmetricEncryptionPadding.OAEPPADDING;
			case "PCKS1PADDING":
				return AsymmetricEncryptionPadding.PCKS1PADDING;
			case "ISO97961PADDING":
				return AsymmetricEncryptionPadding.ISO97961PADDING;
			default:
				error.setError("AE003", "Unrecognized AsymmetricEncryptionPadding");
				return null;
		}
	}

	/**
	 * @param asymmetricEncryptionPadding
	 *            AsymmetricEncryptionPadding enum, padding name
	 * @param error
	 *            Error type for error management
	 * @return String name of asymmetricEncryptionPadding
	 */
	public static String valueOf(AsymmetricEncryptionPadding asymmetricEncryptionPadding, Error error) {
		switch (asymmetricEncryptionPadding) {
			case NOPADDING:
				return "NOPADDING";
			case OAEPPADDING:
				return "OAEPPADDING";
			case PCKS1PADDING:
				return "PCKS1PADDING";
			case ISO97961PADDING:
				return "ISO97961PADDING";
			default:
				error.setError("AE004", "Unrecognized AsymmetricEncryptionPadding");
				return "";
		}
	}

}
