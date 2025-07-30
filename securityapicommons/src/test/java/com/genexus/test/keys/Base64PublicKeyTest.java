package com.genexus.test.keys;

import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.TestSuite;
import org.junit.BeforeClass;
import org.junit.Test;

public class Base64PublicKeyTest extends SecurityAPITestObject {

	protected static String path;
	protected static String base64string;
	protected static String base64Wrong;

	@BeforeClass
	public static void setUp() {

		path = resources.concat("/sha256_pubkey.pem");
		base64Wrong = "--BEGINKEY--sdssf--ENDKEY—";
		base64string = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAudCzBe6bGw0LIAGXa7/6tExlgak3g5j0NemlqG59839vJDm/T3YNhntfhuelYjvGg/3g1Ghouwi7SHOvX7mYxWdFurJ5Tbyimv+ncCrRhfz2629um6YcMXt3lU9VIn7AB/2L/oKTk939B0IF+dsqMkQUAz8cEpuEbaTQW2ieQdLH+bE3RNMYvd0G5Vyk7innt9aoqoqJXcy0qjv07FuGKAuPLkjdjE2E5sTSfsHIogO3k7iq8nUO9ExO+It8bgP9Td/tW+a5wCfHJAlzq66h0vhewbmI9d2OAVf3AsgN0o8C+m4ztsRsO92oYemaYbUl9iWEGJmA0sqejRjKKxjUOQIDAQAB";
	}

	@Test
	public void testImport() {
		PublicKey cert = new PublicKey();
		boolean loaded = cert.fromBase64(base64string);
		True(loaded, cert);
	}

	@Test
	public void testExport() {
		PublicKey cert = new PublicKey();
		cert.load(path);
		String base64res = cert.toBase64();
		assertTrue(SecurityUtils.compareStrings(base64res, base64string));
		assertFalse(cert.hasError());
	}

	@Test
	public void testWrongBase64() {
		PublicKey cert = new PublicKey();
		cert.fromBase64(base64Wrong);
		assertTrue(cert.hasError());
	}
}
