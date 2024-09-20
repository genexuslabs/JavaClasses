package com.genexus.test.keys;

import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class Base64CertificateTest extends SecurityAPITestObject {

	protected static String path;
	protected static String base64string;
	protected static String base64Wrong;

	@BeforeClass
	public static void setUp() {
		path = resources.concat("/sha256_cert.pem");
		System.out.println(path);
		//path = "C:\\Temp\\dummycerts\\RSA_sha256_1024\\sha256_cert.pem";
		base64Wrong = "--BEGINKEY--sdssf--ENDKEY—";
		base64string = "MIIC/DCCAmWgAwIBAgIJAPmCVmfcc0IXMA0GCSqGSIb3DQEBCwUAMIGWMQswCQYDVQQGEwJVWTETMBEGA1UECAwKTW9udGV2aWRlbzETMBEGA1UEBwwKTW9udGV2aWRlbzEQMA4GA1UECgwHR2VuZVh1czERMA8GA1UECwwIU2VjdXJpdHkxEjAQBgNVBAMMCXNncmFtcG9uZTEkMCIGCSqGSIb3DQEJARYVc2dyYW1wb25lQGdlbmV4dXMuY29tMB4XDTIwMDcwODE4NDkxNVoXDTI1MDcwNzE4NDkxNVowgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMZ8m4ftIhfrdugi5kEszRZr5IRuqGDLTex+CfVnhnBYXyQgJXeCI0eyRYUAbNzw/9MPdFN//pV26AXeH/ajORVu1JVoOACZdNOIPFnwXXh8oBxNxLAYlqoK2rAL+/tns8rKqqS4p8HSat9tj07TUXnsYJmmbXJM/eB94Ex66D1ZAgMBAAGjUDBOMB0GA1UdDgQWBBTfXY8eOfDONCZpFE0V34mJJeCYtTAfBgNVHSMEGDAWgBTfXY8eOfDONCZpFE0V34mJJeCYtTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4GBAAPv7AFlCSpJ32c/VYowlbk6UBhOKmVWBQlrAtvVQYtCKO/y9CEB8ikG19c8lHM9axnsbZR+G7g04Rfuiea3T7VPkSmUXPpz5fl6Zyk4LZg5Oji7MMMXGmr+7cpYWRhifCVwoxSgZEXt3d962IZ1Wei0LMO+4w4gnzPxqr8wVHnT";
	}

	@Test
	public void testImport() {
		CertificateX509 cert = new CertificateX509();
		boolean loaded = cert.fromBase64(base64string);
		True(loaded, cert);
	}

	@Test
	public void testExport() {
		CertificateX509 cert = new CertificateX509();
		cert.load(path);
		String base64res = cert.toBase64();
		assertTrue(SecurityUtils.compareStrings(base64res, base64string));
		assertFalse(cert.hasError());
	}

	@Test
	public void testWrongBase64() {
		CertificateX509 cert = new CertificateX509();
		cert.fromBase64(base64Wrong);
		assertTrue(cert.hasError());
	}

}
