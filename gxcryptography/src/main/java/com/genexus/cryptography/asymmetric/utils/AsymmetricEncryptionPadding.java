package com.genexus.cryptography.asymmetric.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum AsymmetricEncryptionPadding {
	NOPADDING, OAEPPADDING, PCKS1PADDING, ISO97961PADDING,
	;

	private static final Logger logger = LogManager.getLogger(AsymmetricEncryptionPadding.class);

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
				logger.error("Unrecognized AsymmetricEncryptionPadding");
				return null;
		}
	}

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
				logger.error("Unrecognized AsymmetricEncryptionPadding");
				return "";
		}
	}

}
