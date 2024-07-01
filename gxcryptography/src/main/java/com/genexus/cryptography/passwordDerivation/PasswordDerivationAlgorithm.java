package com.genexus.cryptography.passwordDerivation;

import com.genexus.securityapicommons.commons.Error;

/**
 * @author sgrampone
 *
 */
public enum PasswordDerivationAlgorithm {
	SCrypt, Bcrypt,Argon2, NONE;

	/**
	 * Mapping between String name and PasswordDerivationAlgorithm enum
	 * representation
	 *
	 * @param passwordDerivationAlgorithm
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return PasswordDerivationAlgorithm enum representation
	 */
	public static PasswordDerivationAlgorithm getPasswordDerivationAlgorithm(String passwordDerivationAlgorithm,
																			 Error error) {
		if (error == null) return PasswordDerivationAlgorithm.NONE;
		if(passwordDerivationAlgorithm == null)
		{
			error.setError("PDA03", "Unrecognized PasswordDerivationAlgorithm");
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
				return null;
		}
	}

	/**
	 * @param passwordDerivationAlgorithm
	 *            PasswordDerivationAlgorithm enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return PasswordDerivationAlgorithm value in String
	 */
	public static String valueOf(PasswordDerivationAlgorithm passwordDerivationAlgorithm, Error error) {
		if(error == null) return "Unrecognized algorithm";
		switch (passwordDerivationAlgorithm) {
			case SCrypt:
				return "SCrypt";
			case Bcrypt:
				return "Bcrypt";
			case Argon2:
				return "Argon2";
			default:
				error.setError("PDA02", "Unrecognized PasswordDerivationAlgorithm");
				return "Unrecognized algorithm";
		}
	}

}
