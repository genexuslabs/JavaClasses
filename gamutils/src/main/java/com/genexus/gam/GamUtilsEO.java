package com.genexus.gam;

import com.genexus.gam.utils.Random;
import com.genexus.gam.utils.cryptography.Encryption;
import com.genexus.gam.utils.cryptography.Hash;
import com.genexus.gam.utils.json.Jwk;
import com.genexus.gam.utils.json.Jwks;
import com.genexus.gam.utils.json.Jwt;

public class GamUtilsEO {

	/********EXTERNAL OBJECT PUBLIC METHODS  - BEGIN ********/

	//**HASH**//
	public static String sha512(String plainText) {
		return Hash.sha512(plainText);
	}

	//**ENCRYPTION**//

	public static String AesGcm(String input, String key, String nonce, int macSize, boolean toEncrypt) {
		return Encryption.AesGcm(input, key, nonce, macSize, toEncrypt);
	}

	//**RANDOM**//
	public static String randomAlphanumeric(int length) {
		return Random.alphanumeric(length);
	}

	public static String randomNumeric(int length) {
		return Random.numeric(length);
	}

	public static String randomHexaBits(int bits) {
		return Random.hexaBits(bits);
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

	public static String createJWTWithFile(String path, String alias, String password, String payload, String header) {
		return Jwt.create(path, alias, password, payload, header);
	}

	/********EXTERNAL OBJECT PUBLIC METHODS  - END ********/
}
