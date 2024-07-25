package com.genexus.test.jwt.features;

import com.genexus.JWT.JWTCreator;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CreateFromJSONTest extends SecurityAPITestObject{

	protected static String payload;
	protected static String key;
	protected static SymmetricKeyGenerator keyGen;
	protected static JWTCreator jwt;
	protected static JWTOptions options;

	public static Test suite() {
		return new TestSuite(CreateFromJSONTest.class);
	}

	@Override
	public void runTest() {
		testCreateFromJSON();
	}

	@Override
	public void setUp() {
		payload = "{\"sub\":\"subject1\",\"aud\":\"audience1\",\"nbf\":1594116920,\"hola1\":\"hola1\",\"iss\":\"GXSA\",\"hola2\":\"hola2\",\"exp\":1909649720,\"iat\":1596449720,\"jti\":\"0696bb20-6223-4a1c-9ebf-e15c74387b9c, 0696bb20-6223-4a1c-9ebf-e15c74387b9c\"}";
		SymmetricKeyGenerator keyGen = new SymmetricKeyGenerator();
		key = keyGen.doGenerateKey("GENERICRANDOM", 256);
		jwt = new JWTCreator();
		options = new JWTOptions();
	}

	public void testCreateFromJSON() {
		options.setSecret(key);
		String token = jwt.doCreateFromJSON("HS256", payload, options);
		boolean verifies = jwt.doVerifyJustSignature(token, "HS256", options);
		True(verifies, jwt);
	}
}
