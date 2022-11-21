package com.genexus.cors;

import com.genexus.common.interfaces.SpecificImplementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CORSHelper {
	public static String REQUEST_METHOD_HEADER_NAME = "Access-Control-Request-Method";
	public static String REQUEST_HEADERS_HEADER_NAME = "Access-Control-Request-Headers";

	private static String CORS_ALLOWED_ORIGIN = "CORS_ALLOW_ORIGIN";
	private static String CORS_MAX_AGE_SECONDS = "86400";
	private static String PREFLIGHT_REQUEST = "OPTIONS";

	public static boolean corsSupportEnabled() {
		return getAllowedOrigin() != null;
	}

	public static HashMap<String, String> getCORSHeaders(String httpMethod, Map<String, List<String>> headers) {
		if (getAllowedOrigin() == null) {
			return null;
		}

		String requestedMethod = getHeaderValue(REQUEST_METHOD_HEADER_NAME, headers);
		String requestedHeaders = getHeaderValue(REQUEST_HEADERS_HEADER_NAME, headers);

		return corsHeaders(httpMethod, requestedMethod, requestedHeaders);
	}

	public static HashMap<String, String> getCORSHeaders(String httpMethod, String requestedMethod, String requestedHeaders) {
		return corsHeaders(httpMethod, requestedMethod, requestedHeaders);
	}

	private static String getAllowedOrigin() {
		String corsAllowedOrigin = SpecificImplementation.Application.getClientPreferences().getProperty(CORS_ALLOWED_ORIGIN, "");
		if (corsAllowedOrigin == null || corsAllowedOrigin.isEmpty()) {
			return null;
		}
		return corsAllowedOrigin;
	}

	private static HashMap<String, String> corsHeaders(String httpMethodName, String requestedMethod, String requestedHeaders) {
		String corsAllowedOrigin = getAllowedOrigin();
		if (corsAllowedOrigin == null) {
			return null;
		}

		boolean isPreflightRequest = httpMethodName.equalsIgnoreCase(PREFLIGHT_REQUEST);

		HashMap<String, String> corsHeaders = new HashMap<>();
		corsHeaders.put("Access-Control-Allow-Origin", corsAllowedOrigin);
		corsHeaders.put("Access-Control-Allow-Credentials", "true");
		corsHeaders.put("Access-Control-Max-Age", CORS_MAX_AGE_SECONDS);

		if (isPreflightRequest && requestedHeaders != null && !requestedHeaders.isEmpty()) {
			corsHeaders.put("Access-Control-Allow-Headers", requestedHeaders);
		}
		if (isPreflightRequest && requestedMethod != null && !requestedMethod.isEmpty()) {
			corsHeaders.put("Access-Control-Allow-Methods", requestedMethod);
		}

		return corsHeaders;
	}

	private static String getHeaderValue(String headerName, Map<String, List<String>> headers) {
		List<String> value = headers.get(headerName);
		if (value != null && value.size() > 0) {
			return value.get(0);
		}
		return null;
	}
}
