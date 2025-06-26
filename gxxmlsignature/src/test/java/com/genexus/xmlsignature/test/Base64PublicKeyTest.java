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

	private final String base64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCr6OYBg0NsNL3mUg8XfpyXO8O8/yLPuIhoThLivQP1O9d0XwXV2Mdqa6fuwQJFKxmJvmekpF2eq+/E5eQ/y9qTXgOp5+YHgqFh9SkhTpQLxJ413Br5HaPGyBPw2stz1hXXiBg/DLdG2nQKCDPtJurOY30rvTfKOCOFF/TuD6BMIQIDAQAB";

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
