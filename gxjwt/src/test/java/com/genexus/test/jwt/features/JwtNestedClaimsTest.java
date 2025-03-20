package com.genexus.test.jwt.features;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.test.jwt.resources.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class JwtNestedClaimsTest extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static SymmetricKeyGenerator keyGen;
	protected static PrivateClaims claimslevel1;
	protected static PrivateClaims claimslevel2;
	protected static PrivateClaims claimslevel3;
	protected static String token;
	protected static String currentDate;
	protected static String hexaKey;

	@BeforeClass
	public static void setUp() {
		jwt = new JWTCreator();
		options = new JWTOptions();
		keyGen = new SymmetricKeyGenerator();
		claimslevel1 = new PrivateClaims();
		claimslevel2 = new PrivateClaims();
		claimslevel3 = new PrivateClaims();

		currentDate = TestUtils.getCurrentDate();
		hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);

		options.addRegisteredClaim("aud", "jitsi");
		options.addRegisteredClaim("iss", "my_client");
		options.addRegisteredClaim("sub", "meet.jit.si");
		options.addCustomTimeValidationClaim("exp", currentDate, "20");

		claimslevel1.setClaim("room", "*");

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

	@Test
	public void testPositive() {
		JWTOptions opt = new JWTOptions();
		opt.addRegisteredClaim("aud", "jitsi");
		opt.addRegisteredClaim("iss", "my_client");
		opt.addRegisteredClaim("sub", "meet.jit.si");
		opt.addCustomTimeValidationClaim("exp", currentDate, "20");

		opt.setSecret(hexaKey);
		String tok = jwt.doCreate("HS256", claimslevel1, opt);
		boolean verification = jwt.doVerify(tok, "HS256", claimslevel1, opt);
		True(verification, jwt);
	}

	@Test
	public void testNegative1() {
		claimslevel2.setClaim("pepe", "whatever");
		boolean verification = jwt.doVerify(token, "HS256", claimslevel1, options);
		assertFalse(verification);
		assertFalse(jwt.hasError());
	}

	@Test
	public void testNegative2() {
		PrivateClaims claimslevel11 = new PrivateClaims();
		PrivateClaims claimslevel21 = new PrivateClaims();
		PrivateClaims claimslevel31 = new PrivateClaims();
		claimslevel11.setClaim("room", "*");

		claimslevel11.setClaim("context", claimslevel21);

		claimslevel21.setClaim("user", claimslevel31);
		claimslevel31.setClaim("avatar", "https:/gravatar.com/avatar/abc123");
		claimslevel31.setClaim("name", "John Doe");
		claimslevel31.setClaim("email", "jdoe@example.com");
		claimslevel31.setClaim("id", "abcd:a1b2c3-d4e5f6-0abc1-23de-abcdef01fedcba");

		boolean verification = jwt.doVerify(token, "HS256", claimslevel11, options);
		assertFalse(verification);
	}

}
