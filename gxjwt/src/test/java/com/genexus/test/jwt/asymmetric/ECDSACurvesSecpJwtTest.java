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

public class ECDSACurvesSecpJwtTest extends SecurityAPITestObject {

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static GUID guid;
	protected static JWTCreator jwt;
	protected static String ECDSA_path;

	public static Test suite() {
		return new TestSuite(ECDSACurvesSecpJwtTest.class);
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
		secp112r1();
		secp112r2();
		secp128r1();
		secp128r2();
		secp160k1();
		secp160r1();
		secp192k1();
		secp256k1();

	}

	public void test_shouldntWork() {
		/***ESTOS DOS FUNCIONAN A VECES SI Y A VECES NO***/
		//secp224k1();
		//secp224r1();
		secp160r2();
		secp384r1();
		secp521r1();
	}

	private void secp112r1() {
		String pathKey = ECDSA_path + "secp112r1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp112r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp112r1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp112r2() {
		String pathKey = ECDSA_path + "secp112r2" + "\\key.pem";
		String pathCert = ECDSA_path + "secp112r2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp112r2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp128r1() {
		String pathKey = ECDSA_path + "secp128r1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp128r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp128r1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp128r2() {
		String pathKey = ECDSA_path + "secp128r2" + "\\key.pem";
		String pathCert = ECDSA_path + "secp128r2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp128r2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp160k1() {
		String pathKey = ECDSA_path + "secp160k1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp160k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp160k1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp160r1() {
		String pathKey = ECDSA_path + "secp160r1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp160r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp160r1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp160r2() {
		String pathKey = ECDSA_path + "secp160r2" + "\\key.pem";
		String pathCert = ECDSA_path + "secp160r2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp160r2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp192k1() {
		String pathKey = ECDSA_path + "secp192k1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp192k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp192k1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp224k1() {
		String pathKey = ECDSA_path + "secp224k1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp224k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp224k1";
		bulkTest_shouldntWork(key, cert, alg, curve);

	}

	private void secp224r1() {
		String pathKey = ECDSA_path + "secp224r1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp224r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp224r1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

	private void secp256k1() {
		String pathKey = ECDSA_path + "secp256k1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp256k1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp256k1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void secp384r1() {
		String pathKey = ECDSA_path + "secp384r1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp384r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp384r1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

	private void secp521r1() {
		String pathKey = ECDSA_path + "secp521r1" + "\\key.pem";
		String pathCert = ECDSA_path + "secp521r1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "secp521r1";
		bulkTest_shouldntWork(key, cert, alg, curve);
	}

}
