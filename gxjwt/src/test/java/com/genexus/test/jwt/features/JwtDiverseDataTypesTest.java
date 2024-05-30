package com.genexus.test.jwt.features;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.DateUtil;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JwtDiverseDataTypesTest extends SecurityAPITestObject{

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static SymmetricKeyGenerator keyGen;
	protected static DateUtil du;
	protected PrivateClaims claimslevel1;
	protected PrivateClaims claimslevel2;
	protected PrivateClaims claimslevel3;
	protected static String token;
	protected static String currentDate;
	protected static String hexaKey;

	public static Test suite() {
		return new TestSuite(JwtDiverseDataTypesTest.class);
	}

	@Override
	public void runTest() {
		testPositive();
	}

	@Override
	public void setUp() {
		jwt = new JWTCreator();
		options = new JWTOptions();
		du = new DateUtil();
		keyGen = new SymmetricKeyGenerator();
		claimslevel1 = new PrivateClaims();
		claimslevel2 = new PrivateClaims();
		claimslevel3 = new PrivateClaims();

		currentDate = du.getCurrentDate();
		hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);

		options.addRegisteredClaim("aud", "jitsi");
		options.addRegisteredClaim("iss", "my_client");
		options.addRegisteredClaim("sub", "meet.jit.si");
		options.addCustomTimeValidationClaim("exp", currentDate, "20");

		claimslevel1.setClaim("room", "*");
		claimslevel1.setNumericClaim("uno", 1);
		claimslevel1.setBooleanClaim("boolean", true);
		//1607626804
		claimslevel1.setDateClaim("date", 1607626804);


		claimslevel1.setClaim("context", claimslevel2);

		claimslevel2.setClaim("user", claimslevel3);
		claimslevel3.setClaim("avatar", "https:/gravatar.com/avatar/abc123");
		claimslevel3.setClaim("name", "John Doe");
		claimslevel3.setClaim("email", "jdoe@example.com");
		claimslevel3.setClaim("id", "abcd:a1b2c3-d4e5f6-0abc1-23de-abcdef01fedcba");
		claimslevel2.setClaim("group", "a123-123-456-789");

		options.setSecret(hexaKey);
		token = jwt.doCreate("HS256", claimslevel1, options);
	}

	public void testPositive()
	{
		boolean verification = jwt.doVerify(token, "HS256", claimslevel1, options);
		True(verification, jwt);
	}
}