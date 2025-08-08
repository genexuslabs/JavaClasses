package com.genexus.test.keys;

import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class LoadPKCS12First extends SecurityAPITestObject {

	private static String certPAth;
	private static String password;
	private static CertificateX509 cert;

	private static PrivateKeyManager key;

	@BeforeClass
	public static void setUp() {
		certPAth = resources.concat("/dummycerts/RSA_sha256_1024/sha256_cert.p12"); //"C:\\Temp\\dummycerts\\RSA_sha256_1024\\sha256_cert.p12";
		password = "dummy";
		cert = new CertificateX509();
		key = new PrivateKeyManager();
	}

	@Test
	public void testLoadCert() {
		boolean loaded = cert.loadPKCS12(certPAth, "", password);
		True(loaded, cert);
	}

	@Test
	public void testLoadKey() {
		boolean loaded = key.loadPKCS12(certPAth, "", password);
		True(loaded, key);
	}
}
