package com.genexus.gam.utils.json;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class Jwk {

	private static Logger logger = LogManager.getLogger(Jwk.class);

	public static String generateKeyPair() {
		try {
			String kid = UUID.randomUUID().toString();
			RSAKey key = new RSAKeyGenerator(2048)
				.keyUse(KeyUse.SIGNATURE)
				.keyID(kid)
				.algorithm(Algorithm.parse("RS256"))
				.generate();
			return key.toString();

		} catch (Exception e) {
			logger.error("generateKeyPair", e);
			return "";
		}
	}

	public static String getPublic(String jwkString) {
		if (jwkString.isEmpty()) {
			logger.error("getPublic jwkString parameter is empty");
			return "";
		}
		try {
			JWK jwk = JWK.parse(jwkString);
			return jwk.toPublicJWK().toString();
		} catch (Exception e) {
			logger.error("getPublic", e);
			return "";
		}
	}

	public static String getAlgorithm(String jwkString) {
		if (jwkString.isEmpty()) {
			logger.error("getAlgorithm jwkString parameter is empty");
			return "";
		}
		try {
			return JWK.parse(jwkString).getAlgorithm().toString();
		} catch (Exception e) {
			logger.error("getPublic", e);
			return "";
		}
	}


	/*public static boolean verifyJWT(String jwkString, String token) {
		if (jwkString.isEmpty()) {
			logger.error("verifyJWT jwkString parameter is empty");
			return false;
		}
		if (token.isEmpty()) {
			logger.error("verifyJWT token parameter is empty");
			return false;
		}
		try {
			JWK jwk = JWK.parse(jwkString);
			return Jwt.verify(jwk.toRSAKey().toRSAPublicKey(), token);
		} catch (Exception e) {
			logger.error("verifyJWT", e);
			return false;
		}

	}

	public static String createJwt(String jwkString, String payload, String header) {
		if (jwkString.isEmpty()) {
			logger.error("createJwt jwkString parameter is empty");
			return "";
		}
		if (payload.isEmpty()) {
			logger.error("createJwt payload parameter is empty");
			return "";
		}
		if (header.isEmpty()) {
			logger.error("createJwt header parameter is empty");
			return "";
		}
		try {
			JWK jwk = JWK.parse(jwkString);
			return Jwt.create(jwk.toRSAKey().toRSAPrivateKey(), payload, header);
		} catch (Exception e) {
			logger.error("createJwt", e);
			return "";
		}
	}*/
}
