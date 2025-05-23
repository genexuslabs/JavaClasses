package com.genexus.cryptography.mac;

import com.genexus.cryptography.commons.CmacObject;
import com.genexus.cryptography.symmetric.SymmetricBlockCipher;
import com.genexus.cryptography.symmetric.utils.SymmetricBlockAlgorithm;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import java.io.InputStream;

public class Cmac extends CmacObject {

	private static final Logger logger = LogManager.getLogger(Cmac.class);

	public Cmac() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	@Override
	public String calculate(String plainText, String key, String algorithm, int macSize) {
		logger.debug("calculate");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(Cmac.class), "calculate", "plainText", plainText, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Cmac.class), "calculate", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Cmac.class), "calculate", "algorithm", algorithm, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		SymmetricBlockAlgorithm symmetricBlockAlgorithm = SymmetricBlockAlgorithm.getSymmetricBlockAlgorithm(algorithm,
			this.error);
		InputStream input = SecurityUtils.stringToStream(plainText, this.error);
		byte[] byteKey = SecurityUtils.hexaToByte(key, this.error);
		if (this.hasError()) {
			return "";
		}

		SymmetricBlockCipher symCipher = new SymmetricBlockCipher();
		BlockCipher blockCipher = symCipher.getCipherEngine(symmetricBlockAlgorithm);

		if (symCipher.hasError()) {
			this.error = symCipher.getError();
			return "";
		}
		int blockSize = blockCipher.getBlockSize() * 8;

		if (macSize > blockSize) {
			this.error.setError("CM001", "The mac length must be less or equal than the algorithm block size.");
			logger.error("The mac length must be less or equal than the algorithm block size.");
			return "";
		}

		if (blockSize != 64 && blockSize != 128) {
			this.error.setError("CM002", "The block size must be 64 or 128 bits for CMAC. Wrong symmetric algorithm");
			logger.error("The block size must be 64 or 128 bits for CMAC. Wrong symmetric algorithm");
			return "";
		}

		byte[] resBytes = calculate(input, byteKey, macSize, blockCipher);

		return this.hasError() && resBytes != null ? "" : Hex.toHexString(resBytes);

	}

	@Override
	public boolean verify(String plainText, String key, String mac, String algorithm, int macSize) {
		logger.debug("verify");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(Cmac.class), "verify", "plainText", plainText, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Cmac.class), "verify", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Cmac.class), "verify", "mac", mac, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Cmac.class), "verify", "algorithm", algorithm, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		//INPUT VERIFICATION - END

		String res = calculate(plainText, key, algorithm, macSize);
		return SecurityUtils.compareStrings(res, mac);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	private byte[] calculate(InputStream input, byte[] key, int macSize, BlockCipher blockCipher) {
		logger.debug("calculate");
		CipherParameters params = new KeyParameter(key);
		org.bouncycastle.crypto.macs.CMac mac = macSize != 0 ? new CMac(blockCipher, macSize) : new CMac(blockCipher);
		byte[] buffer = new byte[8192];
		int n;

		try {
			mac.init(params);
			byte[] retValue = new byte[mac.getMacSize()];
			while ((n = input.read(buffer)) > 0) {
				mac.update(buffer, 0, n);
			}
			mac.doFinal(retValue, 0);
			return retValue;
		} catch (Exception e) {
			this.error.setError("CM004", e.getMessage());
			logger.error("calculate", e);
			return null;
		}
	}

}
