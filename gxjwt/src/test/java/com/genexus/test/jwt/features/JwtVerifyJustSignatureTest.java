package com.genexus.test.jwt.features;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.test.jwt.resources.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class JwtVerifyJustSignatureTest extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static SymmetricKeyGenerator keyGen;
	protected static PrivateClaims claims;
	protected static String token;
	protected static String currentDate;
	protected static String hexaKey;


	@BeforeClass
	public static void setUp() {
		jwt = new JWTCreator();
		options = new JWTOptions();
		keyGen = new SymmetricKeyGenerator();
		claims = new PrivateClaims();

		currentDate = TestUtils.getCurrentDate();
		hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);

		options.addRegisteredClaim("aud", "jitsi");
		options.addRegisteredClaim("iss", "my_client");
		options.addRegisteredClaim("sub", "meet.jit.si");
		String expiration = TestUtils.currentPlusSeconds(200);
		options.addCustomTimeValidationClaim("exp", expiration, "20");

		claims.setClaim("hola", "hola");

		options.addHeaderParameter("cty", "twilio-fpa;v=1");
		options.setSecret(hexaKey);


		token = jwt.doCreate("HS256", claims, options);
	}

	@Test
	public void testPositive_JustSign() {
		JWTOptions options1 = new JWTOptions();
		options1.setSecret(hexaKey);
		boolean verification = jwt.doVerifyJustSignature(token, "HS256", options1);
		True(verification, jwt);
	}

	@Test
	public void testComplete_JustSign() {
		boolean verification = jwt.doVerifyJustSignature(token, "HS256", options);
		True(verification, jwt);
	}

	@Test
	public void testNegative_JustSign() {
		JWTOptions options1 = new JWTOptions();
		String hexaKey1 = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options1.setSecret(hexaKey1);
		boolean verification = jwt.doVerifyJustSignature(token, "HS256", options1);
		assertFalse(verification);
		assertTrue(jwt.hasError());
	}

	@Test
	public void testPositive_Sign() {
		options.setSecret(hexaKey);
		boolean verification = jwt.doVerifySignature(token, "HS256", options);
		True(verification, jwt);
	}

	@Test
	public void testNegative_Sign() {
		String hexaKey1 = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey1);
		boolean verification = jwt.doVerifySignature(token, "HS256", options);
		assertFalse(verification);
		assertTrue(jwt.hasError());
	}

}
