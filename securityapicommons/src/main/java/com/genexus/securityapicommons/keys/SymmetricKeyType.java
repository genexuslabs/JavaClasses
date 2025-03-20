package com.genexus.securityapicommons.keys;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum SymmetricKeyType {

	GENERICRANDOM,
	;

	private static final Logger logger = LogManager.getLogger(SymmetricKeyType.class);

	public static SymmetricKeyType getSymmetricKeyType(String symmetricKeyType, Error error) {
		logger.debug("getSymmetricKeyType");
		switch (symmetricKeyType.toUpperCase().trim()) {
			case "GENERICRANDOM":
				return SymmetricKeyType.GENERICRANDOM;
			default:
				error.setError("SK001", "Unrecognized key type");
				logger.error("Unrecognized key type");
				return null;
		}
	}

	public static String valueOf(SymmetricKeyType symmetricKeyType, Error error) {
		logger.debug("valueOf");
		switch (symmetricKeyType) {
			case GENERICRANDOM:
				return "GENERICRANDOM";
			default:
				error.setError("SK002", "Unrecognized key type");
				logger.error("Unrecognized key type");
				return "";
		}
	}

}