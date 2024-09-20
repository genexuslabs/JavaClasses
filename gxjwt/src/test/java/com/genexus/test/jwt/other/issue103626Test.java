package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("NewClassNamingConvention")
public class issue103626Test extends SecurityAPITestObject {

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static JWTCreator jwt;

	@BeforeClass
	public static void setUp() {

		jwt = new JWTCreator();
		options = new JWTOptions();
		claims = new PrivateClaims();

		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");

	}

	@Test
	public void test_Symmetric() {
		jwt.doCreate("HS256", claims, options);
		assertTrue(jwt.hasError());
	}

	@Test
	public void test_Asymmetric() {
		jwt.doCreate("RS256", claims, options);
		assertTrue(jwt.hasError());
	}
}
