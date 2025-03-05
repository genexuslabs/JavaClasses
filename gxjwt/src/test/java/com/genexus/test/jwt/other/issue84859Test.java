package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("NewClassNamingConvention")
public class issue84859Test extends SecurityAPITestObject {

	protected static String token;
	protected static JWTCreator jwt;

	@BeforeClass
	public static void setUp() {
		token = "dummy";
		jwt = new JWTCreator();
	}

	@Test
	public void testMalformedPayload() {
		String res = jwt.getPayload(token);
		assertTrue(SecurityUtils.compareStrings(res, ""));
		assertTrue(jwt.hasError());
	}

	@Test
	public void testMalformedHeader() {
		String res = jwt.getHeader(token);
		assertTrue(SecurityUtils.compareStrings(res, ""));
		assertTrue(jwt.hasError());
	}

	@Test
	public void testMalformedID() {
		String res = jwt.getTokenID(token);
		assertTrue(SecurityUtils.compareStrings(res, ""));
		assertTrue(jwt.hasError());
	}
}
