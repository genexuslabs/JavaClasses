package com.genexus.test.encoders;

import com.genexus.securityapicommons.encoders.Base64Encoder;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Base64Test extends SecurityAPITestObject {

	protected static String expected_plainText;
	protected static String expected_encoded;
	protected static String expected_hexaText;
	protected static Base64Encoder base64;

	public static Test suite() {
		return new TestSuite(Base64Test.class);
	}

	@Override
	public void runTest() {
		testToBase64();
		testToPlainText();
		testToStringHexa();
		testFromStringHexaToBase64();
	}

	@Override
	public void setUp() {
		expected_plainText = "hello world";
		expected_encoded = "aGVsbG8gd29ybGQ=";
		expected_hexaText = "68656C6C6F20776F726C64";
		base64 = new Base64Encoder();
	}

	public void testToBase64() {
		String encoded = base64.toBase64(expected_plainText);
		Equals(expected_encoded, encoded, base64);
	}

	public void testToPlainText() {
		String plainText = base64.toPlainText(expected_encoded);
		Equals(expected_plainText, plainText, base64);
	}

	public void testToStringHexa() {
		String hexaText = base64.toStringHexa(expected_encoded);
		Equals(expected_hexaText, hexaText, base64);
	}

	public void testFromStringHexaToBase64() {
		String encoded = base64.fromStringHexaToBase64(expected_hexaText);
		Equals(expected_encoded, encoded, base64);
	}
}
