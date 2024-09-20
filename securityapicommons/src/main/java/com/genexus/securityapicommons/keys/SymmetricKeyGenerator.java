package com.genexus.securityapicommons.keys;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;


public class SymmetricKeyGenerator extends SecurityAPIObject {

	private static final Logger logger = LogManager.getLogger(SymmetricKeyGenerator.class);

	public SymmetricKeyGenerator() {
		super();
	}

	public String doGenerateKey(String symmetricKeyType, int length) {
		logger.debug("doGenerateKey");
		this.error.cleanError();
		SymmetricKeyType sKeyType = SymmetricKeyType.getSymmetricKeyType(symmetricKeyType, this.error);
		if (sKeyType == SymmetricKeyType.GENERICRANDOM) {
			return genericKeyGenerator(length);
		}
		this.error.setError("SS003", "Unrecognized SymmetricKeyType");
		logger.error("Unrecognized SymmetricKeyType");
		return "";
	}

	public String doGenerateIV(String symmetricKeyType, int length) {
		logger.debug("doGenerateIV");
		this.error.cleanError();
		return doGenerateKey(symmetricKeyType, length);
	}

	@SuppressWarnings("unused")
	public String doGenerateNonce(String symmetricKeyType, int length) {
		logger.debug("doGenerateNonce");
		this.error.cleanError();
		return doGenerateKey(symmetricKeyType, length);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	private String genericKeyGenerator(int length) {

		logger.debug("genericKeyGenerator");

		try {
			SecureRandom random = new SecureRandom();
			byte[] values = new byte[length / 8];
			random.nextBytes(values);
			StringBuilder sb = new StringBuilder();
			for (byte b : values) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString().replaceAll("\\s", "");
		} catch (Exception e) {
			this.error.setError("SK004", "Key generation error");
			logger.error("genericKeyGenerator", e);
			return "";
		}
	}

}
