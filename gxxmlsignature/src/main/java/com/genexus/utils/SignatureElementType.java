package com.genexus.utils;

import com.genexus.securityapicommons.commons.Error;

public enum SignatureElementType {
	id, path, document,;

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
				return "";

		}
	}
}
