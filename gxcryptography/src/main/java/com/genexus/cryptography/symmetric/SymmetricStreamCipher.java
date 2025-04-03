package com.genexus.cryptography.symmetric;

import com.genexus.cryptography.commons.SymmectricStreamCipherObject;
import com.genexus.cryptography.symmetric.utils.SymmetricStreamAlgorithm;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;

public class SymmetricStreamCipher extends SymmectricStreamCipherObject {

	private static final Logger logger = LogManager.getLogger(SymmetricStreamCipher.class);

	public SymmetricStreamCipher() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public String doEncrypt(String symmetricStreamAlgorithm, String key, String IV, String plainText) {
		logger.debug("doEncrypt");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricStreamCipher.class), "doEncrypt", "symmetricStreamAlgorithm", symmetricStreamAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricStreamCipher.class), "doEncrypt", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricStreamCipher.class), "doEncrypt", "plainText", plainText, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		EncodingUtil eu = new EncodingUtil();
		byte[] input = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}

		byte[] encryptedBytes = setUp(symmetricStreamAlgorithm, key, IV, input, true);
		if (this.hasError()) {
			return null;
		}

		return Strings.fromByteArray(Base64.encode(encryptedBytes)).trim();
	}

	public String doDecrypt(String symmetricStreamAlgorithm, String key, String IV, String encryptedInput) {
		logger.debug("doDecrypt");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricStreamCipher.class), "doDecrypt", "symmetricStreamAlgorithm", symmetricStreamAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricStreamCipher.class), "doDecrypt", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricStreamCipher.class), "doDecrypt", "encryptedInput", encryptedInput, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		byte[] input = null;
		try {
			input = Base64.decode(encryptedInput);
		} catch (Exception e) {
			this.error.setError("SS001", e.getMessage());
			return "";
		}

		byte[] decryptedBytes = setUp(symmetricStreamAlgorithm, key, IV, input, false);
		if (this.hasError()) {
			return null;
		}

		EncodingUtil eu = new EncodingUtil();
		String result = eu.getString(decryptedBytes);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		return result.trim();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	private StreamCipher getCipherEngine(SymmetricStreamAlgorithm algorithm) {
		logger.debug("getCipherEngine");

		switch (algorithm) {
			case RC4:
				return new RC4Engine();
			case HC256:
				return new HC256Engine();
			case SALSA20:
				return new Salsa20Engine();
			case CHACHA20:
				return new ChaChaEngine();
			case XSALSA20:
				return new XSalsa20Engine();
			case ISAAC:
				return new ISAACEngine();
			default:
				this.error.setError("SS002", "Unrecognized stream cipher algorithm");
				logger.error("Unrecognized stream cipher algorithm");
				return null;
		}
	}

	private byte[] setUp(String symmetricStreamAlgorithm, String key, String IV, byte[] input, boolean toEncrypt) {
		byte[] keyBytes = SecurityUtils.hexaToByte(key, this.error);
		byte[] ivBytes = SecurityUtils.hexaToByte(IV, this.error);
		SymmetricStreamAlgorithm algorithm = SymmetricStreamAlgorithm
			.getSymmetricStreamAlgorithm(symmetricStreamAlgorithm, this.error);
		if (this.hasError()) {
			return null;
		}

		return encrypt(algorithm, keyBytes, ivBytes, input, toEncrypt);

	}

	private byte[] encrypt(SymmetricStreamAlgorithm algorithm, byte[] key, byte[] IV, byte[] input, boolean toEncrypt) {
		logger.debug("encrypt");
		StreamCipher engine = getCipherEngine(algorithm);
		if (this.hasError()) {
			return null;
		}


		KeyParameter keyParam = new KeyParameter(key);
		byte[] output = new byte[input.length];
		try {
			if (SymmetricStreamAlgorithm.usesIV(algorithm, this.error)) {
				ParametersWithIV keyParamWithIV = new ParametersWithIV(keyParam, IV);
				engine.init(toEncrypt, keyParamWithIV);
			} else {
				engine.init(toEncrypt, keyParam);
			}
			engine.processBytes(input, 0, input.length, output, 0);
			return output;
		} catch (Exception e) {
			this.error.setError("SS003", e.getMessage());
			logger.error("encrypt", e);
			return null;
		}
	}
}
