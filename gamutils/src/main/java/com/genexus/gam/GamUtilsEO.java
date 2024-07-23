package com.genexus.gam;

import com.genexus.gam.utils.Hash;
import com.genexus.gam.utils.Jwks;
import com.genexus.gam.utils.Random;

public class GamUtilsEO {

	/********EXTERNAL OBJECT PUBLIC METHODS  - BEGIN ********/

	//**HASH**//
	public static String sha512(String plainText)
	{
		return Hash.sha512(plainText);
	}

	//**RANDOM**//
	public static String randomAlphanumeric(int length)
	{
		return Random.randomAlphanumeric(length);
	}

	public static String randomNumeric(int length)
	{
		return Random.randomNumeric(length);
	}


	//**JWKS**//
	public static String generateKeyPair() { return Jwks.generateKeyPair(); }

	public static String getB64PublicKeyFromJwk(String jwkString) { return Jwks.getB64PublicKeyFromJwk(jwkString); }

	public static String getB64PrivateKeyFromJwk(String jwkString) { return Jwks.getB64PrivateKeyFromJwk(jwkString); }

	public static String getPublicJwk(String jwkString) { return Jwks.getPublicJwk(jwkString); }

	/********EXTERNAL OBJECT PUBLIC METHODS  - END ********/
}
