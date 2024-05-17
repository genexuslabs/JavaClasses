package com.genexus.test.jwt.other;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class issue84141Test extends SecurityAPITestObject {
	protected static String ID;
	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static SymmetricKeyGenerator keyGen;
	protected static String token;

	public static Test suite() {
		return new TestSuite(issue84141Test.class);
	}

	@Override
	public void runTest() {
		test_TokenTimes();
	}

	@Override
	public void setUp() {
		options = new JWTOptions();
		jwt = new JWTCreator();
		claims = new PrivateClaims();
		keyGen = new SymmetricKeyGenerator();

		options.addRegisteredClaim("iss", "GXSA");
		options.addRegisteredClaim("sub", "subject1");
		options.addRegisteredClaim("aud", "audience1");
		ID = "0696bb20-6223-4a1c-9ebf-e15c74387b9c, 0696bb20-6223-4a1c-9ebf-e15c74387b9c";// &guid.Generate()
		options.addRegisteredClaim("jti", ID);
		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");
		options.addCustomTimeValidationClaim("exp", "2030/07/07 10:15:20", "0");
		options.addCustomTimeValidationClaim("nbf", "2020/07/07 10:15:20", "0");
		options.addCustomTimeValidationClaim("iat", "2020/08/03 10:15:20", "0");
	}

	@SuppressWarnings("unchecked")
	public static void test_TokenTimes() {
		String hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey);
		token = jwt.doCreate("HS256", claims, options);
		String payload = jwt.getPayload(token);
		System.out.println(payload);
		ObjectMapper objectMapper = new ObjectMapper();
		HashMap<String, Object> map = null;
		try {
			map = objectMapper.readValue(payload, HashMap.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String nbf = Integer.toString((int) map.get("nbf"));
		String exp = Integer.toString((int) map.get("exp"));
		String iat = Integer.toString((int) map.get("iat"));
		assertTrue(SecurityUtils.compareStrings(nbf, "1594116920"));
		assertTrue(SecurityUtils.compareStrings(exp, "1909649720"));
		assertTrue(SecurityUtils.compareStrings(iat, "1596449720"));
	}
}
