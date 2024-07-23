package com.genexus.gam.utils;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.bouncycastle.util.encoders.Base64;

import java.util.UUID;

public class Jwks {

	public static final ILogger logger = LogManager.getLogger(Jwks.class);
	public static String generateKeyPair()
	{
		try{
			String kid = UUID.randomUUID().toString();
			RSAKey jwk = new RSAKeyGenerator(2048)
				.keyUse(KeyUse.SIGNATURE)
				.keyID(kid)
				.algorithm(Algorithm.parse("RS256"))
				.generate();
			return jwk.toString();

		}catch(Exception e)
		{
			logger.error("generateKeyPair", e);
			return "";
		}
	}

	public static String getB64PublicKeyFromJwk(String jwkString)
	{
		if(jwkString.isEmpty())
		{
			logger.error("getB64PublicKeyFromJwk jwkString parameter is empty");
			return "";
		}
		try
		{
			JWK jwk = JWK.parse(jwkString);
			return Base64.toBase64String(jwk.toRSAKey().toRSAPublicKey().getEncoded());
		}catch (Exception e)
		{
			logger.error("getB64PublicKeyFromJwk", e);
			return "";
		}
	}

	public static String getB64PrivateKeyFromJwk(String jwkString)
	{
		if(jwkString.isEmpty())
		{
			logger.error("getB64PrivateKeyFromJwk jwkString parameter is empty");
			return "";
		}
		try
		{
			JWK jwk = JWK.parse(jwkString);
			return Base64.toBase64String(jwk.toRSAKey().toRSAPrivateKey().getEncoded());
		}catch (Exception e)
		{
			logger.error("getB64PrivateKeyFromJwk", e);
			return "";
		}
	}

	public static String getPublicJwk(String jwkString)
	{
		if( jwkString.isEmpty())
		{
			logger.error("getPublicJwk jwkString parameter is empty");
			return "";
		}
		try
		{
			JWK jwk = JWK.parse(jwkString);
			return  jwk.toPublicJWK().toString();
		}catch (Exception e)
		{
			logger.error("getPublicJwk", e);
			return "";
		}
	}
}
