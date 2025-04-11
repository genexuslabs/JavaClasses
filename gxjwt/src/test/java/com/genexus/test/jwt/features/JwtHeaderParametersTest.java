package com.genexus.test.jwt.features;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.test.jwt.resources.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class JwtHeaderParametersTest extends SecurityAPITestObject {

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
		options.addCustomTimeValidationClaim("exp", currentDate, "20");

		claims.setClaim("hola", "hola");

		options.addHeaderParameter("cty", "twilio-fpa;v=1");
		options.setSecret(hexaKey);

		token = jwt.doCreate("HS256", claims, options);
	}

	@Test
	public void testPositive() {
		JWTOptions opt = new JWTOptions();
		opt.addRegisteredClaim("aud", "jitsi");
		opt.addRegisteredClaim("iss", "my_client");
		opt.addRegisteredClaim("sub", "meet.jit.si");
		opt.addCustomTimeValidationClaim("exp", currentDate, "20");

		claims.setClaim("hola", "hola");

		opt.addHeaderParameter("cty", "twilio-fpa;v=1");
		opt.setSecret(hexaKey);
		String tok = jwt.doCreate("HS256", claims, opt);
		boolean verification = jwt.doVerify(tok, "HS256", claims, opt);
       	True(verification, jwt);
	}

	@Test
	public void testNegative1() {
		options.addHeaderParameter("pepe", "whatever");
		boolean verification = jwt.doVerify(token, "HS256", claims, options);
		assertFalse(verification);
		assertFalse(jwt.hasError());
	}

	@Test
	public void testNegative2() {
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

	@Test
	public void testNegative3() {
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
