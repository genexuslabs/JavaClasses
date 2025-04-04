package com.genexus.test.jwt.asymmetric;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.test.jwt.resources.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class PublicKeyAsymmetricJwtTest extends SecurityAPITestObject {

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static SymmetricKeyGenerator keyGenerator;
	protected static JWTCreator jwt;

	protected static String path_RSA_sha256_1024;
	protected static String path_RSA_sha256_2048;
	protected static String path_RSA_sha512_2048;
	protected static String path_EC;

	protected static String alias;
	protected static String password;

	@Test
	public void test_sha256_1024_PublicKey() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		PublicKey cert = new PublicKey();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}

	@Test
	public void test_sha256_2048_PublicKey() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		PublicKey cert = new PublicKey();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS256";
		bulkTest(key, cert, alg);
	}

	@Test
	public void test_sha512_2048_PublicKey() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		PublicKey cert = new PublicKey();
		key.load(pathKey);
		cert.load(pathCert);
		String alg = "RS512";
		bulkTest(key, cert, alg);
	}


	@BeforeClass
	public static void setUp() {

		keyGenerator = new SymmetricKeyGenerator();
		jwt = new JWTCreator();
		options = new JWTOptions();
		claims = new PrivateClaims();

		options.addRegisteredClaim("iss", "GXSA");
		options.addRegisteredClaim("sub", "subject1");
		options.addRegisteredClaim("aud", "audience1");

		options.addRegisteredClaim("jti", TestUtils.generateGUID());

		options.addCustomTimeValidationClaim("exp", TestUtils.getCurrentDate(), "20");
		options.addCustomTimeValidationClaim("iat", TestUtils.getCurrentDate(), "20");
		options.addCustomTimeValidationClaim("nbf", TestUtils.getCurrentDate(), "20");

		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");

		path_RSA_sha256_1024 = resources.concat("/dummycerts/RSA_sha256_1024/");
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		path_RSA_sha512_2048 = resources.concat("/dummycerts/RSA_sha512_2048/");
		path_EC = resources.concat("/dummycerts/JWT_ECDSA/prime_test/");


		alias = "1";
		password = "dummy";

	}

	private void bulkTest(PrivateKeyManager key, PublicKey cert, String alg) {
		options.setPrivateKey(key);
		options.setPublicKey(cert);
		String token = jwt.doCreate(alg, claims, options);
		assertFalse(jwt.hasError());

		boolean verification = jwt.doVerify(token, alg, claims, options);
		True(verification, jwt);
	}
}
