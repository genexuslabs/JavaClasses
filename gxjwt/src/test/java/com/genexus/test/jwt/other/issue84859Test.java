package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class issue84859Test extends SecurityAPITestObject {

	protected static String token;
	protected static JWTCreator jwt;

	public static Test suite() {
		return new TestSuite(issue84859Test.class);
	}

	@Override
	public void runTest() {
		testMalformedPayload();
		testMalformedHeader();
		testMalformedID();
	}

	@Override
	public void setUp() {

		token = "dummy";
		jwt = new JWTCreator();
	}

	public void testMalformedPayload()
	{
		String res = jwt.getPayload(token);
		assertTrue(SecurityUtils.compareStrings(res, ""));
		assertTrue(jwt.hasError());
	}

	public void testMalformedHeader()
	{
		String res = jwt.getHeader(token);
		assertTrue(SecurityUtils.compareStrings(res, ""));
		assertTrue(jwt.hasError());
	}

	public void testMalformedID()
	{
		String res = jwt.getTokenID(token);
		assertTrue(SecurityUtils.compareStrings(res, ""));
		assertTrue(jwt.hasError());
	}
}
