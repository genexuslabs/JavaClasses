package com.genexus.gam.utils.json;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Jwks {

	private static Logger logger = LogManager.getLogger(Jwks.class);

	public static boolean verifyJWT(String jwksString, String token, String kid) {
		if (jwksString.isEmpty()) {
			logger.error("verifyJWT jwksString parameter is empty");
			return false;
		}
		if (token.isEmpty()) {
			logger.error("verifyJWT token parameter is empty");
			return false;
		}
		if (kid.isEmpty()) {
			logger.error("verifyJWT kid parameter is empty");
			return false;
		}
		try {
			JWKSet set = JWKSet.parse(jwksString);
			JWK jwk = set.getKeyByKeyId(kid);
			return Jwt.verify(jwk.toRSAKey().toRSAPublicKey(), token);
		} catch (Exception e) {
			logger.error("verifyJWT", e);
			return false;
		}
	}
}
