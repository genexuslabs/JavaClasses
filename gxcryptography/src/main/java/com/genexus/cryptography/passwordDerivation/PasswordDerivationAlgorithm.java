package com.genexus.cryptography.passwordDerivation;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings({"LoggingSimilarMessage", "unused"})

public enum PasswordDerivationAlgorithm {
	SCrypt, Bcrypt, Argon2, NONE;

	private static final Logger logger = LogManager.getLogger(PasswordDerivationAlgorithm.class);

	public static PasswordDerivationAlgorithm getPasswordDerivationAlgorithm(String passwordDerivationAlgorithm,
																			 Error error) {
		if (error == null) return PasswordDerivationAlgorithm.NONE;
		if (passwordDerivationAlgorithm == null) {
			error.setError("PDA03", "Unrecognized PasswordDerivationAlgorithm");
			logger.error("Unrecognized PasswordDerivationAlgorithm");
			return PasswordDerivationAlgorithm.NONE;
		}
		switch (passwordDerivationAlgorithm.trim()) {
			case "SCrypt":
				return PasswordDerivationAlgorithm.SCrypt;
			case "Bcrypt":
				return PasswordDerivationAlgorithm.Bcrypt;
			case "Argon2":
				return PasswordDerivationAlgorithm.Argon2;
			default:
				error.setError("PDA01", "Unrecognized PasswordDerivationAlgorithm");
				logger.error("Unrecognized PasswordDerivationAlgorithm");
				return null;
		}
	}

	public static String valueOf(PasswordDerivationAlgorithm passwordDerivationAlgorithm, Error error) {
		if (error == null) return "Unrecognized algorithm";
		switch (passwordDerivationAlgorithm) {
			case SCrypt:
				return "SCrypt";
			case Bcrypt:
				return "Bcrypt";
			case Argon2:
				return "Argon2";
			default:
				error.setError("PDA02", "Unrecognized PasswordDerivationAlgorithm");
				logger.error("Unrecognized PasswordDerivationAlgorithm");
				return "Unrecognized algorithm";
		}
	}

}
