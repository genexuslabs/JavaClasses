package com.genexus.cors;

import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CORSHelperTest {

	private final Supplier<String> originalSupplier = CORSHelper.allowedOriginSupplier;

	@After
	public void restoreSupplier() {
		CORSHelper.allowedOriginSupplier = originalSupplier;
	}

	private void configureAllowedOrigin(final String value) {
		CORSHelper.allowedOriginSupplier = new Supplier<String>() {
			@Override public String get() { return value; }
		};
	}

	@Test
	public void corsSupportDisabledWhenNotConfigured() {
		configureAllowedOrigin("");
		assertFalse(CORSHelper.corsSupportEnabled());
		assertNull(CORSHelper.getCORSHeaders("GET", "https://app.example", null, null));
	}

	@Test
	public void corsSupportDisabledWhenSupplierReturnsNull() {
		configureAllowedOrigin(null);
		assertFalse(CORSHelper.corsSupportEnabled());
	}

	@Test
	public void corsSupportEnabledWhenConfigured() {
		configureAllowedOrigin("https://app.example");
		assertTrue(CORSHelper.corsSupportEnabled());
	}

	@Test
	public void noHeadersWhenRequestHasNoOrigin() {
		configureAllowedOrigin("https://app.example");
		assertNull(CORSHelper.getCORSHeaders("GET", null, null, null));
		assertNull(CORSHelper.getCORSHeaders("GET", "", null, null));
	}

	@Test
	public void noHeadersWhenOriginNotInAllowlist() {
		configureAllowedOrigin("https://app.example");
		assertNull(CORSHelper.getCORSHeaders("GET", "https://evil.example", null, null));
	}

	@Test
	public void singleAllowedOriginSimpleRequest() {
		configureAllowedOrigin("https://app.example");
		HashMap<String, String> headers = CORSHelper.getCORSHeaders("GET", "https://app.example", null, null);

		assertNotNull(headers);
		assertEquals("https://app.example", headers.get("Access-Control-Allow-Origin"));
		assertEquals("Origin", headers.get("Vary"));
		assertEquals("true", headers.get("Access-Control-Allow-Credentials"));
		assertFalse("Max-Age belongs only on preflight responses", headers.containsKey("Access-Control-Max-Age"));
		assertFalse(headers.containsKey("Access-Control-Allow-Methods"));
		assertFalse(headers.containsKey("Access-Control-Allow-Headers"));
	}

	@Test
	public void preflightIncludesMaxAgeAndRequestedMethodAndHeaders() {
		configureAllowedOrigin("https://app.example");
		HashMap<String, String> headers = CORSHelper.getCORSHeaders(
			"OPTIONS", "https://app.example", "PUT", "Content-Type, X-Custom");

		assertNotNull(headers);
		assertEquals("https://app.example", headers.get("Access-Control-Allow-Origin"));
		assertEquals("Origin", headers.get("Vary"));
		assertEquals("true", headers.get("Access-Control-Allow-Credentials"));
		assertEquals("86400", headers.get("Access-Control-Max-Age"));
		assertEquals("PUT", headers.get("Access-Control-Allow-Methods"));
		assertEquals("Content-Type, X-Custom", headers.get("Access-Control-Allow-Headers"));
	}

	@Test
	public void wildcardOriginNeverCombinesWithCredentials() {
		configureAllowedOrigin("*");
		HashMap<String, String> headers = CORSHelper.getCORSHeaders("GET", "https://anything.example", null, null);

		assertNotNull(headers);
		assertEquals("*", headers.get("Access-Control-Allow-Origin"));
		assertFalse("'*' must not be sent with credentials per the CORS spec",
			headers.containsKey("Access-Control-Allow-Credentials"));
		assertFalse("Vary: Origin is unnecessary when emitting '*'",
			headers.containsKey("Vary"));
	}

	@Test
	public void wildcardOriginPreflightIncludesMaxAge() {
		configureAllowedOrigin("*");
		HashMap<String, String> headers = CORSHelper.getCORSHeaders(
			"OPTIONS", "https://anything.example", "POST", "Content-Type");

		assertNotNull(headers);
		assertEquals("*", headers.get("Access-Control-Allow-Origin"));
		assertEquals("86400", headers.get("Access-Control-Max-Age"));
		assertEquals("POST", headers.get("Access-Control-Allow-Methods"));
	}

	@Test
	public void allowlistMatchesOneOfMultiple() {
		configureAllowedOrigin("https://a.example, https://b.example ,https://c.example");

		HashMap<String, String> b = CORSHelper.getCORSHeaders("GET", "https://b.example", null, null);
		assertNotNull(b);
		assertEquals("https://b.example", b.get("Access-Control-Allow-Origin"));
		assertEquals("Origin", b.get("Vary"));
		assertEquals("true", b.get("Access-Control-Allow-Credentials"));

		assertNull(CORSHelper.getCORSHeaders("GET", "https://d.example", null, null));
	}

	@Test
	public void mapOverloadReadsOriginAndIsCaseInsensitive() {
		configureAllowedOrigin("https://app.example");
		Map<String, List<String>> requestHeaders = new LinkedHashMap<>();
		requestHeaders.put("origin", Collections.singletonList("https://app.example"));
		requestHeaders.put("access-control-request-method", Collections.singletonList("DELETE"));
		requestHeaders.put("access-control-request-headers", Arrays.asList("X-A, X-B"));

		HashMap<String, String> headers = CORSHelper.getCORSHeaders("OPTIONS", requestHeaders);
		assertNotNull(headers);
		assertEquals("https://app.example", headers.get("Access-Control-Allow-Origin"));
		assertEquals("DELETE", headers.get("Access-Control-Allow-Methods"));
		assertEquals("X-A, X-B", headers.get("Access-Control-Allow-Headers"));
	}

	@Test
	public void mapOverloadReturnsNullWithoutOrigin() {
		configureAllowedOrigin("https://app.example");
		Map<String, List<String>> requestHeaders = new LinkedHashMap<>();
		requestHeaders.put("Access-Control-Request-Method", Collections.singletonList("POST"));

		assertNull(CORSHelper.getCORSHeaders("OPTIONS", requestHeaders));
	}

	@Test
	public void isPreflightSemantics() {
		assertTrue(CORSHelper.isPreflight("OPTIONS", "https://x", "GET"));
		assertTrue(CORSHelper.isPreflight("options", "https://x", "GET"));
		assertFalse(CORSHelper.isPreflight("GET", "https://x", "GET"));
		assertFalse(CORSHelper.isPreflight("OPTIONS", null, "GET"));
		assertFalse(CORSHelper.isPreflight("OPTIONS", "", "GET"));
		assertFalse(CORSHelper.isPreflight("OPTIONS", "https://x", null));
		assertFalse(CORSHelper.isPreflight("OPTIONS", "https://x", ""));
		assertFalse(CORSHelper.isPreflight(null, "https://x", "GET"));
	}

	@Test
	public void nullHttpMethodDoesNotThrow() {
		configureAllowedOrigin("https://app.example");
		HashMap<String, String> headers = CORSHelper.getCORSHeaders(null, "https://app.example", null, null);
		assertNotNull(headers);
		assertEquals("https://app.example", headers.get("Access-Control-Allow-Origin"));
		assertFalse(headers.containsKey("Access-Control-Max-Age"));
	}

	@Test
	public void preflightWithoutRequestedMethodOrHeadersOmitsThem() {
		configureAllowedOrigin("https://app.example");
		HashMap<String, String> headers = CORSHelper.getCORSHeaders(
			"OPTIONS", "https://app.example", null, null);
		assertNotNull(headers);
		assertEquals("86400", headers.get("Access-Control-Max-Age"));
		assertFalse(headers.containsKey("Access-Control-Allow-Methods"));
		assertFalse(headers.containsKey("Access-Control-Allow-Headers"));
	}
}
