package com.genexus.securityapicommons.keys;

import com.genexus.securityapicommons.commons.Error;

/**
 * @author sgrampone
 *
 */
public enum SymmetricKeyType {

	GENERICRANDOM,;

	/**
	 * Mapping between String name and SymmetricKeyType enum representation
	 *
	 * @param symmetricKeyType
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return SymmetricKeyType enum representation
	 */
	public static SymmetricKeyType getSymmetricKeyType(String symmetricKeyType, Error error) {
		switch (symmetricKeyType.toUpperCase().trim()) {
			case "GENERICRANDOM":
				return SymmetricKeyType.GENERICRANDOM;
			default:
				error.setError("SK001", "Unrecognized key type");
				return null;
		}
	}

	/**
	 * @param symmetricKeyType
	 *            SymmetricKeyType enum, key type name
	 * @param error
	 *            Error type for error management
	 * @return String value of key type in string
	 */
	public static String valueOf(SymmetricKeyType symmetricKeyType, Error error) {
		switch (symmetricKeyType) {
			case GENERICRANDOM:
				return "GENERICRANDOM";
			default:
				error.setError("SK002", "Unrecognized key type");
				return "";
		}
	}

}