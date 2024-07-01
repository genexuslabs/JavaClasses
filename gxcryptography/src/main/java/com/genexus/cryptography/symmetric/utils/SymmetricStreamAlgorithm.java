package com.genexus.cryptography.symmetric.utils;

import com.genexus.securityapicommons.commons.Error;

/**
 * @author sgrampone
 *
 */
public enum SymmetricStreamAlgorithm {
	RC4, HC256, CHACHA20, SALSA20, XSALSA20, ISAAC, NONE;

	/**
	 * Mapping between String name and SymmetricStreamAlgorithm enum representation
	 *
	 * @param symmetricStreamAlgorithm
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return SymmetricStreamAlgorithm enum representation
	 */
	public static SymmetricStreamAlgorithm getSymmetricStreamAlgorithm(String symmetricStreamAlgorithm, Error error) {
		if (error == null) return SymmetricStreamAlgorithm.NONE;
		if( symmetricStreamAlgorithm == null)
		{
			error.setError("SSA05", "Unrecognized SymmetricStreamAlgorithm");
			return SymmetricStreamAlgorithm.NONE;
		}

		switch (symmetricStreamAlgorithm.toUpperCase().trim()) {
			case "RC4":
				return SymmetricStreamAlgorithm.RC4;
			case "HC256":
				return SymmetricStreamAlgorithm.HC256;
			case "CHACHA20":
				return SymmetricStreamAlgorithm.CHACHA20;
			case "SALSA20":
				return SymmetricStreamAlgorithm.SALSA20;
			case "XSALSA20":
				return SymmetricStreamAlgorithm.XSALSA20;
			case "ISAAC":
				return SymmetricStreamAlgorithm.ISAAC;
			default:
				error.setError("SSA01", "Unrecognized SymmetricStreamAlgorithm");
				return null;
		}
	}

	/**
	 * @param symmetrcStreamAlgorithm
	 *            SymmetrcStreamAlgorithm enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return String SymmetrcStreamAlgorithm name value
	 */
	public static String valueOf(SymmetricStreamAlgorithm symmetrcStreamAlgorithm, Error error) {
		if(error == null) return "Unrecognized algorithm";

		switch (symmetrcStreamAlgorithm) {
			case RC4:
				return "RC4";
			case HC256:
				return "HC256";
			case CHACHA20:
				return "CHACHA20";
			case SALSA20:
				return "SALSA20";
			case XSALSA20:
				return "XSALSA20";
			case ISAAC:
				return "ISAAC";
			default:
				error.setError("SSA02", "Unrecognized SymmetricStreamAlgorithm");
				return "Unrecognized algorithm";
		}
	}

	/**
	 * @param algorithm
	 *            SymmetrcStreamAlgorithm enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return array int with fixed length 3 with key, if array[0]=0 is range, else
	 *         fixed values
	 */
	protected static int[] getKeySize(SymmetricStreamAlgorithm algorithm, Error error) {
		if (error == null) return null;

		int[] keySize = new int[3];
		switch (algorithm) {
			case RC4:
				keySize[0] = 0;
				keySize[1] = 40;
				keySize[2] = 2048;
				break;
			case HC256:
			case XSALSA20:
				keySize[0] = 1;
				keySize[1] = 256;
				break;
			case CHACHA20:
			case SALSA20:
				keySize[0] = 1;
				keySize[1] = 128;
				keySize[2] = 256;
				break;
			case ISAAC:
				keySize[0] = 0;
				keySize[1] = 32;
				keySize[2] = 8192;
				break;
			default:
				error.setError("SSA03", "Unrecognized SymmetricStreamAlgorithm");
				break;
		}
		return keySize;
	}

	/**
	 * @param algorithm
	 *            SymmetricStreamAlgorithm enum
	 * @param error
	 *            Error type for error management
	 * @return true if the algorithm uses an IV or nonce, false if it do not
	 */
	public static boolean usesIV(SymmetricStreamAlgorithm algorithm, Error error) {
		switch (algorithm) {
			case RC4:
			case ISAAC:
				return false;
			case HC256:
			case SALSA20:
			case CHACHA20:
			case XSALSA20:
				return true;
			default:
				error.setError("SSA04", "Unrecognized SymmetricStreamAlgorithm");
				return true;
		}

	}
}
