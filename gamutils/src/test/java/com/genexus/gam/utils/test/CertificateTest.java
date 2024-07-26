package com.genexus.gam.utils.test;

import com.genexus.gam.utils.CertificateUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.cert.X509Certificate;

public class CertificateTest {

	private static String resources;
	private static String path_RSA_sha256_2048;
	private static String alias;
	private static String password;

	@BeforeClass
	public static void setUp() {
		resources = System.getProperty("user.dir").concat("/src/test/resources");
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
		X509Certificate cert = CertificateUtil.getCertificate(path_RSA_sha256_2048 + "sha256_cert.Key", "", "");
		Assert.assertNotNull("testLoadKey", cert);
	}
}
