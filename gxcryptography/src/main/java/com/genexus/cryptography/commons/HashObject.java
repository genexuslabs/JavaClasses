package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

/**
 * @author sgrampone
 *
 */
public abstract class HashObject extends SecurityAPIObject {

	/**
	 * HashObject constructor
	 */
	public HashObject() {
		super();
	}

	/**
	 * @param hashAlgorithm
	 *            String HashAlgorithm enum, algorithm name
	 * @param txtToHash
	 *            plain text to hcalculate hash
	 * @return String Hexa representation of the txtToHash with the algorithm
	 *         indicated
	 */
	public abstract String doHash(String hashAlgorithm, String txtToHash);
}
