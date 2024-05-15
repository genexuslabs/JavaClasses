package com.genexus.test.encoders;

import com.genexus.securityapicommons.encoders.Base64UrlEncoder;
import com.genexus.test.commons.SecurityAPITestObject;
import junit.framework.Test;
import junit.framework.TestSuite;

public class Base64UrlTest extends SecurityAPITestObject {

	protected static String expected_plainText;
	protected static String expected_encoded;
	protected static String expected_hexaText;
	protected static Base64UrlEncoder base64;

	protected static String expected_base64;

	public static Test suite() {
		return new TestSuite(Base64UrlTest.class);
	}

	@Override
	public void setUp() {
		expected_plainText = "hello world";
		expected_encoded = "aGVsbG8gd29ybGQ.";
		expected_base64 = "aGVsbG8gd29ybGQ=";
		expected_hexaText = "68656c6c6f20776f726c64";
		base64 = new Base64UrlEncoder();
	}
	@Override
	public void runTest() {
		testToBase64Url();
		testToPlainTextUrl();
		testToStringHexaUrl();
		testFromStringHexaToBase64Url();
		testBase64UrlToBase64();
		testBase64ToBase64Url();

	}



	public void testToBase64Url() {
		String encoded = base64.toBase64(expected_plainText);
		Equals(expected_encoded, encoded, base64);
	}

	public void testToPlainTextUrl() {
		String plainText = base64.toPlainText(expected_encoded);
		Equals(expected_plainText, plainText, base64);
	}

	public void testToStringHexaUrl() {
		String hexaText = base64.toStringHexa(expected_encoded);
		Equals(expected_hexaText, hexaText, base64);
	}

	public void testFromStringHexaToBase64Url() {
		String encoded = base64.fromStringHexaToBase64(expected_hexaText);
		Equals(expected_encoded, encoded, base64);
	}

	public void testBase64UrlToBase64()
	{
		String encoded = base64.base64UrlToBase64(expected_encoded);
		Equals(expected_base64, encoded, base64);
	}

	public void testBase64ToBase64Url()
	{
		String encoded = base64.base64ToBase64Url(expected_base64);
		Equals(expected_encoded, encoded, base64);
	}

}
