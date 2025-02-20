package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.test.jwt.resources.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class JwtDomainSpacesTest extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static SymmetricKeyGenerator keyGen;
	protected static PrivateClaims claims;
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

		options.addRegisteredClaim("aud ", "jitsi");
		options.addRegisteredClaim(" iss", "my_client");
		options.addRegisteredClaim(" sub ", "meet.jit.si");
		String expiration = TestUtils.currentPlusSeconds(200);
		options.addCustomTimeValidationClaim("exp", expiration, "20");

		claims.setClaim("hola", "hola");

		options.addHeaderParameter("cty", "twilio-fpa;v=1");
		options.setSecret(hexaKey);

	}

	@Test
	public void testDomains() {

		options.setSecret(hexaKey);
		String token = jwt.doCreate("HS256 ", claims, options);
		assertFalse(jwt.hasError());
		boolean verification = jwt.doVerifyJustSignature(token, " HS256", options);
		True(verification, jwt);
	}


}
