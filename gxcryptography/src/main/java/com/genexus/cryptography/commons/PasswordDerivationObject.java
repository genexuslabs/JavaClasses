package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

/**
 * @author sgrampone
 *
 */
public abstract class PasswordDerivationObject extends SecurityAPIObject {

	/**
	 * PasswordDerivationObject constructor
	 */
	public PasswordDerivationObject() {
		super();
	}

	/**
	 * @param password
	 *            String UTF-8 to hash
	 * @param salt
	 *            String UTF-8 to use as salt
	 * @param CPUCost
	 *            CPUCost must be larger than 1, a power of 2 and less than 2^(128 *
	 *            blockSize / 8)
	 * @param blockSize
	 *            The blockSize must be >= 1
	 * @param parallelization
	 *            Parallelization must be a positive integer less than or equal to
	 *            Integer.MAX_VALUE / (128 * blockSize * 8)
	 * @param keyLenght
	 *            fixed key length
	 */
	public abstract String doGenerateSCrypt(String password, String salt, int CPUCost, int blockSize,
											int parallelization, int keyLenght);

	/**
	 *
	 * Calculates SCrypt digest with arbitrary fixed parameters: CPUCost (N) = 16384
	 * blockSize (r) = 8 parallelization (p) = 1 keyLenght = 256
	 *
	 * @param password
	 *            String UTF-8 to hash
	 * @param salt
	 *            String UTF-8 to use as salt
	 */
	public abstract String doGenerateDefaultSCrypt(String password, String salt);

	/**
	 * @param password
	 *            String UTF-8 to hash. the password bytes (up to 72 bytes) to use
	 *            for this invocation.
	 * @param salt
	 *            String UTF-8 to salt. The salt lenght must be 128 bits
	 * @param cost
	 *            The cost of the bcrypt function grows as 2^cost. Legal values are
	 *            4..31 inclusive.
	 * @return String Base64 hashed password to store
	 */
	public abstract String doGenerateBcrypt(String password, String salt, int cost);

	/**
	 * Calculates Bcrypt digest with arbitrary fixed cost parameter: cost = 6
	 *
	 * @param password
	 *            String UTF-8 to hash. the password bytes (up to 72 bytes) to use
	 *            for this invocation.
	 * @param salt
	 *            String UTF-8 to salt. The salt lenght must be 128 bits
	 * @return String Base64 hashed password to store
	 */
	public abstract String doGenerateDefaultBcrypt(String password, String salt);
}
