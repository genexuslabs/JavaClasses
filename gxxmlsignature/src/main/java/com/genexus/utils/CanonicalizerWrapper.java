package com.genexus.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.c14n.Canonicalizer;

@SuppressWarnings({"LoggingSimilarMessage", "unused"})
public enum CanonicalizerWrapper {

	ALGO_ID_C14N_WITH_COMMENTS, ALGO_ID_C14N_OMIT_COMMENTS, ALGO_ID_C14N_EXCL_OMIT_COMMENTS, ALGO_ID_C14N_EXCL_WITH_COMMENTS,
	;

	private static final Logger logger = LogManager.getLogger(CanonicalizerWrapper.class);

	public static CanonicalizerWrapper getCanonicalizerWrapper(String canonicalizerWrapper, Error error) {
		switch (canonicalizerWrapper.trim()) {
			case "C14n_WITH_COMMENTS":
				return CanonicalizerWrapper.ALGO_ID_C14N_WITH_COMMENTS;
			case "C14n_OMIT_COMMENTS":
				return CanonicalizerWrapper.ALGO_ID_C14N_OMIT_COMMENTS;
			case "exc_C14n_OMIT_COMMENTS":
				return CanonicalizerWrapper.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
			case "exc_C14N_WITH_COMMENTS":
				return CanonicalizerWrapper.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
			default:
				error.setError("CAW01", "Unrecognized CanonicalizationMethod");
				logger.error("Unrecognized CanonicalizationMethod");
				return null;
		}
	}

	public static String valueOf(CanonicalizerWrapper canonicalizerWrapper, Error error) {
		switch (canonicalizerWrapper) {
			case ALGO_ID_C14N_WITH_COMMENTS:
				return "C14n_WITH_COMMENTS";
			case ALGO_ID_C14N_OMIT_COMMENTS:
				return "C14n_OMIT_COMMENTS";
			case ALGO_ID_C14N_EXCL_OMIT_COMMENTS:
				return "exc_C14n_OMIT_COMMENTS";
			case ALGO_ID_C14N_EXCL_WITH_COMMENTS:
				return "exc_C14N_WITH_COMMENTS";
			default:
				error.setError("CAW02", "Unrecognized CanonicalizationMethod");
				logger.error("Unrecognized CanonicalizationMethod");
				return "";
		}
	}

	public static String valueOfInternal(CanonicalizerWrapper canonicalizerWrapper, Error error) {
		switch (canonicalizerWrapper) {
			case ALGO_ID_C14N_WITH_COMMENTS:
				return "ALGO_ID_C14N_WITH_COMMENTS";
			case ALGO_ID_C14N_OMIT_COMMENTS:
				return "ALGO_ID_C14N_OMIT_COMMENTS";
			case ALGO_ID_C14N_EXCL_OMIT_COMMENTS:
				return "ALGO_ID_C14N_EXCL_OMIT_COMMENTS";
			case ALGO_ID_C14N_EXCL_WITH_COMMENTS:
				return "ALGO_ID_C14N_EXCL_WITH_COMMENTS";
			default:
				error.setError("CAW03", "Unrecognized CanonicalizationMethod");
				logger.error("Unrecognized CanonicalizationMethod");
				return "";
		}
	}

	public static String getCanonicalizationMethodAlorithm(CanonicalizerWrapper canonicalizerWrapper, Error error) {
		switch (canonicalizerWrapper) {
			case ALGO_ID_C14N_WITH_COMMENTS:
				return Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
			case ALGO_ID_C14N_OMIT_COMMENTS:
				return Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
			case ALGO_ID_C14N_EXCL_WITH_COMMENTS:
				return Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
			case ALGO_ID_C14N_EXCL_OMIT_COMMENTS:
				return Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
			default:
				error.setError("CAW04", "Unrecognized CanonicalizationMethod");
				logger.error("Unrecognized CanonicalizationMethod");
				return null;

		}
	}
}
