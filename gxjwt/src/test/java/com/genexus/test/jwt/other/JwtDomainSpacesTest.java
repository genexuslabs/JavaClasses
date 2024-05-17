package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.DateUtil;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.test.jwt.features.JwtVerifyJustSignatureTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JwtDomainSpacesTest extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static SymmetricKeyGenerator keyGen;
	protected static DateUtil du;
	protected PrivateClaims claims;
	protected static String currentDate;
	protected static String hexaKey;

	public static Test suite() {
		return new TestSuite(JwtDomainSpacesTest.class);
	}

	@Override
	public void runTest() {
		testDomains();

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

		options.addRegisteredClaim("aud ", "jitsi");
		options.addRegisteredClaim(" iss", "my_client");
		options.addRegisteredClaim(" sub ", "meet.jit.si");
		String expiration = du.currentPlusSeconds(200);
		options.addCustomTimeValidationClaim("exp", expiration, "20");

		claims.setClaim("hola", "hola");

		options.addHeaderParameter("cty", "twilio-fpa;v=1");
		options.setSecret(hexaKey);

	}

	public void testDomains() {

		options.setSecret(hexaKey);
		String token = jwt.doCreate("HS256 ", claims, options);
		assertFalse(jwt.hasError());
		boolean verification = jwt.doVerifyJustSignature(token, " HS256", options);
		True(verification, jwt);
	}


}
