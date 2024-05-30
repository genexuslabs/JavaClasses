package com.genexus.xmlsignature.test;

import com.genexus.commons.DSigOptions;
import com.genexus.dsig.XmlDSigSigner;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Base64PublicKeyTest extends SecurityAPITestObject{

	private String base64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCr6OYBg0NsNL3mUg8XfpyXO8O8/yLPuIhoThLivQP1O9d0XwXV2Mdqa6fuwQJFKxmJvmekpF2eq+/E5eQ/y9qTXgOp5+YHgqFh9SkhTpQLxJ413Br5HaPGyBPw2stz1hXXiBg/DLdG2nQKCDPtJurOY30rvTfKOCOFF/TuD6BMIQIDAQAB";

	private String path_RSA_sha1_1024;
	private String xmlUnsignedPath;
	private String pathSigned;
	private DSigOptions options;
	private String pathKey;
	private String xmlSignedPathRoot;
	private String hash;

	public static Test suite() {
		return new TestSuite(Base64CertificateTest.class);
	}

	@Override
	public void runTest() {
		testSignBase64();
		testToBase64();
	}

	@Override
	public void setUp() {
		path_RSA_sha1_1024 = resources.concat("/dummycerts/RSA_sha1_1024/");

		options = new DSigOptions();
		options.setKeyInfoType("NONE");

		xmlUnsignedPath = resources.concat("/tosign.xml");
		pathSigned = tempFolder + "base64.xml";
		pathKey = path_RSA_sha1_1024 + "sha1_pubkey.pem";
		xmlSignedPathRoot = tempFolder.toString(); //"C:\\Temp\\outputTestFilesJ\\";
		hash = "SHA1";
	}

	public void testSignBase64() {
		PublicKey newCert = new PublicKey();
		boolean loaded = newCert.fromBase64(base64);
		assertTrue(loaded);
		True(loaded, newCert);
		PrivateKeyManager key = new PrivateKeyManager();
		boolean privateLoaded = key.load(pathKey);
		assertTrue(privateLoaded);
		True(privateLoaded, key);
		XmlDSigSigner signer = new XmlDSigSigner();
		boolean result = signer.doSignFileWithPublicKey(xmlUnsignedPath, key, newCert, xmlSignedPathRoot + pathSigned, options, hash);
		assertTrue(result);
		True(result, signer);
		boolean verify = signer.doVerifyFileWithPublicKey(xmlSignedPathRoot + pathSigned, newCert, options);
		assertTrue(verify);
		True(verify, signer);
	}

	public void testToBase64() {
		PublicKey newCert = new PublicKey();
		boolean loaded = newCert.fromBase64(base64);
		assertTrue(loaded);
		True(loaded, newCert);
		String newBase64 = newCert.toBase64();
		boolean result = SecurityUtils.compareStrings(newBase64, base64);
		assertTrue(result);
		True(result, newCert);
	}
}
