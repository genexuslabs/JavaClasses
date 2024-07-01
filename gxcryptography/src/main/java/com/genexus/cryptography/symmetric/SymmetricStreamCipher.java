package com.genexus.cryptography.symmetric;

import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.engines.HC128Engine;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.engines.ISAACEngine;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.engines.VMPCEngine;
import org.bouncycastle.crypto.engines.XSalsa20Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;

import com.genexus.cryptography.commons.SymmectricStreamCipherObject;
import com.genexus.cryptography.symmetric.utils.SymmetricStreamAlgorithm;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;

/**
 * @author sgrampone
 *
 */
public class SymmetricStreamCipher extends SymmectricStreamCipherObject {

	/**
	 * SymmetricStreamCipher class constructor
	 */
	public SymmetricStreamCipher() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	/**
	 * @param symmetricStreamAlgorithm String SymmetrcStreamAlgorithm enum,
	 *                                 algorithm name
	 * @param symmetricBlockMode       String SymmetricBlockMode enum, mode name
	 * @param key                      String Hexa key for the algorithm excecution
	 * @param IV                       String Hexa IV (nonce) for those algorithms
	 *                                 that uses, ignored if not
	 * @param plainText                String UTF-8 plain text to encrypt
	 * @return String Base64 encrypted text with the given algorithm and parameters
	 */
	public String doEncrypt(String symmetricStreamAlgorithm, String key, String IV, String plainText) {
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("symmetricStreamAlgorithm", symmetricStreamAlgorithm, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		EncodingUtil eu = new EncodingUtil();
		byte[] input = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}

		byte[] encryptedBytes = setUp(symmetricStreamAlgorithm, key, IV, input,  true);
		if(this.hasError()) {return null; }

		return Strings.fromByteArray(Base64.encode(encryptedBytes)).trim();
	}

	/**
	 * @param symmetricStreamAlgorithm String SymmetrcStreamAlgorithm enum,
	 *                                 algorithm name
	 * @param symmetricBlockMode       String SymmetricBlockMode enum, mode name
	 * @param key                      String Hexa key for the algorithm excecution
	 * @param IV                       String Hexa IV (nonce) for those algorithms
	 *                                 that uses, ignored if not
	 * @param encryptedInput           String Base64 encrypted text with the given
	 *                                 algorithm and parameters
	 * @return String plain text UTF-8 with the given algorithm and parameters
	 */
	public String doDecrypt(String symmetricStreamAlgorithm, String key, String IV, String encryptedInput) {
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("symmetricStreamAlgorithm", symmetricStreamAlgorithm, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("encryptedInput", encryptedInput, this.error);
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		byte[] input = null;
		try {
			input = Base64.decode(encryptedInput);
		}catch(Exception e)
		{
			this.error.setError("SS001", e.getMessage());
			return "";
		}

		byte[] decryptedBytes = setUp(symmetricStreamAlgorithm, key, IV, input,  false);
		if(this.hasError()) {return null; }

		EncodingUtil eu = new EncodingUtil();
		String result = eu.getString(decryptedBytes);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		return result.trim();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	/**
	 * @param algorithm SymmetrcStreamAlgorithm enum, algorithm name
	 * @return StreamCipher with the algorithm Stream Engine
	 */
	private StreamCipher getCipherEngine(SymmetricStreamAlgorithm algorithm) {
		StreamCipher engine = null;

		switch (algorithm) {
			case RC4:
				engine = new RC4Engine();
				break;
			case HC256:
				engine = new HC256Engine();
				break;
			case SALSA20:
				engine = new Salsa20Engine();
				break;
			case CHACHA20:
				engine = new ChaChaEngine();
				break;
			case XSALSA20:
				engine = new XSalsa20Engine();
				break;
			case ISAAC:
				engine = new ISAACEngine();
				break;
			default:
				this.error.setError("SS002", "Unrecognized stream cipher algorithm");
				break;
		}
		return engine;

	}

	private byte[] setUp(String symmetricStreamAlgorithm, String key, String IV, byte[] input, boolean toEncrypt)
	{
		byte[] keyBytes = SecurityUtils.hexaToByte(key, this.error);
		byte[] ivBytes = SecurityUtils.hexaToByte(IV, this.error);
		SymmetricStreamAlgorithm algorithm = SymmetricStreamAlgorithm
			.getSymmetricStreamAlgorithm(symmetricStreamAlgorithm, this.error);
		if(this.hasError()) { return null; }

		return encrypt(algorithm, keyBytes, ivBytes, input, toEncrypt);

	}

	private byte[] encrypt(SymmetricStreamAlgorithm algorithm, byte[] key, byte[] IV, byte[] input, boolean toEncrypt)
	{
		StreamCipher engine = getCipherEngine(algorithm);
		if(this.hasError()) { return null; }


		KeyParameter keyParam = new KeyParameter(key);

		try {
			if (SymmetricStreamAlgorithm.usesIV(algorithm, this.error))
			{
				ParametersWithIV keyParamWithIV = new ParametersWithIV(keyParam, IV);
				engine.init(toEncrypt, keyParamWithIV);
			}else {
				engine.init(toEncrypt, keyParam);
			}
		}catch(Exception e) {
			this.error.setError("SS003", e.getMessage());
			return null;
		}


		byte[] output = new byte[input.length];
		try {
			engine.processBytes(input, 0, input.length, output, 0);
		}catch(Exception e) {
			this.error.setError("SS004", e.getMessage());
			return null;
		}
		return output;
	}
}
