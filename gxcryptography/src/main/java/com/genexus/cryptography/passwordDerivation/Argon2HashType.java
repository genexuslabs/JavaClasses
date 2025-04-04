package com.genexus.cryptography.passwordDerivation;

import com.genexus.cryptography.mac.Hmac;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.params.Argon2Parameters;

import com.genexus.securityapicommons.commons.Error;

@SuppressWarnings("LoggingSimilarMessage")
public enum Argon2HashType {
	ARGON2_d, ARGON2_i, ARGON2_id;

	private static final Logger logger = LogManager.getLogger(Argon2HashType.class);

	public static Argon2HashType getArgon2HashType(String argon2HashType, Error error) {
		switch (argon2HashType.trim()) {
			case "ARGON2_d":
				return Argon2HashType.ARGON2_d;
			case "ARGON2_i":
				return Argon2HashType.ARGON2_i;
			case "ARGON2_id":
				return Argon2HashType.ARGON2_id;
			default:
				error.setError("PDH01", "Unrecognized Argon2HashType");
				logger.error("Unrecognized Argon2HashType");
				return null;
		}

	}

	public static String valueOf(Argon2HashType argon2HashType, Error error) {
		switch (argon2HashType) {
			case ARGON2_d:
				return "ARGON2_d";
			case ARGON2_i:
				return "ARGON2_i";
			case ARGON2_id:
				return "ARGON2_id";
			default:
				error.setError("PDH02", "Unrecognized Argon2HashType");
				logger.error("Unrecognized Argon2HashType");
				return "";
		}
	}

	public static int getArgon2Parameter(Argon2HashType argon2HashType, Error error) {
		switch (argon2HashType) {
			case ARGON2_d:
				return Argon2Parameters.ARGON2_d;
			case ARGON2_i:
				return Argon2Parameters.ARGON2_i;
			case ARGON2_id:
				return Argon2Parameters.ARGON2_id;
			default:
				error.setError("PDH03", "Unrecognized Argon2HashType");
				logger.error("Unrecognized Argon2HashType");
				return 0;
		}
	}
}
