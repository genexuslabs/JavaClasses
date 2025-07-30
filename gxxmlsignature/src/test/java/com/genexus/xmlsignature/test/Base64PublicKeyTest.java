package com.genexus.xmlsignature.test;

import com.genexus.commons.DSigOptions;
import com.genexus.dsig.XmlDSigSigner;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class Base64PublicKeyTest extends SecurityAPITestObject {

	private final String base64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDqq4yriScMFgJSHK4PsOhucubSroiseJKvys6UnveRO6tLRBlEgtkWfs7cOgEYU4Dt8hil5KhYuRuRTgBmBFTe34G2aUmp9aLbUW5lrs/zoui9rYmrKHMmCTkYuOg+J7j1YFOZ5xldTK3BpOVHV3n8S6G3/evjFvf+WlG0ypMQdQIDAQAB";

	private static String path_RSA_sha1_1024;
	private static String xmlUnsignedPath;
	private static String pathSigned;
	private static DSigOptions options;
	private static String xmlSignedPathRoot;
	private static String hash;

	@BeforeClass
	public static void setUp() {
		path_RSA_sha1_1024 = resources.concat("/dummycerts/RSA_sha1_1024/");

		options = new DSigOptions();
		options.setKeyInfoType("NONE");

		xmlUnsignedPath = resources.concat("/tosign.xml");
		pathSigned = tempFolder + "base64.xml";
		xmlSignedPathRoot = tempFolder.toString();
		hash = "SHA1";
	}

	@Test
	public void testSignBase64() {
		PublicKey newCert = new PublicKey();
		boolean loaded = newCert.fromBase64(base64);
		True(loaded, newCert);
		PrivateKeyManager key = new PrivateKeyManager();
		boolean privateLoaded = key.load(path_RSA_sha1_1024 + "sha1d_key.pem");
		True(privateLoaded, key);
		XmlDSigSigner signer = new XmlDSigSigner();
		boolean result = signer.doSignFileWithPublicKey(xmlUnsignedPath, key, newCert, xmlSignedPathRoot + pathSigned, options, hash);
		True(result, signer);
		boolean verify = signer.doVerifyFileWithPublicKey(xmlSignedPathRoot + pathSigned, newCert, options);
		True(verify, signer);
	}

	@Test
	public void testToBase64() {
		PublicKey newCert = new PublicKey();
		boolean loaded = newCert.fromBase64(base64);
		True(loaded, newCert);
		String newBase64 = newCert.toBase64();
		boolean result = SecurityUtils.compareStrings(newBase64, base64);
		True(result, newCert);
	}
}
