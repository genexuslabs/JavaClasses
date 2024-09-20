package com.genexus.cryptography.asymmetric.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum AsymmetricEncryptionAlgorithm {

	RSA,
	;

	private static final Logger logger = LogManager.getLogger(AsymmetricEncryptionAlgorithm.class);

	public static AsymmetricEncryptionAlgorithm getAsymmetricEncryptionAlgorithm(String asymmetricEncryptionAlgorithm,
																				 Error error) {
		switch (asymmetricEncryptionAlgorithm.toUpperCase().trim()) {
			case "RSA":
				return AsymmetricEncryptionAlgorithm.RSA;
			default:
				error.setError("AE001", "Unrecognized AsymmetricEncryptionAlgorithm");
				logger.error("Unrecognized AsymmetricEncryptionAlgorithm");
				return null;
		}
	}

	public static String valueOf(AsymmetricEncryptionAlgorithm asymmetricEncryptionAlgorithm, Error error) {
		switch (asymmetricEncryptionAlgorithm) {
			case RSA:
				return "RSA";
			default:
				error.setError("AE002", "Unrecognized AsymmetricEncryptionAlgorithm");
				logger.error("Unrecognized AsymmetricEncryptionAlgorithm");
				return "";
		}
	}

}
