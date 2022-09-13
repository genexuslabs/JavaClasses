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


	public static boolean corsSupportEnabled() {
		return getAllowedOrigin() != null;
	}

	public static HashMap<String, String> getCORSHeaders(Map<String, List<String>> headers) {
		String corsAllowedOrigin = getAllowedOrigin();
		if (corsAllowedOrigin == null) return null;

		String requestedMethod = getHeaderValue(REQUEST_METHOD_HEADER_NAME, headers);
		String requestedHeaders = getHeaderValue(REQUEST_HEADERS_HEADER_NAME, headers);
		if (requestedMethod == null) {
			return null;
		}

		return corsHeaders(corsAllowedOrigin, requestedMethod, requestedHeaders);
	}

	public static HashMap<String, String> getCORSHeaders(String requestedMethod, String requestedHeaders) {
		String corsAllowedOrigin = getAllowedOrigin();

		if (corsAllowedOrigin == null || requestedMethod == null) {
			return null;
		}

		return corsHeaders(corsAllowedOrigin, requestedMethod, requestedHeaders);
	}

	private static String getAllowedOrigin() {
		String corsAllowedOrigin = SpecificImplementation.Application.getClientPreferences().getProperty(CORS_ALLOWED_ORIGIN, "");
		if (corsAllowedOrigin == null || corsAllowedOrigin.isEmpty()) {
			return null;
		}
		return corsAllowedOrigin;
	}

	private static HashMap<String, String> corsHeaders(String corsAllowedOrigin, String requestedMethod, String requestedHeaders) {
		HashMap<String, String> corsHeaders = new HashMap<>();
		corsHeaders.put("Access-Control-Allow-Origin", corsAllowedOrigin);
		corsHeaders.put("Access-Control-Allow-Credentials", "true");
		if (requestedHeaders != null && !requestedHeaders.isEmpty()) {
			corsHeaders.put("Access-Control-Allow-Headers", requestedHeaders);
		}
		corsHeaders.put("Access-Control-Allow-Methods", requestedMethod);
		corsHeaders.put("Access-Control-Max-Age", CORS_MAX_AGE_SECONDS);
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
