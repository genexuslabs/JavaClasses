package com.genexus.utils;

import org.apache.xml.security.algorithms.MessageDigestAlgorithm;

import com.genexus.securityapicommons.commons.Error;

public enum MessageDigestAlgorithmWrapper {

	SHA1, SHA256, SHA512,;

	/**
	 * Mapping between String name and MessageDigestAlgorithmWrapper enum
	 * representation
	 *
	 * @param messageDigestAlgorithmWrapper
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return MessageDigestAlgorithmWrapper enum representation
	 */
	public static MessageDigestAlgorithmWrapper getMessageDigestAlgorithmWrapper(String messageDigestAlgorithmWrapper,
																				 Error error) {
		switch (messageDigestAlgorithmWrapper.toUpperCase().trim()) {
			case "SHA1":
				return MessageDigestAlgorithmWrapper.SHA1;
			case "SHA256":
				return MessageDigestAlgorithmWrapper.SHA256;
			case "SHA512":
				return MessageDigestAlgorithmWrapper.SHA512;
			default:
				error.setError("MDA01", "Not recognized digest algorithm");
				return null;
		}
	}

	/**
	 * @param messageDigestAlgorithmWrapper
	 *            MessageDigestAlgorithmWrapper enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return String messageDigestAlgorithmWrapper name
	 */
	public static String valueOf(MessageDigestAlgorithmWrapper messageDigestAlgorithmWrapper, Error error) {
		switch (messageDigestAlgorithmWrapper) {
			case SHA1:
				return "SHA1";
			case SHA256:
				return "SHA256";
			case SHA512:
				return "SHA512";
			default:
				error.setError("MDA02", "Not recognized digest algorithm");
				return null;
		}
	}

	public static String getDigestMethod(MessageDigestAlgorithmWrapper messageDigestAlgorithmWrapper, Error error) {
		switch (messageDigestAlgorithmWrapper) {
			case SHA1:
				return MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1;
			case SHA256:
				return MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256;
			case SHA512:
				return MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512;
			default:
				error.setError("MDA03", "Not recognized digest algorithm");
				return null;
		}
	}

}
