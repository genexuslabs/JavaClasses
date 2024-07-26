package com.genexus.gam.utils;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

public class Jwks {

	public static final ILogger logger = LogManager.getLogger(Jwks.class);

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
