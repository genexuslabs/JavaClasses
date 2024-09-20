package com.genexus.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings({"unused", "LoggingSimilarMessage"})
public enum AsymmetricSigningAlgorithm {

	RSA, ECDSA,
	;

	private static final Logger logger = LogManager.getLogger(AsymmetricSigningAlgorithm.class);

	public static AsymmetricSigningAlgorithm getAsymmetricSigningAlgorithm(String asymmetricSigningAlgorithm,
																		   Error error) {
		switch (asymmetricSigningAlgorithm.toUpperCase().trim()) {
			case "RSA":
				return AsymmetricSigningAlgorithm.RSA;
			case "ECDSA":
				return AsymmetricSigningAlgorithm.ECDSA;
			default:
				error.setError("ASA01", "Unrecognized AsymmetricSigningAlgorithm");
				logger.error("Unrecognized AsymmetricSigningAlgorithm");
				return null;
		}
	}

	public static String valueOf(AsymmetricSigningAlgorithm asymmetricSigningAlgorithm, Error error) {
		switch (asymmetricSigningAlgorithm) {
			case RSA:
				return "RSA";
			case ECDSA:
				return "ECDSA";
			default:
				error.setError("ASA02", "Unrecognized AsymmetricSigningAlgorithm");
				logger.error("Unrecognized AsymmetricSigningAlgorithm");
				return "";
		}
	}
}
