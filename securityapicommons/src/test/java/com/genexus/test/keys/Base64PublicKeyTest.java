package com.genexus.test.keys;

import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Base64PublicKeyTest extends SecurityAPITestObject {

	protected static String path;
	protected static String base64string;
	protected static String base64Wrong;

	public static Test suite() {
		return new TestSuite(Base64PublicKeyTest.class);
	}

	@Override
	public void runTest() {
		testImport();
		testExport();
		testWrongBase64();
	}

	@Override
	public void setUp() {
		path = resources.concat("/sha256_pubkey.pem");
		base64Wrong = "--BEGINKEY--sdssf--ENDKEY—";
		base64string = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGfJuH7SIX63boIuZBLM0Wa+SEbqhgy03sfgn1Z4ZwWF8kICV3giNHskWFAGzc8P/TD3RTf/6VdugF3h/2ozkVbtSVaDgAmXTTiDxZ8F14fKAcTcSwGJaqCtqwC/v7Z7PKyqqkuKfB0mrfbY9O01F57GCZpm1yTP3gfeBMeug9WQIDAQAB";
	}

	public void testImport() {
		PublicKey cert = new PublicKey();
		boolean loaded = cert.fromBase64(base64string);
		True(loaded, cert);
	}

	public void testExport() {
		PublicKey cert = new PublicKey();
		cert.load(path);
		String base64res = cert.toBase64();
		assertTrue(SecurityUtils.compareStrings(base64res, base64string));
		assertFalse(cert.hasError());
	}

	public void testWrongBase64() {
		PublicKey cert = new PublicKey();
		cert.fromBase64(base64Wrong);
		assertTrue(cert.hasError());
	}
}
