package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.DateUtil;
import com.genexus.JWT.utils.GUID;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.test.jwt.symmetric.SymmetricJwtTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class issue103626Test extends SecurityAPITestObject {

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static JWTCreator jwt;

	public static Test suite() {
		return new TestSuite(issue103626Test.class);
	}

	@Override
	public void runTest() {
		test_Symmetric();
		test_Asymmetric();
	}

	@Override
	public void setUp() {

		jwt = new JWTCreator();
		options = new JWTOptions();
		claims = new PrivateClaims();

		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");

	}

	public void test_Symmetric() {
		String token = jwt.doCreate("HS256", claims, options);
		assertTrue(jwt.hasError());
	}

	public void test_Asymmetric() {
		String token = jwt.doCreate("RS256", claims, options);
		assertTrue(jwt.hasError());
	}
}
