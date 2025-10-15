package com.genexus.xmlsignature.test;

import com.genexus.commons.DSigOptions;
import com.genexus.dsig.XmlDSigSigner;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class Base64CertificateTest extends SecurityAPITestObject {
	private static String base64 = "MIIDCjCCAnOgAwIBAgIUW1tCN/KqaNQX0DvdDb3WFDCxEuIwDQYJKoZIhvcNAQEFBQAwgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wHhcNMjUwNzA4MTQzNzQ5WhcNMzAwNzA3MTQzNzQ5WjCBljELMAkGA1UEBhMCVVkxEzARBgNVBAgMCk1vbnRldmlkZW8xEzARBgNVBAcMCk1vbnRldmlkZW8xEDAOBgNVBAoMB0dlbmVYdXMxETAPBgNVBAsMCFNlY3VyaXR5MRIwEAYDVQQDDAlzZ3JhbXBvbmUxJDAiBgkqhkiG9w0BCQEWFXNncmFtcG9uZUBnZW5leHVzLmNvbTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA6quMq4knDBYCUhyuD7DobnLm0q6IrHiSr8rOlJ73kTurS0QZRILZFn7O3DoBGFOA7fIYpeSoWLkbkU4AZgRU3t+BtmlJqfWi21FuZa7P86Lova2JqyhzJgk5GLjoPie49WBTmecZXUytwaTlR1d5/Euht/3r4xb3/lpRtMqTEHUCAwEAAaNTMFEwHQYDVR0OBBYEFEZdLshvqnzXpVD17GoS8yVosNVSMB8GA1UdIwQYMBaAFEZdLshvqnzXpVD17GoS8yVosNVSMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEFBQADgYEAPptVObAvKso9m+QLxddNOrqGZTonRe0SaQo8BO/v3GzX8BL6zptEqNAe5Rxme5TwY8ZlyYsb0f3YKO0czR5YQQDz3EdYXdOV2YuS/o02n9kGv677ITMS4T0ka+QzHrMdUql/IuzFwFr7lYeNEh44afyJ2HQjyea0JbULLuWRUSY=";

	private static String path_RSA_sha1_1024;
	private static String xmlUnsignedPath;
	private static String pathSigned;
	private static DSigOptions options;
	private static String pathKey;
	private static String xmlSignedPathRoot;

	@BeforeClass
	public static void setUp() {
		path_RSA_sha1_1024 = resources.concat("/dummycerts/RSA_sha1_1024/");

		options = new DSigOptions();

		xmlUnsignedPath = resources.concat("/tosign.xml");
		pathSigned = tempFolder + "base64.xml";
		pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		xmlSignedPathRoot = tempFolder.toString();

	}

	@Test
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

	@Test
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
