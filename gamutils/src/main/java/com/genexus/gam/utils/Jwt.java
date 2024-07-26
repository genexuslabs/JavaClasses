package com.genexus.gam.utils;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.io.FilenameUtils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class Jwt {

	public static final ILogger logger = LogManager.getLogger(Jwt.class);

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

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	private static boolean extensionIs(String stringPath, String extension) {
		return extension.equalsIgnoreCase(FilenameUtils.getExtension(stringPath));
	}

}
