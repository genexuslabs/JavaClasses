package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Issue84142Test extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static SymmetricKeyGenerator keyGen;

	public static Test suite() {
		return new TestSuite(Issue84142Test.class);
	}

	@Override
	public void runTest() {

	}

	@Override
	public void setUp() {
		options = new JWTOptions();
		jwt = new JWTCreator();
		claims = new PrivateClaims();
		keyGen = new SymmetricKeyGenerator();

		String hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey);

		claims.setClaim("hola1", "hola1");

	}

	public void test_expValidationPositive() {
		options.addCustomTimeValidationClaim("exp", "2030/07/07 10:15:20", "20");
		String token = jwt.doCreate("HS256", claims, options);
		boolean validation = jwt.doVerify(token, "HS256", claims, options);
		assertTrue(validation);
	}

	public void test_expValidationNegative() {
		options.addCustomTimeValidationClaim("exp", "2019/07/07 10:15:20", "20");
		String token = jwt.doCreate("HS256", claims, options);
		boolean validation = jwt.doVerify(token, "HS256", claims, options);
		assertFalse(validation);
	}
}
