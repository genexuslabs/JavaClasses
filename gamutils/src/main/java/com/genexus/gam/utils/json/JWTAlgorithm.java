package com.genexus.gam.utils.json;

import com.nimbusds.jose.JWSAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum JWTAlgorithm {

	HS256, HS512, HS384, RS256, RS512;

	private static final Logger logger = LogManager.getLogger(JWTAlgorithm.class);

	public static JWSAlgorithm getJWSAlgorithm(JWTAlgorithm alg)
	{
		logger.debug("getJWSAlgorithm");
		switch (alg)
		{
			case HS256:
				return JWSAlgorithm.HS256;
			case HS512:
				return JWSAlgorithm.HS512;
			case HS384:
				return JWSAlgorithm.HS384;
			case RS256:
				return JWSAlgorithm.RS256;
			case RS512:
				return JWSAlgorithm.RS512;
			default:
				logger.error("getJWSAlgorithm - not implemented algorithm");
				return null;
		}
	}

	public static JWTAlgorithm getJWTAlgoritm(String alg)
	{
		logger.debug("getJWTAlgoritm");
		switch (alg.trim().toUpperCase())
		{
			case "HS256":
				return JWTAlgorithm.HS256;
			case "HS512":
				return JWTAlgorithm.HS512;
			case "HS384":
				return JWTAlgorithm.HS384;
			case "RS256":
				return JWTAlgorithm.RS256;
			case "RS512":
				return JWTAlgorithm.RS512;
			default:
				logger.error("getJWTAlgoritm- not implemented algorithm");
				return null;
		}
	}

	public static boolean isSymmetric(JWTAlgorithm alg)
	{
		logger.debug("isSymmetric");
		switch (alg)
		{
			case HS256:
			case HS384:
			case HS512:
				return true;
			case RS256:
			case RS512:
				return false;
			default:
				logger.error("isSymmetric - not implemented algorithm");
				return false;
		}
	}
}
