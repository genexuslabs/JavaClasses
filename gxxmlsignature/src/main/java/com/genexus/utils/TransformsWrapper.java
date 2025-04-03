package com.genexus.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.transforms.Transforms;

@SuppressWarnings({"LoggingSimilarMessage", "unused"})
public enum TransformsWrapper {

	ENVELOPED, ENVELOPING, DETACHED,
	;

	private static final Logger logger = LogManager.getLogger(TransformsWrapper.class);

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
				logger.error("Unrecognized transformation");
				return null;
		}
	}

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
				logger.error("Unrecognized transformation");
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
				logger.error("Unrecognized transformation");
				return null;

		}
	}

	public static String getCanonicalizationTransformation(CanonicalizerWrapper canonicalizerWrapper, Error error) {
		return CanonicalizerWrapper.getCanonicalizationMethodAlorithm(canonicalizerWrapper, error);
	}
}
