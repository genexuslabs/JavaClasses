package com.genexus.cryptography.passwordDerivation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.params.Argon2Parameters;

import com.genexus.securityapicommons.commons.Error;

@SuppressWarnings("LoggingSimilarMessage")
public enum Argon2Version {

	ARGON2_VERSION_10, ARGON2_VERSION_13;

	private static final Logger logger = LogManager.getLogger(Argon2Version.class);

	public static Argon2Version getArgon2Version(String argon2Version, Error error) {
		switch (argon2Version.toUpperCase().trim()) {
			case "ARGON2_VERSION_10":
				return Argon2Version.ARGON2_VERSION_10;
			case "ARGON2_VERSION_13":
				return Argon2Version.ARGON2_VERSION_13;
			default:
				error.setError("PDV01", "Unrecognized Argon2Version");
				logger.error("Unrecognized Argon2Version");
				return null;
		}
	}

	public static String valueOf(Argon2Version argon2Version, Error error) {
		switch (argon2Version) {
			case ARGON2_VERSION_10:
				return "ARGON2_VERSION_10";
			case ARGON2_VERSION_13:
				return "ARGON2_VERSION_13";
			default:
				error.setError("PDV02", "Unrecognized Argon2Version");
				logger.error("Unrecognized Argon2Version");
				return "";
		}
	}

	public static int getVersionParameter(Argon2Version argon2Version, Error error) {
		switch (argon2Version) {
			case ARGON2_VERSION_10:
				return Argon2Parameters.ARGON2_VERSION_10;
			case ARGON2_VERSION_13:
				return Argon2Parameters.ARGON2_VERSION_13;
			default:
				error.setError("PDV03", "Unrecognized Argon2Version");
				logger.error("Unrecognized Argon2Version");
				return 0;
		}
	}

}
