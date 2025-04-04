package com.genexus.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public enum SignatureElementType {
	id, path, document,
	;

	private static final Logger logger = LogManager.getLogger(SignatureElementType.class);

	public static String valueOf(SignatureElementType signatureElementType, Error error) {
		switch (signatureElementType) {
			case id:
				return "id";
			case path:
				return "path";
			case document:
				return "document";
			default:
				error.setError("SET01", "Unrecognized SignatureElementType");
				logger.error("Unrecognized SignatureElementType");
				return "";

		}
	}
}
