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
		base64string = "MIIEDzCCAvegAwIBAgIUdbZlqwWgv2JQlHp4SIAfMesMCKswDQYJKoZIhvcNAQELBQAwgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wHhcNMjUwNzA4MTUwODI3WhcNMzAwNzA3MTUwODI3WjCBljELMAkGA1UEBhMCVVkxEzARBgNVBAgMCk1vbnRldmlkZW8xEzARBgNVBAcMCk1vbnRldmlkZW8xEDAOBgNVBAoMB0dlbmVYdXMxETAPBgNVBAsMCFNlY3VyaXR5MRIwEAYDVQQDDAlzZ3JhbXBvbmUxJDAiBgkqhkiG9w0BCQEWFXNncmFtcG9uZUBnZW5leHVzLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALnQswXumxsNCyABl2u/+rRMZYGpN4OY9DXppahuffN/byQ5v092DYZ7X4bnpWI7xoP94NRoaLsIu0hzr1+5mMVnRbqyeU28opr/p3Aq0YX89utvbpumHDF7d5VPVSJ+wAf9i/6Ck5Pd/QdCBfnbKjJEFAM/HBKbhG2k0FtonkHSx/mxN0TTGL3dBuVcpO4p57fWqKqKiV3MtKo79OxbhigLjy5I3YxNhObE0n7ByKIDt5O4qvJ1DvRMTviLfG4D/U3f7VvmucAnxyQJc6uuodL4XsG5iPXdjgFX9wLIDdKPAvpuM7bEbDvdqGHpmmG1JfYlhBiZgNLKno0YyisY1DkCAwEAAaNTMFEwHQYDVR0OBBYEFFYk4TOHdhQ6Yk3sKcrouG7XgLBNMB8GA1UdIwQYMBaAFFYk4TOHdhQ6Yk3sKcrouG7XgLBNMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH/DbEa70n+GAaqFViAoFuzcpxNHvjl8oH4LTeGK/dVVOYn58B79psf0EcsJ9LxrNgOCl9ZRuxLnat1/MGptCJjQeP5US8RtiTTpNnm5GbKdu6fq17ub9RjETlsX+OjDQuyk4+1B5DKo2bbqveWG+O7M2a7/7CVZ3wMu+Cg0FaUJAHBAOP68K7is21Fdy4CSxdvqOahKiComKQOBHfIjJsogUU/xR4Izw+HJV6oTbt39CXGhrKIli4CNxzAsJtDbkplwAN/wCooR8PEM6kbLTzJCY8JpS404Z8NG2tbKyKtc6Mz6iZ8oM1AVdzjoxyPx0YtehyNB39bMqIWO5U73yQ0=";
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
