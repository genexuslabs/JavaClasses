package com.genexus.gam.utils.test;

import com.genexus.gam.utils.keys.CertificateUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.cert.X509Certificate;

public class CertificateTest {

	private static String path_RSA_sha256_2048;
	private static String alias;
	private static String password;

	@BeforeClass
	public static void setUp() {
		String resources = System.getProperty("user.dir").concat("/src/test/resources");
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		alias = "1";
		password = "dummy";
	}

	@Test
	public void testLoadCrt() {
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.crt", "", "");
		Assert.assertNotNull("testLoadCrt", cert);
	}

	@Test
	public void testLoadCer() {
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.cer", "", "");
		Assert.assertNotNull("testLoadCer", cert);
	}

	@Test
	public void testLoadPfx() {
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.pfx", alias, password);
		Assert.assertNotNull("testLoadPfx", cert);
		cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.pfx", "", password);
		Assert.assertNotNull("testLoadPfx empty alias", cert);
	}

	@Test
	public void testLoadJks() {
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.jks", alias, password);
		Assert.assertNotNull("testLoadJks", cert);
		X509Certificate cert1 = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.jks", "", password);
		Assert.assertNotNull("testLoadJks empty alias", cert1);
	}

	@Test
	public void testLoadPkcs12() {
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.pkcs12", alias, password);
		Assert.assertNotNull("testLoadPkcs12", cert);
		cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.pkcs12", "", password);
		Assert.assertNotNull("testLoadPkcs12 empty alias", cert);
	}

	@Test
	public void testLoadP12() {
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password);
		Assert.assertNotNull("testLoadP12", cert);
		cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.p12", "", password);
		Assert.assertNotNull("testLoadP12 empty alias", cert);
	}

	@Test
	public void testLoadPem() {
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.pem", "", "");
		Assert.assertNotNull("testLoadPem", cert);
	}

	@Test
	public void testLoadKey() {
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.key", "", "");
		Assert.assertNotNull("testLoadKey", cert);
	}

	@Test
	public void testLoadBase64()
	{
		String base64 = "MIIEATCCAumgAwIBAgIJAIAqvKHZ+gFhMA0GCSqGSIb3DQEBCwUAMIGWMQswCQYDVQQGEwJVWTETMBEGA1UECAwKTW9udGV2aWRlbzETMBEGA1UEBwwKTW9udGV2aWRlbzEQMA4GA1UECgwHR2VuZVh1czERMA8GA1UECwwIU2VjdXJpdHkxEjAQBgNVBAMMCXNncmFtcG9uZTEkMCIGCSqGSIb3DQEJARYVc2dyYW1wb25lQGdlbmV4dXMuY29tMB4XDTIwMDcwODE4NTcxN1oXDTI1MDcwNzE4NTcxN1owgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC1zgaU+Wh63p9DNWoAy64252EvZjN49AY3x0QCnAa8JO9Pk7znQwrxEFUKgZzv0GHEYW7+X+uyJr7BW4TA6fuJJ8agE/bmZRZyjdJjoue0FML6fbmCZ9Tsxpxe4pzispyWQ8jYT4Kl4I3fdZNUSn4XSidnDKBISeC05mrcchDKhInpiYDJ481lsB4JTEti3S4Xy/ToKwY4t6attw6z5QDhBc+Yro+YUqruliOAKqcfybe9k07jwMCvFVM1hrYYJ7hwHDSFo3MKwZ0y2gw0w6SgVBxLFo+KYP3q63b5wVhD8lzaSh+8UcyiHM2/yjEej7EnRFzdclTSNXRFNaiLnEVdAgMBAAGjUDBOMB0GA1UdDgQWBBQtQAWJRWNr/OswPSAdwCQh0Eei/DAfBgNVHSMEGDAWgBQtQAWJRWNr/OswPSAdwCQh0Eei/DAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQCjHe3JbNKv0Ywc1zlLacUNWcjLbmzvnjs8Wq5oxtf5wG5PUlhLSYZ9MPhuf95PlibnrO/xVY292P5lo4NKhS7VOonpbPQ/PrCMO84Pz1LGfM/wCWQIowh6VHq18PiZka9zbwl6So0tgClKkFSRk4wpKrWX3+M3+Y+D0brd8sEtA6dXeYHDtqV0YgjKdZIIOx0vDT4alCoVQrQ1yAIq5INT3cSLgJezIhEadDv3Tc7bMxMFeL+81qHm9Z/9/KE6Z+JB0ZEOkF/2NSQJd+Z7MBR8CxOdTQis3ltMoXDatNkjZ2Env40sw4NICB8YYhsWMIarew5uNT+RS28YHNlbmogh";
		X509Certificate cert = CertificateUtil.getCertificate(base64, "", "");
		Assert.assertNotNull("testLoadBase64", cert);
	}
}
