package com.genexus.utils;

import org.apache.xml.security.signature.XMLSignature;

import com.genexus.securityapicommons.commons.Error;

public enum XMLSignatureWrapper {

	RSA_SHA1, RSA_SHA256, RSA_SHA512, ECDSA_SHA1, ECDSA_SHA256,;

	/**
	 * Mapping between String name and XMLSignatureWrapper enum representation
	 *
	 * @param xMLSignatureWrapper
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return XMLSignatureWrapper enum representation
	 */
	public static XMLSignatureWrapper getXMLSignatureWrapper(String xMLSignatureWrapper, Error error) {
		switch (xMLSignatureWrapper.toUpperCase().trim()) {
			case "RSA_SHA1":
				return RSA_SHA1;
			case "RSA_SHA256":
				return RSA_SHA256;
			case "RSA_SHA512":
				return RSA_SHA512;
			case "ECDSA_SHA1":
				return ECDSA_SHA1;
			case "ECDSA_SHA256":
				return ECDSA_SHA256;
			default:
				error.setError("XSW01", "Unrecognized algorithm");
				return null;
		}
	}

	/**
	 * @param xMLSignatureWrapper
	 *            XMLSignatureWrapper enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return String xMLSignatureWrapper name
	 */
	public static String valueOf(XMLSignatureWrapper xMLSignatureWrapper, Error error) {
		switch (xMLSignatureWrapper) {
			case RSA_SHA1:
				return "RSA_SHA1";
			case RSA_SHA256:
				return "RSA_SHA256";
			case RSA_SHA512:
				return "RSA_SHA512";
			case ECDSA_SHA1:
				return "ECDSA_SHA1";
			case ECDSA_SHA256:
				return "ECDSA_SHA256";
			default:
				error.setError("XSW02", "Unrecognized algorithm");
				return null;
		}
	}

	public static String getSignatureMethodAlgorithm(XMLSignatureWrapper xMLSignatureWrapper, Error error) {
		switch (xMLSignatureWrapper) {
			case RSA_SHA1:
				return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;
			case RSA_SHA256:
				return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
			case RSA_SHA512:
				return XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512;
			case ECDSA_SHA1:
				return XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA1;
			case ECDSA_SHA256:
				return XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA256;
			default:
				error.setError("XSW03", "Unrecognized algorithm");
				return null;
		}
	}
}
