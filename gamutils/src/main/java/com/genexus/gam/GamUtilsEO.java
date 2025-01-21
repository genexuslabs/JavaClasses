package com.genexus.gam;

import com.genexus.gam.utils.Encoding;
import com.genexus.gam.utils.Random;
import com.genexus.gam.utils.cryptography.Encryption;
import com.genexus.gam.utils.cryptography.Hash;
import com.genexus.gam.utils.json.Jwk;
import com.genexus.gam.utils.json.Jwt;
import com.genexus.gam.utils.json.UnixTimestamp;

import java.util.Date;

public class GamUtilsEO {

	/********EXTERNAL OBJECT PUBLIC METHODS  - BEGIN ********/

	//**HASH**//
	public static String sha512(String plainText) {
		return Hash.hash(plainText, Hash.SHA512);
	}

	public static String sha256(String plainText) {
		return Hash.hash(plainText, Hash.SHA256);
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

	public static String getJwkAlgorithm(String jwkString) {
		return Jwk.getAlgorithm(jwkString);
	}

	//**JWT**//
	public static boolean verifyJwt(String path, String alias, String password, String token) {
		return Jwt.verify(path, alias, password, token);
	}

	public static String createJwt(String path, String alias, String password, String payload, String header) {
		return Jwt.create(path, alias, password, payload, header);
	}

	public static boolean verifyAlgorithm(String expectedAlgorithm, String token)
	{
		return Jwt.verifyAlgorithm(expectedAlgorithm, token);
	}

	public static long createUnixTimestamp(Date date) {
		return UnixTimestamp.create(date);
	}

	public static String getJwtHeader(String token) {
		return Jwt.getHeader(token);
	}

	public static String getJwtPayload(String token) {
		return Jwt.getPayload(token);
	}

	//**ENCODING**//
	public static String base64ToBase64Url(String base64) {
		return Encoding.b64ToB64Url(base64);
	}

	public static String hexaToBase64(String hexa) { return Encoding.hexaToBase64(hexa); }

	public static String toBase64Url(String input) { return Encoding.toBase64Url(input); }

	public static String fromBase64Url(String base64) { return Encoding.fromBase64Url(base64); }

	/********EXTERNAL OBJECT PUBLIC METHODS  - END ********/
}
