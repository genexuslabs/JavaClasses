package com.genexus.utils;

import com.genexus.securityapicommons.commons.Error;

public enum KeyInfoType {

	NONE, KeyValue, X509Certificate,;

	public static KeyInfoType getKeyInfoType(String keyInfoType, Error error) {
		switch (keyInfoType.trim()) {
			case "NONE":
				return NONE;
			case "KeyValue":
				return KeyValue;
			case "X509Certificate":
				return X509Certificate;
			default:
				error.setError("KIT01", "Unrecognized KeyInfoType");
				return NONE;
		}

	}

	public static String valueOf(KeyInfoType keyInfoType, Error error) {
		switch (keyInfoType) {
			case NONE:
				return "NONE";
			case KeyValue:
				return "KeyValue";
			case X509Certificate:
				return "X509Certificate";
			default:
				error.setError("KIT02", "Unrecognized KeyInfoType");
				return "";
		}
	}
}
