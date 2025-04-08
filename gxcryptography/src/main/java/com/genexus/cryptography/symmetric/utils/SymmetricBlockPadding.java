package com.genexus.cryptography.symmetric.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum SymmetricBlockPadding {

	NOPADDING, PKCS7PADDING, ISO10126D2PADDING, X923PADDING, ISO7816D4PADDING, ZEROBYTEPADDING, WITHCTS,
	;

	private static final Logger logger = LogManager.getLogger(SymmetricBlockPadding.class);

	public static SymmetricBlockPadding getSymmetricBlockPadding(String symmetricBlockPadding, Error error) {
		if (error == null) return SymmetricBlockPadding.NOPADDING;
		if (symmetricBlockPadding == null) {
			error.setError("SBP03", "Unrecognized SymmetricBlockPadding");
			logger.error("Unrecognized SymmetricBlockPadding");
			return SymmetricBlockPadding.NOPADDING;
		}

		switch (symmetricBlockPadding.toUpperCase().trim()) {
			case "NOPADDING":
				return SymmetricBlockPadding.NOPADDING;
			case "PKCS7PADDING":
				return SymmetricBlockPadding.PKCS7PADDING;
			case "ISO10126D2PADDING":
				return SymmetricBlockPadding.ISO10126D2PADDING;
			case "X923PADDING":
				return SymmetricBlockPadding.X923PADDING;
			case "ISO7816D4PADDING":
				return SymmetricBlockPadding.ISO7816D4PADDING;
			case "ZEROBYTEPADDING":
				return SymmetricBlockPadding.ZEROBYTEPADDING;
			case "WITHCTS":
				return SymmetricBlockPadding.WITHCTS;
			default:
				error.setError("SBP01", "Unrecognized SymmetricBlockPadding");
				logger.error("Unrecognized SymmetricBlockPadding");
				return null;
		}
	}

	public static String valueOf(SymmetricBlockPadding symmetricBlockPadding, Error error) {
		if (error == null) return "Unrecognized block padding";

		switch (symmetricBlockPadding) {
			case NOPADDING:
				return "NOPADDING";
			case PKCS7PADDING:
				return "PKCS7PADDING";
			case ISO10126D2PADDING:
				return "ISO10126D2PADDING";
			case X923PADDING:
				return "X923PADDING";
			case ISO7816D4PADDING:
				return "ISO7816D4PADDING";
			case ZEROBYTEPADDING:
				return "ZEROBYTEPADDING";
			case WITHCTS:
				return "WITHCTS";
			default:
				error.setError("SBP02", "Unrecognized SymmetricBlockPadding");
				logger.error("Unrecognized SymmetricBlockPadding");
				return "Unrecognized block padding";
		}
	}
}
