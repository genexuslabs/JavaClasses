package com.genexus.gam.utils.json;

import com.genexus.gam.utils.keys.CertificateUtil;
import com.genexus.gam.utils.keys.PrivateKeyUtil;
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

	private static Logger logger = LogManager.getLogger(Jwt.class);

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public static boolean verify(RSAPublicKey publicKey, String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			JWSVerifier verifier = new RSASSAVerifier(publicKey);
			return signedJWT.verify(verifier);
		} catch (Exception e) {
			logger.error("verify", e);
			return false;
		}
	}

	public static String create(RSAPrivateKey privateKey, String payload, String header) {
		try {
			SignedJWT signedJWT = new SignedJWT(JWSHeader.parse(header), JWTClaimsSet.parse(payload));
			signedJWT.sign(new RSASSASigner(privateKey));
			return signedJWT.serialize();
		} catch (Exception e) {
			logger.error("create", e);
			return "";
		}
	}

	public static boolean verify(String path, String alias, String password, String token) {
		return verify((RSAPublicKey) CertificateUtil.getCertificate(path, alias, password).getPublicKey(), token);
	}

	public static String create(String path, String alias, String password, String payload, String header)
	{
		return create(PrivateKeyUtil.getPrivateKey(path, alias, password), payload, header);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


}
