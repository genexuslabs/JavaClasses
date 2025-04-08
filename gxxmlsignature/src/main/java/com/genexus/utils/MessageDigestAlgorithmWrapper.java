package com.genexus.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;

@SuppressWarnings({"LoggingSimilarMessage", "unused"})
public enum MessageDigestAlgorithmWrapper {

	SHA1, SHA256, SHA512,
	;

	private static final Logger logger = LogManager.getLogger(MessageDigestAlgorithmWrapper.class);

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
				logger.error("Not recognized digest algorithm");
				return null;
		}
	}

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
				logger.error("Not recognized digest algorithm");
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
				logger.error("Not recognized digest algorithm");
				return null;
		}
	}

}
