package com.genexus.securityapicommons.config;

public final class Global {

	private static String GLOBAL_ENCODING;

	public static void setGlobalEncoding(String val) {

		GLOBAL_ENCODING = val;

	}

	public static String getGlobalEncoding() {
		if (GLOBAL_ENCODING == null) {
			GLOBAL_ENCODING = "UTF_8";
		}
		return GLOBAL_ENCODING;
	}

}
