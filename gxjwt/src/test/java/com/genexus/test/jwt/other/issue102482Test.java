package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import junit.framework.Test;
import junit.framework.TestSuite;

public class issue102482Test extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static SymmetricKeyGenerator keyGen;
	protected static String token;

	public static Test suite() {
		return new TestSuite(issue102482Test.class);
	}

	@Override
	public void runTest() {
		test_AddRegisteredClaimsDate();
	}

	@Override
	public void setUp() {
		options = new JWTOptions();
		jwt = new JWTCreator();
		claims = new PrivateClaims();
		keyGen = new SymmetricKeyGenerator();

		String secret = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(secret);
		options.addRegisteredClaim("iss", "GXSA");
		options.addRegisteredClaim("iat", "1681928114");
		options.addRegisteredClaim("exp", "1681928114");
		options.addRegisteredClaim("nbf", "1681928114");

		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");

	}

	public static void test_AddRegisteredClaimsDate()
	{
		token = jwt.doCreate("HS256", claims, options);
		assertFalse( SecurityUtils.compareStrings("", token));
	}

}
