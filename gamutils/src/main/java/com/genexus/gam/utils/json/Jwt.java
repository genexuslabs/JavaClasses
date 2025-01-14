package com.genexus.gam.utils.json;

import com.genexus.gam.utils.keys.PrivateKeyUtil;
import com.genexus.gam.utils.keys.PublicKeyUtil;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
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

	public static boolean verify(String path, String alias, String password, String token, String secret, boolean isSymmetric) {
		logger.debug("verify");
		try {
			return !isSymmetric ? verify_internal(PublicKeyUtil.getPublicKey(path, alias, password, token), token, "", isSymmetric) : verify_internal(null, token, secret, isSymmetric);
		} catch (Exception e) {
			logger.error("verify", e);
			return false;
		}
	}

	public static String create(String path, String alias, String password, String payload, String header, String secret, boolean isSymmetric) {
		logger.debug("create");
		try {
			return !isSymmetric ? create_internal(PrivateKeyUtil.getPrivateKey(path, alias, password), payload, header, "", isSymmetric): create_internal(null, payload, header, secret, isSymmetric);
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

	public static boolean verifyAlgorithm(String algorithm, String token)
	{
		logger.debug("verifyAlgorithm");
		try{
			return SignedJWT.parse(token).getHeader().getAlgorithm().equals(JWSAlgorithm.parse(algorithm));
		}catch (Exception e)
		{
			logger.error("verifyAlgorithm", e);
			return false;
		}
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private static boolean verify_internal(RSAPublicKey publicKey, String token, String secret, boolean isSymmetric){
		logger.debug("verify_internal");
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			JWSVerifier verifier = isSymmetric ? new MACVerifier(secret):new RSASSAVerifier(publicKey);
			return signedJWT.verify(verifier);
		} catch (Exception e) {
			logger.error("verify_internal", e);
			return false;
		}
	}

	private static String create_internal(RSAPrivateKey privateKey, String payload, String header, String secret, boolean isSymmetric) {
		logger.debug("create_internal");
		try {
			SignedJWT signedJWT = new SignedJWT(JWSHeader.parse(header), JWTClaimsSet.parse(payload));
			JWSSigner signer = isSymmetric ? new MACSigner(secret): new RSASSASigner(privateKey);
			signedJWT.sign(signer);
			return signedJWT.serialize();
		} catch (Exception e) {
			logger.error("create_internal", e);
			return "";
		}
	}

}
