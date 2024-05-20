package com.genexus.cryptography.test.asymmetric;

import com.genexus.cryptography.asymmetric.AsymmetricCipher;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AsymmetricDomainSpacesTest extends SecurityAPITestObject{

	private static String path_RSA_sha1_1024;
	private static String plainText;
	private static PrivateKeyManager key;
	private static CertificateX509 cert;
	private static String pathKey;
	private static String pathCert;
	private static AsymmetricCipher asymCipher;

	@Override
	protected void setUp() {
		path_RSA_sha1_1024 = resources.concat("/dummycerts/RSA_sha1_1024/");
		plainText = "Lorem ipsum";
		pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		pathCert = path_RSA_sha1_1024 + "sha1_cert.crt";
		key = new PrivateKeyManager();
		cert = new CertificateX509();
		asymCipher = new AsymmetricCipher();

	}

	public static Test suite() {
		return new TestSuite(AsymmetricDomainSpacesTest.class);
	}

	@Override
	public void runTest() {
		testSpaces();
	}

	public void testSpaces()
	{

		key.load(pathKey);
		cert.load(pathCert);
		String encrypted1 = asymCipher.doEncrypt_WithPrivateKey("SHA1 ", "PCKS1PADDING ", key, plainText);
		assertFalse(asymCipher.hasError());

		String decrypted = asymCipher.doDecrypt_WithPublicKey(" SHA1", " PCKS1PADDING", cert, encrypted1);
		assertFalse(asymCipher.hasError());
		assertTrue(SecurityUtils.compareStrings(plainText, decrypted));
	}


}
