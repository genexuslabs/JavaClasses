package com.genexus.gam.utils.test;

import com.genexus.gam.utils.Jwks;
import com.genexus.gam.utils.test.resources.securityapicommons.keys.PrivateKeyManager;
import com.nimbusds.jose.jwk.JWKSet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.genexus.gam.utils.test.resources.securityapicommons.keys.PublicKey;
import org.junit.Assert;

import java.text.ParseException;

public class JwksTest extends TestCase {

	private static String jwk;
	public static Test suite() {
		return new TestSuite(JwksTest.class);
	}

	@Override
	public void runTest() {
		testGenerateKeyPair();
		testLoadPublicKey();
		testLoadPrivateKey();
		testPublicJwk();
	}

	@Override
	public void setUp() {
		jwk = Jwks.generateKeyPair();
	}

	public void testGenerateKeyPair()
	{
		String jwk = Jwks.generateKeyPair();
		Assert.assertTrue("Generate key pair jwk", jwk.length() > 0);
	}
	public void testLoadPublicKey()
	{
		String b64 = Jwks.getB64PublicKeyFromJwk(jwk);
		PublicKey key = new PublicKey();
		boolean loaded = key.fromBase64(b64);
		Assert.assertTrue("Load public key from base64 jwk", loaded);
		Assert.assertFalse("Public key has error loading from jwk", key.hasError());
	}

	public void testLoadPrivateKey()
	{
		String b64 = Jwks.getB64PrivateKeyFromJwk(jwk);
		PrivateKeyManager key = new PrivateKeyManager();
		boolean loaded = key.fromBase64(b64);
		Assert.assertTrue("Load private key from base64 jwk", loaded);
		Assert.assertFalse("Private key has error loading from jwk", key.hasError());
	}

	public void testPublicJwk() {
		String public_jwk = Jwks.getPublicJwk(jwk);
		String public_jwks = "{\"keys\": [" + public_jwk + "]}";
		try {
			JWKSet jwks = JWKSet.parse(public_jwks).toPublicJWKSet();
			Assert.assertNotNull("To public JWK fail", jwks);
		} catch (ParseException e) {
			Assert.fail("Exception on testPublicJwk" + e.getMessage());
		}
	}
}
