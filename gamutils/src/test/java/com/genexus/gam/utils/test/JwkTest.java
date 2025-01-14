package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;

public class JwkTest {

	@Test
	public void testGenerateKeyPair() {
		String jwk = GamUtilsEO.generateKeyPair();
		Assert.assertFalse("Generate key pair jwk", jwk.isEmpty());
	}

	@Test
	public void testPublicJwk() {
		String jwk = GamUtilsEO.generateKeyPair();
		String public_jwk = GamUtilsEO.getPublicJwk(jwk);
		String public_jwks = "{\"keys\": [" + public_jwk + "]}";
		try {
			JWKSet jwks = JWKSet.parse(public_jwks).toPublicJWKSet();
			Assert.assertNotNull("To public JWK fail", jwks);
		} catch (ParseException e) {
			Assert.fail("Exception on testPublicJwk" + e.getMessage());
		}
	}

	@Test
	public void testGetAlgorithm()
	{
		String jwk = GamUtilsEO.generateKeyPair();
		String algorithm = GamUtilsEO.getJwkAlgorithm(jwk);
		Assert.assertEquals("testGetAlgorithm", algorithm, "RS256");
	}
}
