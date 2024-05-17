package com.genexus.test.jwt.asymmetric;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CreateWithoutCertTest extends SecurityAPITestObject{

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static JWTCreator jwt;

	protected static String path_RSA_sha256_1024;

	public static Test suite() {
		return new TestSuite(CreateWithoutCertTest.class);
	}

	@Override
	public void runTest() {
		test_sha256_1024_PEM();
	}

	@Override
	public void setUp() {

		jwt = new JWTCreator();
		options = new JWTOptions();
		claims = new PrivateClaims();

		options.addRegisteredClaim("iss", "GXSA");
		options.addRegisteredClaim("sub", "subject1");
		options.addRegisteredClaim("aud", "audience1");

		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");

		path_RSA_sha256_1024 = resources.concat("/dummycerts/RSA_sha256_1024/");
	}

	public void test_sha256_1024_PEM() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		options.setPrivateKey(key);
		String alg = "RS256";
		String token = jwt.doCreate(alg, claims, options);
		assertFalse(SecurityUtils.compareStrings("", token));
		assertFalse(jwt.hasError());
	}
}
