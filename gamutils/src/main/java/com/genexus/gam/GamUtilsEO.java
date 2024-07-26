package com.genexus.gam;

import com.genexus.gam.utils.*;

public class GamUtilsEO {

	/********EXTERNAL OBJECT PUBLIC METHODS  - BEGIN ********/

	//**HASH**//
	public static String sha512(String plainText) {
		return Hash.sha512(plainText);
	}

	//**RANDOM**//
	public static String randomAlphanumeric(int length) {
		return Random.randomAlphanumeric(length);
	}

	public static String randomNumeric(int length) {
		return Random.randomNumeric(length);
	}


	//**JWK**//

	public static String generateKeyPair() {
		return Jwk.generateKeyPair();
	}

	public static String getPublicJwk(String jwkString) {
		return Jwk.getPublic(jwkString);
	}

	public static boolean jwk_verifyJWT(String jwkString, String token) {
		return Jwk.verifyJWT(jwkString, token);
	}

	public static String jwk_createJwt(String jwkString, String payload, String header) {
		return Jwk.createJwt(jwkString, payload, header);
	}

	//**JWKS**//

	public static boolean jwks_verifyJWT(String jwksString, String token, String kid) {
		return Jwks.verifyJWT(jwksString, token, kid);
	}

	//**JWT**//
	public static boolean verifyJWTWithFile(String path, String alias, String password, String token) {
		return Jwt.verify(path, alias, password, token);
	}

	/********EXTERNAL OBJECT PUBLIC METHODS  - END ********/
}
