package com.genexus.cors;

import com.genexus.common.interfaces.SpecificImplementation;

import java.util.HashMap;

public class CORSHelper {
	private static String CORS_ALLOWED_ORIGIN = "CORS_ALLOW_ORIGIN";
	private static String CORS_MAX_AGE_SECONDS = "86400";
	private static String CORS_ALLOWED_METHODS = "GET, POST, PUT, DELETE, HEAD";
	private static String CORS_ALLOWED_HEADERS = "*";

	public static HashMap<String, String> getCORSHeaders(String requestRequiredHeaders) {
		return getCORSHeaders(requestRequiredHeaders, CORS_ALLOWED_METHODS);
	}
	public static HashMap<String, String> getCORSHeaders(String requestRequiredHeaders, String allowedMethods) {
		String corsAllowedOrigin = SpecificImplementation.Application.getClientPreferences().getProperty(CORS_ALLOWED_ORIGIN, "");
		if (corsAllowedOrigin == null || corsAllowedOrigin.isEmpty() || allowedMethods.isEmpty()) {
			return null;
		}

		HashMap<String, String> corsHeaders = new HashMap<>();
		corsHeaders.put(
			"Access-Control-Allow-Origin", corsAllowedOrigin);
		corsHeaders.put(
			"Access-Control-Allow-Credentials", "true");
		corsHeaders.put(
			"Access-Control-Allow-Headers",
			requestRequiredHeaders == null || requestRequiredHeaders.isEmpty() ? CORS_ALLOWED_HEADERS : requestRequiredHeaders);
		corsHeaders.put(
			"Access-Control-Allow-Methods",
			allowedMethods);
		corsHeaders.put(
			"Access-Control-Max-Age",
			CORS_MAX_AGE_SECONDS);
		return corsHeaders;
	}
}
