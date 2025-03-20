package com.genexus.cryptography.asymmetric.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum SignatureStandard {
	CMS,
	;

	private static final Logger logger = LogManager.getLogger(SignatureStandard.class);

	public static SignatureStandard getSignatureStandard(String signatureStandard,
														 Error error) {
		switch (signatureStandard.toUpperCase().trim()) {
			case "CMS":
				return SignatureStandard.CMS;
			default:
				error.setError("SS001", "Unrecognized SignatureStandard");
				logger.error("Unrecognized SignatureStandard");
				return null;
		}
	}

	public static String valueOf(SignatureStandard signatureStandard, Error error) {
		switch (signatureStandard) {
			case CMS:
				return "CMS";
			default:
				error.setError("SS002", "Unrecognized SignatureStandard");
				logger.error("Unrecognized SignatureStandard");
				return "";
		}
	}
}
