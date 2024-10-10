package com.genexus.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.signature.XMLSignature;

@SuppressWarnings({"LoggingSimilarMessage", "unused"})
public enum XMLSignatureWrapper {

	RSA_SHA1, RSA_SHA256, RSA_SHA512, ECDSA_SHA1, ECDSA_SHA256,
	;

	private static final Logger logger = LogManager.getLogger(XMLSignatureWrapper.class);

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
				logger.error("Unrecognized algorithm");
				return null;
		}
	}

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
				logger.error("Unrecognized algorithm");
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
				logger.error("Unrecognized algorithm");
				return null;
		}
	}
}
