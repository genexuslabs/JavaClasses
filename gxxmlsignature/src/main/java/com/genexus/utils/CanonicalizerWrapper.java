package com.genexus.utils;

import org.apache.xml.security.c14n.Canonicalizer;

import com.genexus.securityapicommons.commons.Error;

/**
 * @author sgrampone
 *
 */
public enum CanonicalizerWrapper {

	ALGO_ID_C14N_WITH_COMMENTS, ALGO_ID_C14N_OMIT_COMMENTS, ALGO_ID_C14N_EXCL_OMIT_COMMENTS, ALGO_ID_C14N_EXCL_WITH_COMMENTS,;

	/**
	 * Mapping between String name and CanonicalizerWrapper enum representation
	 *
	 * @param canonicalizerWrapper
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return CanonicalizerWrapper enum representation
	 */
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
				return null;
		}
	}

	/**
	 * @param canonicalizerWrapper
	 *            CanonicalizerWrapper enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return String CanonicalizerWrapper name
	 */
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
				return "";
		}
	}

	/**
	 * @param canonicalizerWrapper
	 *            CanonicalizerWrapper enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return String CanonicalizerWrapper name
	 */
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
				return null;

		}
	}
}
