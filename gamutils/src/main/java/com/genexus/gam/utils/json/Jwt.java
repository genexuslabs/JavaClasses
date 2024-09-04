package com.genexus.gam.utils.json;

import com.genexus.gam.utils.keys.PrivateKeyUtil;
import com.genexus.gam.utils.keys.PublicKeyUtil;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class Jwt {

	private static final Logger logger = LogManager.getLogger(Jwt.class);

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public static boolean verify(String path, String alias, String password, String token) {
		logger.debug("verify");
		try {
			return verify(PublicKeyUtil.getPublicKey(path, alias, password, token), token);
		} catch (Exception e) {
			logger.error("verify", e);
			return false;
		}
	}

	public static String create(String path, String alias, String password, String payload, String header) {
		logger.debug("create");
		try {
			return create(PrivateKeyUtil.getPrivateKey(path, alias, password), payload, header);
		}catch (Exception e)
		{
			logger.error("create", e);
			return "";
		}
	}

	public static String getHeader(String token) {
		logger.debug("getHeader");
		try {
			return SignedJWT.parse(token).getHeader().toString();
		} catch (Exception e) {
			logger.error("getHeader", e);
			return "";
		}
	}

	public static String getPayload(String token) {
		logger.debug("getPayload");
		try {
			return SignedJWT.parse(token).getPayload().toString();
		} catch (Exception e) {
			logger.error("getPayload", e);
			return "";
		}
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private static boolean verify(RSAPublicKey publicKey, String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			JWSVerifier verifier = new RSASSAVerifier(publicKey);
			return signedJWT.verify(verifier);
		} catch (Exception e) {
			logger.error("verify", e);
			return false;
		}
	}

	private static String create(RSAPrivateKey privateKey, String payload, String header) {
		try {
			SignedJWT signedJWT = new SignedJWT(JWSHeader.parse(header), JWTClaimsSet.parse(payload));
			signedJWT.sign(new RSASSASigner(privateKey));
			return signedJWT.serialize();
		} catch (Exception e) {
			logger.error("create", e);
			return "";
		}
	}

}
