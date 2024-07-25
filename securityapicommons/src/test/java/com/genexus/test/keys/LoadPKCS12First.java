package com.genexus.test.keys;

import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;
import junit.framework.Test;
import junit.framework.TestSuite;

public class LoadPKCS12First extends SecurityAPITestObject {

	private static String certPAth;
	private static String password;
	private static CertificateX509 cert;

	private static PrivateKeyManager key;

	public static Test suite() {
		return new TestSuite(LoadPKCS12First.class);
	}

	@Override
	public void runTest() {
		testLoadCert();
		testLoadKey();
	}

	@Override
	public void setUp() {
		certPAth = resources.concat("/dummycerts/RSA_sha256_1024/sha256_cert.p12"); //"C:\\Temp\\dummycerts\\RSA_sha256_1024\\sha256_cert.p12";
		password = "dummy";
		cert = new CertificateX509();
		key = new PrivateKeyManager();
	}

	public void testLoadCert()
	{
		boolean loaded =  cert.loadPKCS12(certPAth, "", password);
		True(loaded, cert );
	}

	public void testLoadKey()
	{
		boolean loaded =  key.loadPKCS12(certPAth, "", password);
		True(loaded, key );
	}
}
