package com.genexus.test.jwt.symmetric;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.test.jwt.resources.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class SymmetricJwtTest extends SecurityAPITestObject {

	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static SymmetricKeyGenerator keyGenerator;
	protected static JWTCreator jwt;

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

	}

	@Test
	public void test_HS256() {
		String hexaKey = keyGenerator.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey);
		String token = jwt.doCreate("HS256", claims, options);
		assertFalse(jwt.hasError());
		boolean verification = jwt.doVerify(token, "HS256", claims, options);
		True(verification, jwt);
	}

	@Test
	public void test_HS512() {
		String hexaKey = keyGenerator.doGenerateKey("GENERICRANDOM", 512);
		options.setSecret(hexaKey);
		String token = jwt.doCreate("HS512", claims, options);
		assertFalse(jwt.hasError());
		boolean verification = jwt.doVerify(token, "HS512", claims, options);
		True(verification, jwt);
	}
}
