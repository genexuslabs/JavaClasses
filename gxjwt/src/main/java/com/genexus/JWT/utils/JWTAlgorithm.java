package com.genexus.JWT.utils;


import com.auth0.jwt.algorithms.Algorithm;
import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;


@SuppressWarnings("LoggingSimilarMessage")
public enum JWTAlgorithm {
	HS256, HS512, RS256, RS512, ES256, ES384, ES512;

	private static final Logger logger = LogManager.getLogger(JWTAlgorithm.class);

	@SuppressWarnings("unused")
	public static String valueOf(JWTAlgorithm jWTAlgorithm, Error error) {
		switch (jWTAlgorithm) {
			case HS256:
				return "HS256";
			case HS512:
				return "HS512";
			case RS256:
				return "RS256";
			case RS512:
				return "RS512";
			case ES256:
				return "ES256";
			case ES384:
				return "ES384";
			case ES512:
				return "ES512";
			default:
				error.setError("JWA01", "Unrecognized algorithm");
				logger.error("Unrecognized algorithm");
				return "Unrecognized algorithm";
		}
	}

	public static JWTAlgorithm getJWTAlgorithm(String jWTAlgorithm, Error error) {
		switch (jWTAlgorithm.toUpperCase().trim()) {
			case "HS256":
				return JWTAlgorithm.HS256;
			case "HS512":
				return JWTAlgorithm.HS512;
			case "RS256":
				return JWTAlgorithm.RS256;
			case "RS512":
				return JWTAlgorithm.RS512;
			case "ES256":
				return JWTAlgorithm.ES256;
			case "ES384":
				return JWTAlgorithm.ES384;
			case "ES512":
				return JWTAlgorithm.ES512;
			default:
				error.setError("JWA02", "Unrecognized algorithm");
				logger.error("Unrecognized algorithm");
				return null;
		}
	}

	public static boolean isPrivate(JWTAlgorithm jWTAlgorithm) {
		switch (jWTAlgorithm) {
			case RS256:
			case RS512:
			case ES256:
			case ES384:
			case ES512:
				return true;
			default:
				return false;
		}
	}

	public static Algorithm getSymmetricAlgorithm(JWTAlgorithm algorithm, byte[] secret, Error error) {
		if (isPrivate(algorithm)) {
			error.setError("JWA03", "It is not a symmetric algorithm name");
			logger.error("It is not a symmetric algorithm name");
			return null;
		} else {
			if (secret == null) {
				error.setError("JWA14", "Set the secret using JWTOptions.SetSecret function");
				logger.error("Set the secret using JWTOptions.SetSecret function");
				return null;
			}
			switch (algorithm) {
				case HS256:
					return Algorithm.HMAC256(secret);
				case HS512:
					return Algorithm.HMAC512(secret);
				default:
					error.setError("JWA04", "Unknown symmetric algorithm");
					logger.error("Unknown symmetric algorithm");
					return null;
			}
		}

	}

	public static Algorithm getAsymmetricAlgorithm(JWTAlgorithm algorithm, PrivateKeyManager key, PublicKey cert,
												   Error error) {
		if (!isPrivate(algorithm)) {
			error.setError("JWA07", "It is not an asymmetric algorithm name");
			logger.error("It is not an asymmetric algorithm name");
			return null;
		} else {
			try {
				switch (algorithm) {
					case RS256:
						if (cert == null) {
							if (key != null)
								return Algorithm.RSA256(null, (RSAPrivateKey) key.getPrivateKey());
						} else {
							return (key != null) ? Algorithm.RSA256((RSAPublicKey) cert.getPublicKey(), (RSAPrivateKey) key.getPrivateKey())
								: Algorithm.RSA256((RSAPublicKey) cert.getPublicKey(), null);
						}
					case RS512:
						if (cert == null) {
							if (key != null)
								return Algorithm.RSA512(null, (RSAPrivateKey) key.getPrivateKey());
						} else {
							return (key != null) ? Algorithm.RSA512((RSAPublicKey) cert.getPublicKey(), (RSAPrivateKey) key.getPrivateKey())
								: Algorithm.RSA512((RSAPublicKey) cert.getPublicKey(), null);
						}
					case ES256:
						if (cert == null) {
							if (key != null)
								return Algorithm.ECDSA256(null, (ECPrivateKey) key.getPrivateKey());
						} else {
							return (key != null) ? Algorithm.ECDSA256((ECPublicKey) cert.getPublicKey(), (ECPrivateKey) key.getPrivateKey())
								: Algorithm.ECDSA256((ECPublicKey) cert.getPublicKey(), null);
						}
					case ES384:
						if (cert == null) {
							if (key != null)
								return Algorithm.ECDSA384(null, (ECPrivateKey) key.getPrivateKey());
						} else {
							return (key != null) ? Algorithm.ECDSA384((ECPublicKey) cert.getPublicKey(), (ECPrivateKey) key.getPrivateKey())
								: Algorithm.ECDSA384((ECPublicKey) cert.getPublicKey(), null);
						}
					case ES512:
						if (cert == null) {
							if (key != null)
								return Algorithm.ECDSA512(null, (ECPrivateKey) key.getPrivateKey());
						} else {
							return (key != null) ? Algorithm.ECDSA512((ECPublicKey) cert.getPublicKey(), (ECPrivateKey) key.getPrivateKey())
								: Algorithm.ECDSA512((ECPublicKey) cert.getPublicKey(), null);
						}
					default:
						error.setError("JWA13", "Unknown asymmetric algorithm");
						logger.error("Unknown asymmetric algorithm");
						return null;
				}
			} catch (Exception e) {
				error.setError("JWA12", e.getMessage());
				logger.error("getAsymmetricAlgorithm", e);
				return null;
			}
		}

	}
}
