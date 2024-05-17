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

public class ECDSACurvesPrimeJwtTest extends SecurityAPITestObject {

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static GUID guid;
	protected static JWTCreator jwt;
	protected static String ECDSA_path;

	public static Test suite() {
		return new TestSuite(ECDSACurvesPrimeJwtTest.class);
	}

	@Override
	public void runTest() {

		test_shouldWork();
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

	public void test_shouldWork() {

		//prime192v1();
		prime192v2();
		prime192v3();
		prime239v1();
		prime239v2();
		prime239v3();
		prime256v1();
	}

	/*private void prime192v1() {
		String pathKey = ECDSA_path + "prime192v1" + "\\key.pem";
		String pathCert = ECDSA_path + "prime192v1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "prime192v1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}*/

	private void prime192v2() {
		String pathKey = ECDSA_path + "prime192v2" + "\\key.pem";
		String pathCert = ECDSA_path + "prime192v2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "prime192v2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void prime192v3() {
		String pathKey = ECDSA_path + "prime192v3" + "\\key.pem";
		String pathCert = ECDSA_path + "prime192v3" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "prime192v3";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void prime239v1() {
		String pathKey = ECDSA_path + "prime239v1" + "\\key.pem";
		String pathCert = ECDSA_path + "prime239v1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "prime239v1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void prime239v2() {
		String pathKey = ECDSA_path + "prime239v2" + "\\key.pem";
		String pathCert = ECDSA_path + "prime239v2" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "prime239v2";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void prime239v3() {
		String pathKey = ECDSA_path + "prime239v3" + "\\key.pem";
		String pathCert = ECDSA_path + "prime239v3" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "prime239v3";
		bulkTest_shouldWork(key, cert, alg, curve);
	}

	private void prime256v1() {
		String pathKey = ECDSA_path + "prime256v1" + "\\key.pem";
		String pathCert = ECDSA_path + "prime256v1" + "\\cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		String curve = "prime256v1";
		bulkTest_shouldWork(key, cert, alg, curve);
	}
}
