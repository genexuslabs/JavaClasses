package com.genexus.gam.utils.json;

import com.genexus.gam.utils.keys.PrivateKeyUtil;
import com.genexus.gam.utils.keys.PublicKeyUtil;
import com.nimbusds.jose.*;
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
import java.text.ParseException;
import java.util.Objects;

public class Jwt {

	private static final Logger logger = LogManager.getLogger(Jwt.class);

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public static boolean verify(String path, String alias, String password, String token) {
		logger.debug("verify");
		try {
			return verify_internal(path, alias, password, token);
		} catch (Exception e) {
			logger.error("verify", e);
			return false;
		}
	}

	public static String create(String path, String alias, String password, String payload, String header) {
		logger.debug("create");
		try {
			return create_internal(path, alias, password, payload, header);
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

	private static boolean verify_internal(String path, String alias, String password, String token) throws JOSEException, ParseException {
		logger.debug("verify_internal");
		JWTAlgorithm algorithm = JWTAlgorithm.getJWTAlgoritm(JWSHeader.parse(getHeader(token)).getAlgorithm().getName());
		assert algorithm != null;
		boolean isSymmetric = JWTAlgorithm.isSymmetric(algorithm);
		SignedJWT signedJWT = SignedJWT.parse(token);
		JWSVerifier verifier = isSymmetric ? new MACVerifier(password):new RSASSAVerifier(Objects.requireNonNull(PublicKeyUtil.getPublicKey(path, alias, password, token)));
		return signedJWT.verify(verifier);
	}

	private static String create_internal(String path, String alias, String password, String payload, String header) throws Exception {
		logger.debug("create_internal");
		JWSHeader parsedHeader = JWSHeader.parse(header);
		JWTAlgorithm algorithm = JWTAlgorithm.getJWTAlgoritm(parsedHeader.getAlgorithm().getName());
		assert algorithm != null;
		boolean isSymmetric = JWTAlgorithm.isSymmetric(algorithm);
		SignedJWT signedJWT = new SignedJWT(parsedHeader, JWTClaimsSet.parse(payload));
		JWSSigner signer = isSymmetric ? new MACSigner(password): new RSASSASigner(Objects.requireNonNull(PrivateKeyUtil.getPrivateKey(path, alias, password)));
		signedJWT.sign(signer);
		return signedJWT.serialize();
	}
}
