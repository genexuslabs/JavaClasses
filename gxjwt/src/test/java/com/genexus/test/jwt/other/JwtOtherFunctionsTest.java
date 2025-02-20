package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JwtOtherFunctionsTest extends SecurityAPITestObject {

	protected static String ID;
	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static SymmetricKeyGenerator keyGen;
	protected static String token;

	@BeforeClass
	public static void setUp() {
		options = new JWTOptions();
		jwt = new JWTCreator();
		claims = new PrivateClaims();
		keyGen = new SymmetricKeyGenerator();

		options.addRegisteredClaim("iss", "GXSA");
		options.addRegisteredClaim("sub", "subject1");
		options.addRegisteredClaim("aud", "audience1");
		ID = "0696bb20-6223-4a1c-9ebf-e15c74387b9c"; // &guid.Generate()
		options.addRegisteredClaim("jti", ID);
		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");
		generateToken();
	}


	public static void generateToken() {
		String hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey);
		token = jwt.doCreate("HS256", claims, options);
		assertFalse(jwt.hasError());
		boolean verification = jwt.doVerify(token, "HS256", claims, options);
		True(verification, jwt);
	}

	@Test
	public void testGetID() {
		String tID = jwt.getTokenID(token);
		Assert.assertEquals(ID, tID);
	}

	@Test
	public void testGetPayload() {
		String payload = "{\"sub\":\"subject1\",\"aud\":\"audience1\",\"hola1\":\"hola1\",\"iss\":\"GXSA\",\"hola2\":\"hola2\",\"jti\":\""
			+ ID + "\"}";
		String payload1 = "{\"hola1\":\"hola1\",\"hola2\":\"hola2\",\"iss\":\"GXSA\",\"sub\":\"subject1\",\"aud\":\"audience1\",\"jti\":\""
			+ ID + "\"}";
		String payload2 = "{\"sub\":\"subject1\",\"aud\":\"audience1\",\"hola1\":\"hola1\",\"iss\":\"GXSA\",\"hola2\":\"hola2\",\"jti\":\""
			+ ID + "\"}";

		String tPayload = jwt.getPayload(token);
		Assert.assertTrue(SecurityUtils.compareStrings(payload, tPayload) || SecurityUtils.compareStrings(payload1, tPayload)
			|| SecurityUtils.compareStrings(payload2, tPayload));
	}

	@Test
	public void testGetHeader() {
		String header = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
		String header1 = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
		String tHeader = jwt.getHeader(token);
		Assert.assertTrue(SecurityUtils.compareStrings(header, tHeader) || SecurityUtils.compareStrings(header1, tHeader));
	}

	@Test
	public void testbadAlgorithm() {
		String hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey);
		String token1 = jwt.doCreate("HS256", claims, options);
		boolean verification = jwt.doVerify(token1, "RS256", claims, options);
		Assert.assertFalse(verification);
		Assert.assertTrue(jwt.hasError());
	}
}
