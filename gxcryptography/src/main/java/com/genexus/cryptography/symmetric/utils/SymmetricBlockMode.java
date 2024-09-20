package com.genexus.cryptography.symmetric.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum SymmetricBlockMode {

	NONE, ECB, CBC, CFB, CTR, CTS, GOFB, OFB, OPENPGPCFB, SIC, /* AEAD */ AEAD_EAX, AEAD_GCM, AEAD_KCCM, AEAD_CCM,
	;

	private static final Logger logger = LogManager.getLogger(SymmetricBlockMode.class);

	public static SymmetricBlockMode getSymmetricBlockMode(String symmetricBlockMode, Error error) {
		if (error == null) return SymmetricBlockMode.NONE;
		if (symmetricBlockMode == null) {
			error.setError("SBM04", "Unrecognized SymmetricBlockMode");
			logger.error("Unrecognized SymmetricBlockMode");
			return SymmetricBlockMode.NONE;
		}
		switch (symmetricBlockMode.toUpperCase().trim()) {
			case "ECB":
				return SymmetricBlockMode.ECB;
			case "CBC":
				return SymmetricBlockMode.CBC;
			case "CFB":
				return SymmetricBlockMode.CFB;
			case "CTS":
				return SymmetricBlockMode.CTS;
			case "GOFB":
				return SymmetricBlockMode.GOFB;
			case "OFB":
				return SymmetricBlockMode.OFB;
			case "OPENPGPCFB":
				return SymmetricBlockMode.OPENPGPCFB;
			case "SIC":
				return SymmetricBlockMode.SIC;
			case "CTR":
				return SymmetricBlockMode.CTR;

			/* AEAD */
			case "AEAD_EAX":
				return SymmetricBlockMode.AEAD_EAX;
			case "AEAD_GCM":
				return SymmetricBlockMode.AEAD_GCM;
			case "AEAD_KCCM":
				return SymmetricBlockMode.AEAD_KCCM;
			case "AEAD_CCM":
				return SymmetricBlockMode.AEAD_CCM;
			default:
				error.setError("SBM01", "Unrecognized SymmetricBlockMode");
				logger.error("Unrecognized SymmetricBlockMode");
				return null;
		}
	}

	public static String valueOf(SymmetricBlockMode symmetricBlockMode, Error error) {
		if (error == null) return "Unrecognized operation mode";

		switch (symmetricBlockMode) {
			case ECB:
				return "ECB";
			case CBC:
				return "CBC";
			case CFB:
				return "CFB";
			case CTS:
				return "CTS";
			case GOFB:
				return "GOFB";
			case OFB:
				return "OFB";
			case OPENPGPCFB:
				return "OPENPGPCFB";
			case SIC:
				return "SIC";
			case CTR:
				return "CTR";

			/* AEAD */
			case AEAD_EAX:
				return "AEAD_EAX";
			case AEAD_GCM:
				return "AEAD_GCM";
			case AEAD_KCCM:
				return "AEAD_KCCM";
			case AEAD_CCM:
				return "AEAD_CCM";
			default:
				error.setError("SBM02", "Unrecognized SymmetricBlockMode");
				logger.error("Unrecognized SymmetricBlockMode");
				return "Unrecognized operation mode";
		}
	}

	@SuppressWarnings("unused")
	public static boolean isAEAD(SymmetricBlockMode symmetricBlockMode, Error error) {
		if (error == null) return false;

		switch (symmetricBlockMode) {
			case AEAD_EAX:
			case AEAD_GCM:
			case AEAD_KCCM:
			case AEAD_CCM:
				return true;
			default:
				error.setError("SBM03", "Unrecognized Symmetric AEAD BlockMode");
				logger.error("Unrecognized Symmetric AEAD BlockMode");
				return false;
		}
	}
}
