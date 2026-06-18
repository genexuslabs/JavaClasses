package com.genexus.cors;

import com.genexus.common.interfaces.SpecificImplementation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CORSHelper {
	public static final String REQUEST_METHOD_HEADER_NAME = "Access-Control-Request-Method";
	public static final String REQUEST_HEADERS_HEADER_NAME = "Access-Control-Request-Headers";
	public static final String ORIGIN_HEADER_NAME = "Origin";

	private static final String CORS_ALLOWED_ORIGIN_PROPERTY = "CORS_ALLOW_ORIGIN";
	private static final String CORS_MAX_AGE_SECONDS = "86400";
	private static final String PREFLIGHT_REQUEST = "OPTIONS";
	private static final String WILDCARD = "*";

	// Test seam: tests can replace this to avoid wiring SpecificImplementation.
	static Supplier<String> allowedOriginSupplier = CORSHelper::readAllowedOriginFromConfig;

	public static boolean corsSupportEnabled() {
		return getConfiguredAllowedOrigin() != null;
	}

	/** Build CORS headers from a multi-valued header map (JAX-RS style). */
	public static HashMap<String, String> getCORSHeaders(String httpMethod, Map<String, List<String>> headers) {
		return corsHeaders(httpMethod,
			getHeaderValue(ORIGIN_HEADER_NAME, headers),
			getHeaderValue(REQUEST_METHOD_HEADER_NAME, headers),
			getHeaderValue(REQUEST_HEADERS_HEADER_NAME, headers));
	}

	/** Build CORS headers from individual header values (Servlet style). */
	public static HashMap<String, String> getCORSHeaders(String httpMethod, String origin, String requestedMethod, String requestedHeaders) {
		return corsHeaders(httpMethod, origin, requestedMethod, requestedHeaders);
	}

	/** True iff this request looks like a CORS preflight (OPTIONS + Origin + Access-Control-Request-Method). */
	public static boolean isPreflight(String httpMethod, String origin, String requestedMethod) {
		return httpMethod != null
			&& PREFLIGHT_REQUEST.equalsIgnoreCase(httpMethod)
			&& origin != null && !origin.isEmpty()
			&& requestedMethod != null && !requestedMethod.isEmpty();
	}

	private static String getConfiguredAllowedOrigin() {
		String value = allowedOriginSupplier.get();
		return (value == null || value.isEmpty()) ? null : value;
	}

	private static String readAllowedOriginFromConfig() {
		if (SpecificImplementation.Application == null) {
			return null;
		}
		return SpecificImplementation.Application.getClientPreferences().getProperty(CORS_ALLOWED_ORIGIN_PROPERTY, "");
	}

	/**
	 * Resolve the value to send in Access-Control-Allow-Origin, or null when the
	 * request origin is not in the configured allowlist (no CORS headers should be emitted).
	 *
	 * Configuration accepts:
	 *   "*"                                 -> allow any origin (without credentials, per spec)
	 *   "https://a.example"                 -> single origin
	 *   "https://a.example,https://b.test"  -> allowlist
	 */
	private static String resolveAllowedOrigin(String configuredOrigin, String requestOrigin) {
		if (requestOrigin == null || requestOrigin.isEmpty()) {
			return null;
		}
		if (WILDCARD.equals(configuredOrigin.trim())) {
			return WILDCARD;
		}
		for (String allowed : configuredOrigin.split(",")) {
			String candidate = allowed.trim();
			if (!candidate.isEmpty() && candidate.equals(requestOrigin)) {
				return candidate;
			}
		}
		return null;
	}

	private static HashMap<String, String> corsHeaders(String httpMethodName, String origin, String requestedMethod, String requestedHeaders) {
		String configuredOrigin = getConfiguredAllowedOrigin();
		if (configuredOrigin == null) return null;

		String allowOriginValue = resolveAllowedOrigin(configuredOrigin, origin);
		if (allowOriginValue == null) return null;

		boolean isWildcard = WILDCARD.equals(allowOriginValue);
		boolean isPreflight = httpMethodName != null && PREFLIGHT_REQUEST.equalsIgnoreCase(httpMethodName);

		HashMap<String, String> corsHeaders = new LinkedHashMap<>();
		corsHeaders.put("Access-Control-Allow-Origin", allowOriginValue);
		if (!isWildcard) {
			// Vary lets caches differentiate responses per Origin.
			corsHeaders.put("Vary", "Origin");
			// "*" + credentials is forbidden by the CORS spec, so credentials only when echoing a real origin.
			corsHeaders.put("Access-Control-Allow-Credentials", "true");
		}

		if (isPreflight) {
			corsHeaders.put("Access-Control-Max-Age", CORS_MAX_AGE_SECONDS);
			if (requestedMethod != null && !requestedMethod.isEmpty()) {
				corsHeaders.put("Access-Control-Allow-Methods", requestedMethod);
			}
			if (requestedHeaders != null && !requestedHeaders.isEmpty()) {
				corsHeaders.put("Access-Control-Allow-Headers", requestedHeaders);
			}
		}

		return corsHeaders;
	}

	private static String getHeaderValue(String headerName, Map<String, List<String>> headers) {
		if (headers == null) return null;
		List<String> value = headers.get(headerName);
		if (value == null) {
			for (Map.Entry<String, List<String>> e : headers.entrySet()) {
				if (e.getKey() != null && headerName.equalsIgnoreCase(e.getKey())) {
					value = e.getValue();
					break;
				}
			}
		}
		if (value != null && !value.isEmpty()) return value.get(0);
		return null;
	}
}
