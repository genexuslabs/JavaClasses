package com.genexus.cryptography.test.asymmetric;

import com.genexus.cryptography.asymmetric.AsymmetricCipher;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.config.Global;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RSAEncryptionTest extends SecurityAPITestObject {

	private static String path_RSA_sha1_1024;
	private static String path_RSA_sha256_1024;
	private static String path_RSA_sha256_2048;
	private static String path_RSA_sha512_2048;

	private static String[] arrayPaddings;

	private static String plainText;

	private static String plainText16;
	private static String plainText32;

	private static String alias;
	private static String password;

	private static String[] encodings;
	private static EncodingUtil eu;

	@Override
	protected void setUp() {

		path_RSA_sha1_1024 = resources.concat("/dummycerts/RSA_sha1_1024/");
		path_RSA_sha256_1024 = resources.concat("/dummycerts/RSA_sha256_1024/");
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		path_RSA_sha512_2048 = resources.concat("/dummycerts/RSA_sha512_2048/");

		arrayPaddings = new String[] { "OAEPPADDING", "PCKS1PADDING", "ISO97961PADDING" };

		plainText = "";
		plainText16 = "Lorem ipsum dolor sit amet";
		plainText32 = "Lorem ipsum";

		alias = "1";
		password = "dummy";

		encodings = new String[] { "UTF_8", "UTF_16", "UTF_16BE", "UTF_16LE", "UTF_32", "UTF_32BE", "UTF_32LE", "SJIS",
			"GB2312" };

		eu = new EncodingUtil();

	}

	private void bulkTest(PrivateKeyManager privateKey, PublicKey cert, String hashAlgorithm, boolean isPublicKey) {
		String enc = Global.getGlobalEncoding();
		if (SecurityUtils.compareStrings(enc, "UTF_32") || SecurityUtils.compareStrings(enc, "UTF_32BE")
			|| SecurityUtils.compareStrings(enc, "UTF_32LE")) {
			plainText = eu.getString(eu.getBytes(plainText32));
		} else {

			plainText = eu.getString(eu.getBytes(plainText16));
		}

		for (int p = 0; p < arrayPaddings.length; p++) {

			AsymmetricCipher asymCipher = new AsymmetricCipher();
			String encrypted1 = asymCipher.doEncrypt_WithPrivateKey(hashAlgorithm, arrayPaddings[p], privateKey,
				plainText);
			String decrypted1 = isPublicKey ? asymCipher.doDecrypt_WithPublicKey(hashAlgorithm, arrayPaddings[p], cert, encrypted1): asymCipher.doDecrypt_WithCertificate(hashAlgorithm, arrayPaddings[p], (CertificateX509)cert, encrypted1);
			assertEquals(decrypted1, plainText);
			True(SecurityUtils.compareStrings(decrypted1, plainText), asymCipher);
			String encrypted2 = isPublicKey ? asymCipher.doEncrypt_WithPublicKey(hashAlgorithm, arrayPaddings[p], cert, plainText): asymCipher.doEncrypt_WithCertificate(hashAlgorithm, arrayPaddings[p], (CertificateX509)cert, plainText);
			String decrypted2 = asymCipher.doDecrypt_WithPrivateKey(hashAlgorithm, arrayPaddings[p], privateKey,
				encrypted2);
			assertTrue(SecurityUtils.compareStrings(decrypted2, plainText));
			True(SecurityUtils.compareStrings(decrypted2, plainText), asymCipher);
		}
	}

	public static Test suite() {
		return new TestSuite(RSAEncryptionTest.class);
	}

	private void runTestWithEncoding(PrivateKeyManager key, PublicKey cert, String hash, boolean isPublicKey) {
		for (int i = 0; i < encodings.length; i++) {
			eu.setEncoding(encodings[i]);
			bulkTest(key, cert, hash, isPublicKey);
		}
	}

	public void test_sha1_1024_DER() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA1", false);

	}

	public void test_sha1_1024_PEM() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA1", false);

	}



	public void test_sha1_1024_PKCS12() {
		String pathKey = path_RSA_sha1_1024 + "sha1_cert.p12";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		runTestWithEncoding(key, cert, "SHA1", false);

	}



	public void test_sha256_1024_DER() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_sha256_1024_PEM() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}



	public void test_sha256_1024_PKCS12() {
		String pathKey = path_RSA_sha256_1024 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_sha256_2048_DER() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_sha256_2048_PEM() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}



	public void test_sha256_2048_PKCS12() {
		String pathKey = path_RSA_sha256_2048 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_sha512_2048_DER() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA512", false);

	}

	public void test_sha512_2048_PEM() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA512", false);

	}




	public void test_sha512_2048_PKCS12() {
		String pathKey = path_RSA_sha512_2048 + "sha512_cert.p12";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		runTestWithEncoding(key, cert, "SHA512", false);

	}

	public void test_base64()
	{
		String base64stringCert = "MIIC/DCCAmWgAwIBAgIJAPmCVmfcc0IXMA0GCSqGSIb3DQEBCwUAMIGWMQswCQYDVQQGEwJVWTETMBEGA1UECAwKTW9udGV2aWRlbzETMBEGA1UEBwwKTW9udGV2aWRlbzEQMA4GA1UECgwHR2VuZVh1czERMA8GA1UECwwIU2VjdXJpdHkxEjAQBgNVBAMMCXNncmFtcG9uZTEkMCIGCSqGSIb3DQEJARYVc2dyYW1wb25lQGdlbmV4dXMuY29tMB4XDTIwMDcwODE4NDkxNVoXDTI1MDcwNzE4NDkxNVowgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMZ8m4ftIhfrdugi5kEszRZr5IRuqGDLTex+CfVnhnBYXyQgJXeCI0eyRYUAbNzw/9MPdFN//pV26AXeH/ajORVu1JVoOACZdNOIPFnwXXh8oBxNxLAYlqoK2rAL+/tns8rKqqS4p8HSat9tj07TUXnsYJmmbXJM/eB94Ex66D1ZAgMBAAGjUDBOMB0GA1UdDgQWBBTfXY8eOfDONCZpFE0V34mJJeCYtTAfBgNVHSMEGDAWgBTfXY8eOfDONCZpFE0V34mJJeCYtTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4GBAAPv7AFlCSpJ32c/VYowlbk6UBhOKmVWBQlrAtvVQYtCKO/y9CEB8ikG19c8lHM9axnsbZR+G7g04Rfuiea3T7VPkSmUXPpz5fl6Zyk4LZg5Oji7MMMXGmr+7cpYWRhifCVwoxSgZEXt3d962IZ1Wei0LMO+4w4gnzPxqr8wVHnT";
		String base64stringKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMZ8m4ftIhfrdugi5kEszRZr5IRuqGDLTex+CfVnhnBYXyQgJXeCI0eyRYUAbNzw/9MPdFN//pV26AXeH/ajORVu1JVoOACZdNOIPFnwXXh8oBxNxLAYlqoK2rAL+/tns8rKqqS4p8HSat9tj07TUXnsYJmmbXJM/eB94Ex66D1ZAgMBAAECgYA1xrTs0taV3HnO0wXHSrgWBw1WxBRihTKLjGpuTqoh7g943izIgD3GwwoKyt6zzafCK0G9DcSQAjNCw7etPvPL3FxwhDl+AHSv9JcChk/auICtMWwjurG4npto+s3byj/N00Idpz1xuOgKd8k9sdoPBGKa8l+LL+adSXzoivLG8QJBAPDvbOLSs9petB2iM6w5/DiC8EoxqDaBc7I1JFCvPOfB7i1GFFxkQ7hlgxpvaPX3NHXjAZpgdOW68P/SjU0izKsCQQDS5bjrNo3xn/MbYKojzwprR/Bo8Kvbi4/2M9NE3GwHegVmx5I+df+J0aObrbBNPLs/rhrFtt12OtgxJaac+FYLAkEA8DUUbvO4wj7m/iBnug65irHo1V+6oFThv0tCIHsFkt4DEvoqdI62AZKbafCnSYqjr+CaCYqfIScG/Vay77OBLwJBAI8EYAmKPmn7+SW4wMh1z+/+ogbYJwNEOoVQkdXh0JSlZ+JSNleLN5ajhtq8x5EpPSYrEFbB8p8JurBhgwJx2g8CQQDrp9scoK8eKBJ2p/63xqLGYSN6OZQo/4Lkq3983rmHoDCAp3Bz1zUyxQB3UVyrOj4U44C7RtDNiMSZuCwvjYAI";
		PrivateKeyManager key = new PrivateKeyManager();
		key.fromBase64(base64stringKey);
		CertificateX509 cert = new CertificateX509();
		cert.fromBase64(base64stringCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}
	public void test_base64_PublicKey()
	{
		String base64stringCert = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGfJuH7SIX63boIuZBLM0Wa+SEbqhgy03sfgn1Z4ZwWF8kICV3giNHskWFAGzc8P/TD3RTf/6VdugF3h/2ozkVbtSVaDgAmXTTiDxZ8F14fKAcTcSwGJaqCtqwC/v7Z7PKyqqkuKfB0mrfbY9O01F57GCZpm1yTP3gfeBMeug9WQIDAQAB";
		String base64stringKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMZ8m4ftIhfrdugi5kEszRZr5IRuqGDLTex+CfVnhnBYXyQgJXeCI0eyRYUAbNzw/9MPdFN//pV26AXeH/ajORVu1JVoOACZdNOIPFnwXXh8oBxNxLAYlqoK2rAL+/tns8rKqqS4p8HSat9tj07TUXnsYJmmbXJM/eB94Ex66D1ZAgMBAAECgYA1xrTs0taV3HnO0wXHSrgWBw1WxBRihTKLjGpuTqoh7g943izIgD3GwwoKyt6zzafCK0G9DcSQAjNCw7etPvPL3FxwhDl+AHSv9JcChk/auICtMWwjurG4npto+s3byj/N00Idpz1xuOgKd8k9sdoPBGKa8l+LL+adSXzoivLG8QJBAPDvbOLSs9petB2iM6w5/DiC8EoxqDaBc7I1JFCvPOfB7i1GFFxkQ7hlgxpvaPX3NHXjAZpgdOW68P/SjU0izKsCQQDS5bjrNo3xn/MbYKojzwprR/Bo8Kvbi4/2M9NE3GwHegVmx5I+df+J0aObrbBNPLs/rhrFtt12OtgxJaac+FYLAkEA8DUUbvO4wj7m/iBnug65irHo1V+6oFThv0tCIHsFkt4DEvoqdI62AZKbafCnSYqjr+CaCYqfIScG/Vay77OBLwJBAI8EYAmKPmn7+SW4wMh1z+/+ogbYJwNEOoVQkdXh0JSlZ+JSNleLN5ajhtq8x5EpPSYrEFbB8p8JurBhgwJx2g8CQQDrp9scoK8eKBJ2p/63xqLGYSN6OZQo/4Lkq3983rmHoDCAp3Bz1zUyxQB3UVyrOj4U44C7RtDNiMSZuCwvjYAI";
		PrivateKeyManager key = new PrivateKeyManager();
		key.fromBase64(base64stringKey);
		PublicKey cert = new PublicKey();
		cert.fromBase64(base64stringCert);
		runTestWithEncoding(key, cert, "SHA256", true);

	}

	public void test_sha512_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha512_2048 + "sha512_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA512", false);

	}

	public void test_sha1_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha1_1024 + "sha1_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA1", false);

	}

	public void test_sha256_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_1024 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_sha256_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_2048 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_sha512_2048_PEM_PublicKey() {
		String pathKey = path_RSA_sha512_2048 + "sha512_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		PublicKey cert = new PublicKey();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA512", true);

	}

	public void test_sha1_1024_PEM_PublicKey() {
		String pathKey = path_RSA_sha1_1024 + "sha1_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		PublicKey cert = new PublicKey();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA1", true);

	}

	public void test_sha256_1024_PEM_PublicKey() {
		String pathKey = path_RSA_sha256_1024 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		PublicKey cert = new PublicKey();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", true);

	}

	public void test_sha256_2048_PEM_PublicKey() {
		String pathKey = path_RSA_sha256_2048 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		PublicKey cert = new PublicKey();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", true);

	}

	@Override
	public void runTest() {
		test_sha1_1024_DER();
		test_sha1_1024_PEM();
		test_sha1_1024_PEM_Encrypted();
		test_sha1_1024_PKCS12();

		test_sha256_1024_DER();
		test_sha256_1024_PEM();
		test_sha256_1024_PEM_Encrypted();
		test_sha256_1024_PKCS12();

		test_sha256_2048_DER();
		test_sha256_2048_PEM();
		test_sha256_2048_PEM_Encrypted();
		test_sha256_2048_PKCS12();

		test_sha512_2048_DER();
		test_sha512_2048_PEM();
		test_sha512_2048_PEM_Encrypted();
		test_sha512_2048_PKCS12();

		test_base64();
		test_base64_PublicKey();

		test_sha1_1024_PEM_PublicKey();
		test_sha256_1024_PEM_PublicKey();
		test_sha256_2048_PEM_PublicKey();
		test_sha512_2048_PEM_PublicKey();

	}

}
