package com.genexus.xmlsignature.test;

import com.genexus.commons.DSigOptions;
import com.genexus.dsig.XmlDSigSigner;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Base64CertificateTest extends SecurityAPITestObject {
	private String base64 = "MIIC/DCCAmWgAwIBAgIJAIh1DtAn5T0IMA0GCSqGSIb3DQEBBQUAMIGWMQswCQYDVQQGEwJVWTETMBEGA1UECAwKTW9udGV2aWRlbzETMBEGA1UEBwwKTW9udGV2aWRlbzEQMA4GA1UECgwHR2VuZVh1czERMA8GA1UECwwIU2VjdXJpdHkxEjAQBgNVBAMMCXNncmFtcG9uZTEkMCIGCSqGSIb3DQEJARYVc2dyYW1wb25lQGdlbmV4dXMuY29tMB4XDTIwMDcwODE4NDM1N1oXDTI1MDcwNzE4NDM1N1owgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKvo5gGDQ2w0veZSDxd+nJc7w7z/Is+4iGhOEuK9A/U713RfBdXYx2prp+7BAkUrGYm+Z6SkXZ6r78Tl5D/L2pNeA6nn5geCoWH1KSFOlAvEnjXcGvkdo8bIE/Day3PWFdeIGD8Mt0badAoIM+0m6s5jfSu9N8o4I4UX9O4PoEwhAgMBAAGjUDBOMB0GA1UdDgQWBBSLvqEYCzyExQe0fuRFBXpHjVbb6TAfBgNVHSMEGDAWgBSLvqEYCzyExQe0fuRFBXpHjVbb6TAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBQUAA4GBAArYRju3NQeCspTxvpixMLLPWaYzxRmtUkEz1yr7VhlIH63RTIqbRcbP+40DRxx83LkIOJRdOcCVeLX3ZutknJglfrqFkUF5grWrhrHpd+IRSeN3lePMYa3GeeljTyrPINCwnv0YFLQOwRf8UlZcKAquJO2ouQZkVd9t1tRWTvNo";

	private String path_RSA_sha1_1024;
	private String xmlUnsignedPath;
	private String pathSigned;
	private DSigOptions options;
	private String pathKey;
	private String xmlSignedPathRoot;

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

		xmlUnsignedPath =  resources.concat("/tosign.xml");
		pathSigned = tempFolder + "base64.xml";
		pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		xmlSignedPathRoot = tempFolder.toString();

	}

	public void testSignBase64() {
		CertificateX509 newCert = new CertificateX509();
		boolean loaded = newCert.fromBase64(base64);
		assertTrue(loaded);
		True(loaded, newCert);
		PrivateKeyManager key = new PrivateKeyManager();
		boolean privateLoaded = key.load(pathKey);
		assertTrue(privateLoaded);
		True(privateLoaded, key);
		XmlDSigSigner signer = new XmlDSigSigner();
		boolean result = signer.doSignFile(xmlUnsignedPath, key, newCert, xmlSignedPathRoot + pathSigned, options);
		assertTrue(result);
		True(result, signer);
		boolean verify = signer.doVerifyFile(xmlSignedPathRoot + pathSigned, options);
		assertTrue(verify);
		True(verify, signer);
	}

	public void testToBase64() {
		CertificateX509 newCert = new CertificateX509();
		boolean loaded = newCert.fromBase64(base64);
		assertTrue(loaded);
		True(loaded, newCert);
		String newBase64 = newCert.toBase64();
		boolean result = SecurityUtils.compareStrings(newBase64, base64);
		assertTrue(result);
		True(result, newCert);
	}
}
