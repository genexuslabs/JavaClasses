package com.genexus.test.keys;

import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Base64PrivateKeyTest extends SecurityAPITestObject {

	protected static String path;
	protected static String base64string;
	protected static String base64Wrong;

	public static Test suite() {
		return new TestSuite(Base64PrivateKeyTest.class);
	}

	@Override
	public void runTest() {
		testImport();
		testExport();
		testWrongBase64();
	}

	@Override
	public void setUp() {
		path = resources.concat("/sha256d_key.pem");
		base64string = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMZ8m4ftIhfrdugi5kEszRZr5IRuqGDLTex+CfVnhnBYXyQgJXeCI0eyRYUAbNzw/9MPdFN//pV26AXeH/ajORVu1JVoOACZdNOIPFnwXXh8oBxNxLAYlqoK2rAL+/tns8rKqqS4p8HSat9tj07TUXnsYJmmbXJM/eB94Ex66D1ZAgMBAAECgYA1xrTs0taV3HnO0wXHSrgWBw1WxBRihTKLjGpuTqoh7g943izIgD3GwwoKyt6zzafCK0G9DcSQAjNCw7etPvPL3FxwhDl+AHSv9JcChk/auICtMWwjurG4npto+s3byj/N00Idpz1xuOgKd8k9sdoPBGKa8l+LL+adSXzoivLG8QJBAPDvbOLSs9petB2iM6w5/DiC8EoxqDaBc7I1JFCvPOfB7i1GFFxkQ7hlgxpvaPX3NHXjAZpgdOW68P/SjU0izKsCQQDS5bjrNo3xn/MbYKojzwprR/Bo8Kvbi4/2M9NE3GwHegVmx5I+df+J0aObrbBNPLs/rhrFtt12OtgxJaac+FYLAkEA8DUUbvO4wj7m/iBnug65irHo1V+6oFThv0tCIHsFkt4DEvoqdI62AZKbafCnSYqjr+CaCYqfIScG/Vay77OBLwJBAI8EYAmKPmn7+SW4wMh1z+/+ogbYJwNEOoVQkdXh0JSlZ+JSNleLN5ajhtq8x5EpPSYrEFbB8p8JurBhgwJx2g8CQQDrp9scoK8eKBJ2p/63xqLGYSN6OZQo/4Lkq3983rmHoDCAp3Bz1zUyxQB3UVyrOj4U44C7RtDNiMSZuCwvjYAI";
		base64Wrong = "--BEGINKEY--sdssf--ENDKEY—";

	}

	public void testImport() {
		PrivateKeyManager pkm = new PrivateKeyManager();
		boolean loaded = pkm.fromBase64(base64string);
		True(loaded, pkm);
	}

	public void testExport() {
		PrivateKeyManager pkm = new PrivateKeyManager();
		pkm.load(path);
		String base64res = pkm.toBase64();
		assertTrue(SecurityUtils.compareStrings(base64res, base64string));
		assertFalse(pkm.hasError());
	}

	public void testWrongBase64()
	{
		PrivateKeyManager pkm = new PrivateKeyManager();
		pkm.fromBase64(base64Wrong);
		assertTrue(pkm.hasError());
	}
}
