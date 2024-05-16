package com.genexus.test.jwt.asymmetric;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.GUID;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ECDSACurvesSectJwtTest extends SecurityAPITestObject{

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static GUID guid;
	protected static JWTCreator jwt;
	protected static String ECDSA_path;

	public static Test suite() {
		return new TestSuite(ECDSACurvesSectJwtTest.class);
	}

	@Override
	public void runTest() {
		test_shouldWork();
		test_shouldntWork();
	}

	@Override
	public void setUp() {
		guid = new GUID();
		jwt = new JWTCreator();
		options = new JWTOptions();
		claims = new PrivateClaims();

		options.addRegisteredClaim("iss", "GXSA");
		options.addRegisteredClaim("sub", "subject1");
		options.addRegisteredClaim("aud", "audience1");

		options.addRegisteredClaim("jti", guid.generate());

		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");

		ECDSA_path = resources.concat("/dummycerts/JWT_ECDSA/");
	}

	private void bulkTest_shouldWork(PrivateKeyManager key, CertificateX509 cert, String alg, String curve) {
		options.setPrivateKey(key);
		options.setCertificate(cert);
		String token = jwt.doCreate(alg, claims, options);
		assertFalse(jwt.hasError());
		boolean verification = jwt.doVerify(token, alg, claims, options);
		True(verification, jwt);
	}

	private void bulkTest_shouldntWork(PrivateKeyManager key, CertificateX509 cert, String alg, String curve) {
		options.setPrivateKey(key);
		options.setCertificate(cert);
		String token = jwt.doCreate(alg, claims, options);
		assertTrue(jwt.hasError());
	}

	public void test_shouldWork() {
		sect113r1();
		sect113r2();
		sect131r1();
		sect131r2();
		sect163k1();
		sect163r1();
		sect163r2();
		sect193r1();
		sect193r2();
		sect239k1();


	}

	public void test_shouldntWork() {
		/***ANDA CUANDO QUIERE***/
		//sect233k1();
		// sect233r1();
		sect283k1();
		sect283r1();
		sect409k1();
		sect409r1();
		sect571k1();
		sect571r1();
	}

	private void sect113r1() {
		String pathKey = ECDSA_path + "sect113r1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect113r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect113r1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect113r2() {
		String pathKey = ECDSA_path + "sect113r2" + "\\key.pem";
		String pathCert = ECDSA_path + "sect113r2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect113r2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect131r1() {
		String pathKey = ECDSA_path + "sect131r1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect131r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect131r1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect131r2() {
		String pathKey = ECDSA_path + "sect131r2" + "\\key.pem";
		String pathCert = ECDSA_path + "sect131r2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect131r2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect163k1() {
		String pathKey = ECDSA_path + "sect163k1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect163k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect163k1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect163r1() {
		String pathKey = ECDSA_path + "sect163r1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect163r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect163r1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect163r2() {
		String pathKey = ECDSA_path + "sect163r2" + "\\key.pem";
		String pathCert = ECDSA_path + "sect163r2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect163r2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect193r1() {
		String pathKey = ECDSA_path + "sect193r1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect193r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect193r1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect193r2() {
		String pathKey = ECDSA_path + "sect193r2" + "\\key.pem";
		String pathCert = ECDSA_path + "sect193r2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect193r2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect233k1() {
		String pathKey = ECDSA_path + "sect233k1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect233k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect233k1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

	private void sect233r1() {
		String pathKey = ECDSA_path + "sect233r1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect233r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect233r1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect239k1() {
		String pathKey = ECDSA_path + "sect239k1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect239k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect239k1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void sect283k1() {
		String pathKey = ECDSA_path + "sect283k1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect283k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect283k1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

	private void sect283r1() {
		String pathKey = ECDSA_path + "sect283r1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect283r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect283r1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

	private void sect409k1() {
		String pathKey = ECDSA_path + "sect409k1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect409k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect409k1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

	private void sect409r1() {
		String pathKey = ECDSA_path + "sect409r1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect409r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect409r1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

	private void sect571k1() {
		String pathKey = ECDSA_path + "sect571k1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect571k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect571k1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

	private void sect571r1() {
		String pathKey = ECDSA_path + "sect571r1" + "\\key.pem";
		String pathCert = ECDSA_path + "sect571r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "sect571r1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}
}
