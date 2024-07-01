package com.genexus.test.jwt.symmetric;

import java.security.SecureRandom;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.DateUtil;
import com.genexus.JWT.utils.GUID;
import com.genexus.JWT.utils.JWTAlgorithm;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.securityapicommons.commons.Error;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SymmetricJwtTest extends SecurityAPITestObject {

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static GUID guid;
	protected static DateUtil du;
	protected static SymmetricKeyGenerator keyGenerator;
	protected static JWTCreator jwt;

	public static Test suite() {
		return new TestSuite(SymmetricJwtTest.class);
	}

	@Override
	public void runTest() {
		test_HS256();
		test_HS512();
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

	}

	public void test_HS256() {
		String hexaKey = keyGenerator.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey);
		String token = jwt.doCreate("HS256", claims, options);
		assertFalse(jwt.hasError());
		boolean verification = jwt.doVerify(token, "HS256", claims, options);
		True(verification, jwt);
	}

	public void test_HS512() {
		String hexaKey = keyGenerator.doGenerateKey("GENERICRANDOM", 512);
		options.setSecret(hexaKey);
		String token = jwt.doCreate("HS512", claims, options);
		assertFalse(jwt.hasError());
		boolean verification = jwt.doVerify(token, "HS512", claims, options);
		True(verification, jwt);
	}


}
