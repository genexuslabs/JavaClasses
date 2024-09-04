package com.genexus.utils;

import org.apache.xml.security.transforms.Transforms;

import com.genexus.securityapicommons.commons.Error;

public enum TransformsWrapper {

	ENVELOPED, ENVELOPING, DETACHED,;

	/**
	 * Mapping between String name and TransformsWrapper enum representation
	 *
	 * @param transformsWrapper
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return TransformsWrapper enum representation
	 */
	public static TransformsWrapper getTransformsWrapper(String transformsWrapper, Error error) {
		switch (transformsWrapper.toUpperCase().trim()) {
			case "ENVELOPED":
				return TransformsWrapper.ENVELOPED;
			case "ENVELOPING":
				return TransformsWrapper.ENVELOPING;
			case "DETACHED":
				return TransformsWrapper.DETACHED;
			default:
				error.setError("TRW01", "Unrecognized transformation");
				return null;
		}
	}

	/**
	 * @param transformsWrapper
	 *            TransformsWrapper enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return String transformsWrapper name
	 */
	public static String valueOf(TransformsWrapper transformsWrapper, Error error) {
		switch (transformsWrapper) {
			case ENVELOPED:
				return "ENVELOPED";
			case ENVELOPING:
				return "ENVELOPING";
			case DETACHED:
				return "DETACHED";
			default:
				error.setError("TRW02", "Unrecognized transformation");
				return null;
		}
	}

	public static String getSignatureTypeTransform(TransformsWrapper transformsWrapper, Error error) {
		switch (transformsWrapper) {
			case ENVELOPED:
				return Transforms.TRANSFORM_ENVELOPED_SIGNATURE;
			case ENVELOPING:
				return "http://www.w3.org/2000/09/xmldsig#enveloping-signature";
			case DETACHED:
				return "http://www.w3.org/2000/09/xmldsig#detached-signature";
			default:
				error.setError("TRW03", "Unrecognized transformation");
				return null;

		}
	}

	public static String getCanonicalizationTransformation(CanonicalizerWrapper canonicalizerWrapper, Error error) {
		return CanonicalizerWrapper.getCanonicalizationMethodAlorithm(canonicalizerWrapper, error);
	}
}
