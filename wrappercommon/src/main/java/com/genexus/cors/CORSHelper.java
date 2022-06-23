package com.genexus.cors;

import java.util.HashMap;

public class CORSHelper {
	private static String CORS_ALLOWED_ORIGINS_ENV_VAR_NAME = "GX_CORS_ALLOW_ORIGIN";
	private static String CORS_MAX_AGE_SECONDS = "86400";
	private static String CORS_ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
	private static String CORS_ALLOWED_HEADERS = "*";

	public static HashMap<String, String> getCORSHeaders() {
		String corsAllowedOrigin = System.getenv(CORS_ALLOWED_ORIGINS_ENV_VAR_NAME);
		if (corsAllowedOrigin == null || corsAllowedOrigin.isEmpty()) {
			return null;
		}
		HashMap<String, String> corsHeaders = new HashMap<>();
		corsHeaders.put(
			"Access-Control-Allow-Origin", corsAllowedOrigin);
		corsHeaders.put(
			"Access-Control-Allow-Credentials", "true");
		corsHeaders.put(
			"Access-Control-Allow-Headers",
			CORS_ALLOWED_HEADERS);
		corsHeaders.put(
			"Access-Control-Allow-Methods",
			CORS_ALLOWED_METHODS);
		corsHeaders.put(
			"Access-Control-Max-Age",
			CORS_MAX_AGE_SECONDS);
		return corsHeaders;
	}
}
