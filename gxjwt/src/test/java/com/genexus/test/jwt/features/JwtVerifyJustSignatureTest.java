package com.genexus.test.jwt.features;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.DateUtil;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JwtVerifyJustSignatureTest extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static SymmetricKeyGenerator keyGen;
	protected static DateUtil du;
	protected PrivateClaims claims;
	protected static String token;
	protected static String currentDate;
	protected static String hexaKey;

	public static Test suite() {
		return new TestSuite(JwtVerifyJustSignatureTest.class);
	}

	@Override
	public void runTest() {

		testPositive_JustSign();
		testComplete_JustSign();
		testNegative_JustSign();
		testPositive_Sign();
		testNegative_Sign();
	}

	@Override
	public void setUp() {
		jwt = new JWTCreator();
		options = new JWTOptions();
		du = new DateUtil();
		keyGen = new SymmetricKeyGenerator();
		claims = new PrivateClaims();

		currentDate = du.getCurrentDate();
		hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);

		options.addRegisteredClaim("aud", "jitsi");
		options.addRegisteredClaim("iss", "my_client");
		options.addRegisteredClaim("sub", "meet.jit.si");
		String expiration = du.currentPlusSeconds(200);
		options.addCustomTimeValidationClaim("exp", expiration, "20");

		claims.setClaim("hola", "hola");

		options.addHeaderParameter("cty", "twilio-fpa;v=1");
		options.setSecret(hexaKey);


		token = jwt.doCreate("HS256", claims, options);
	}

	public void testPositive_JustSign() {
		JWTOptions options1 = new JWTOptions();
		options1.setSecret(hexaKey);
		boolean verification = jwt.doVerifyJustSignature(token, "HS256", options1);
		True(verification, jwt);
	}

	public void testComplete_JustSign() {
		boolean verification = jwt.doVerifyJustSignature(token, "HS256", options);
		True(verification, jwt);
	}

	public void testNegative_JustSign() {
		JWTOptions options1 = new JWTOptions();
		String hexaKey1 = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options1.setSecret(hexaKey1);
		boolean verification = jwt.doVerifyJustSignature(token, "HS256", options1);
		assertFalse(verification);
		assertTrue(jwt.hasError());
	}

	public void testPositive_Sign() {
		options.setSecret(hexaKey);
		boolean verification = jwt.doVerifySignature(token, "HS256", options);
		True(verification, jwt);
	}

	public void testNegative_Sign() {
		String hexaKey1 = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey1);
		boolean verification = jwt.doVerifySignature(token, "HS256", options);
		assertFalse(verification);
		assertTrue(jwt.hasError());
	}

}
