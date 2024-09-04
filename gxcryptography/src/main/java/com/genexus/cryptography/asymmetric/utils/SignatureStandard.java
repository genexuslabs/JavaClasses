package com.genexus.cryptography.asymmetric.utils;

import com.genexus.securityapicommons.commons.Error;
public enum SignatureStandard {
	CMS,;
	public static SignatureStandard getSignatureStandard(String signatureStandard,
														 Error error) {
		switch (signatureStandard.toUpperCase().trim()) {
			case "CMS":
				return SignatureStandard.CMS;
			default:
				error.setError("SS001", "Unrecognized SignatureStandard");
				return null;
		}
	}
	public static String valueOf(SignatureStandard signatureStandard, Error error) {
		switch (signatureStandard) {
			case CMS:
				return "CMS";
			default:
				error.setError("SS002", "Unrecognized SignatureStandard");
				return "";
		}
	}
}
