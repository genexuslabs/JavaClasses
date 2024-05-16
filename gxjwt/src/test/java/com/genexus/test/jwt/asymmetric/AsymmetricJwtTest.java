package com.genexus.test.jwt.asymmetric;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.DateUtil;
import com.genexus.JWT.utils.GUID;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AsymmetricJwtTest extends SecurityAPITestObject {

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static GUID guid;
	protected static DateUtil du;
	protected static SymmetricKeyGenerator keyGenerator;
	protected static JWTCreator jwt;

	protected static String path_RSA_sha256_1024;
	protected static String path_RSA_sha256_2048;
	protected static String path_RSA_sha512_2048;
	protected static String path_EC;

	protected static String alias;
	protected static String password;


	public static Test suite() {
		return new TestSuite(AsymmetricJwtTest.class);
	}

	@Override
	public void runTest() {
		test_sha256_1024_DER();
		test_sha256_1024_PEM();
		test_sha256_1024_PEM_Encrypted();
		test_sha256_1024_PKCS12();

		test_sha256_2048_DER();
		test_sha256_2048_PEM();
		test_sha256_2048_PEM_Encrypted();
		test_sha256_2048_PKCS12();

		test_sha512_2048_DER();
		test_sha256_2048_PEM();
		test_sha256_2048_PEM_Encrypted();
		test_sha512_2048_PKCS12();

		test_sha256_EC();
		test_sha384_EC();
		test_sha512_EC();

	}


	@Override
	public void setUp() {

		du = new DateUtil();
		guid = new GUID();
		keyGenerator = new SymmetricKeyGenerator();
		jwt = new JWTCreator();
		options = new JWTOptions();
		claims = new PrivateClaims();

		options.addRegisteredClaim("iss", "GXSA");
		options.addRegisteredClaim("sub", "subject1");
		options.addRegisteredClaim("aud", "audience1");

		options.addRegisteredClaim("jti", guid.generate());

		options.addCustomTimeValidationClaim("exp", du.getCurrentDate(), "20");
		options.addCustomTimeValidationClaim("iat", du.getCurrentDate(), "20");
		options.addCustomTimeValidationClaim("nbf", du.getCurrentDate(), "20");

		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");

		path_RSA_sha256_1024 = resources.concat("/dummycerts/RSA_sha256_1024/");
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		path_RSA_sha512_2048 = resources.concat("/dummycerts/RSA_sha512_2048/");
		path_EC = resources.concat("/dummycerts/JWT_ECDSA/prime_test/");

		alias = "1";
		password = "dummy";

	}

	private void bulkTest(PrivateKeyManager key, CertificateX509 cert, String alg) {
		options.setPrivateKey(key);
		options.setCertificate(cert);
		String token = jwt.doCreate(alg, claims, options);
		assertFalse(jwt.hasError());

		boolean verification = jwt.doVerify(token, alg, claims, options);
		True(verification, jwt);
	}

	public void test_sha256_1024_DER() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}

	public void test_sha256_1024_PEM() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}



	public void test_sha256_1024_PKCS12() {
		String pathKey = path_RSA_sha256_1024 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.loadPKCS12(pathKey, alias, password);
		cert.loadPKCS12(pathCert, alias, password);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}

	public void test_sha256_2048_DER() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}

	public void test_sha256_2048_PEM() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}



	public void test_sha256_2048_PKCS12() {
		String pathKey = path_RSA_sha256_2048 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.loadPKCS12(pathKey, alias, password);
		cert.loadPKCS12(pathCert, alias, password);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}

	public void test_sha512_2048_DER() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS512";
		bulkTest(key, cert, alg);
	}

	public void test_sha512_2048_PEM() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS512";
		bulkTest(key, cert, alg);
	}



	public void test_sha512_2048_PKCS12() {
		String pathKey = path_RSA_sha512_2048 + "sha512_cert.p12";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.loadPKCS12(pathKey, alias, password);
		cert.loadPKCS12(pathCert, alias, password);
		String alg = "RS512";
		bulkTest(key, cert, alg);
	}

	public void test_sha256_EC() {
		String pathKey = path_EC + "key.pem";
		String pathCert = path_EC + "cert_sha256.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES256";
		bulkTest(key, cert, alg);
	}

	public void test_sha384_EC() {
		String pathKey = path_EC + "key.pem";
		String pathCert = path_EC + "cert_sha384.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES384";
		bulkTest(key, cert, alg);
	}

	public void test_sha512_EC() {
		String pathKey = path_EC + "key.pem";
		String pathCert = path_EC + "cert_sha512.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "ES512";
		bulkTest(key, cert, alg);
	}

	public void test_sha256_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_1024 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.loadEncrypted(pathKey, password);
		cert.load(pathCert);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}

	public void test_sha256_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_2048 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.loadEncrypted(pathKey, password);
		cert.load(pathCert);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}

	public void test_sha512_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha512_2048 + "sha512_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.loadEncrypted(pathKey, password);
		cert.load(pathCert);
		String alg = "RS512";
		bulkTest(key, cert, alg);
	}
}
