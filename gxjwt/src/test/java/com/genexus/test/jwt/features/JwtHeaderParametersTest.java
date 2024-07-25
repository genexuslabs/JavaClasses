package com.genexus.test.jwt.features;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.DateUtil;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JwtHeaderParametersTest extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static SymmetricKeyGenerator keyGen;
	protected static DateUtil du;
	protected PrivateClaims claims;
	protected static String token;
	protected static String currentDate;
	protected static String hexaKey;

	public static Test suite() {
		return new TestSuite(JwtHeaderParametersTest.class);
	}

	@Override
	public void runTest() {
		testPositive();
		testNegative1();
		testNegative2();
		testNegative3();
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
		options.addCustomTimeValidationClaim("exp", currentDate, "20");

		claims.setClaim("hola", "hola");

		options.addHeaderParameter("cty", "twilio-fpa;v=1");
		options.setSecret(hexaKey);

		token = jwt.doCreate("HS256", claims, options);
	}

	public void testPositive()
	{
		boolean verification = jwt.doVerify(token, "HS256", claims, options);
		True(verification, jwt);
	}

	public void testNegative1()
	{
		options.addHeaderParameter("pepe", "whatever");
		boolean verification = jwt.doVerify(token, "HS256", claims, options);
		assertFalse(verification);
		assertFalse(jwt.hasError());
	}

	public void testNegative2()
	{
		JWTOptions op = new JWTOptions();
		op.addRegisteredClaim("aud", "jitsi");
		op.addRegisteredClaim("iss", "my_client");
		op.addRegisteredClaim("sub", "meet.jit.si");
		op.addCustomTimeValidationClaim("exp", currentDate, "20");
		op.setSecret(hexaKey);
		op.addHeaderParameter("pepe", "whatever");

		boolean verification = jwt.doVerify(token, "HS256", claims, op);
		assertFalse(verification);
		assertFalse(jwt.hasError());

	}

	public void testNegative3()
	{
		JWTOptions op = new JWTOptions();
		op.addRegisteredClaim("aud", "jitsi");
		op.addRegisteredClaim("iss", "my_client");
		op.addRegisteredClaim("sub", "meet.jit.si");
		op.addCustomTimeValidationClaim("exp", currentDate, "20");
		op.setSecret(hexaKey);


		boolean verification = jwt.doVerify(token, "HS256", claims, op);
		assertFalse(verification);
		assertFalse(jwt.hasError());
	}
}
