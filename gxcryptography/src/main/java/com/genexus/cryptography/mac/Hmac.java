package com.genexus.cryptography.mac;

import com.genexus.cryptography.commons.HmacObject;
import com.genexus.cryptography.hash.Hashing;
import com.genexus.cryptography.hash.utils.HashAlgorithm;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import java.io.InputStream;

public class Hmac extends HmacObject {

	private static final Logger logger = LogManager.getLogger(Hmac.class);

	public Hmac() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	@Override
	public String calculate(String plainText, String password, String algorithm) {
		this.error.cleanError();
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(Hmac.class), "calculate", "plainText", plainText, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Hmac.class), "calculate", "password", password, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Hmac.class), "calculate", "algorithm", algorithm, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		byte[] pass = SecurityUtils.hexaToByte(password, this.error);
		HashAlgorithm hashAlgorithm = HashAlgorithm.getHashAlgorithm(algorithm, this.error);
		InputStream input = SecurityUtils.stringToStream(plainText, this.error);
		if (this.hasError()) {
			return "";
		}

		byte[] resBytes = calculate(input, pass, hashAlgorithm);
		return this.hasError() ? "" : Hex.toHexString(resBytes);
	}

	@Override
	public boolean verify(String plainText, String password, String mac, String algorithm) {
		logger.debug("verify");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(Hmac.class), "verify", "plainText", plainText, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Hmac.class), "verify", "password", password, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Hmac.class), "verify", "algorithm", algorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Hmac.class), "verify", "mac", mac, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		// INPUT VERIFICATION - END

		String res = calculate(plainText, password, algorithm);
		return SecurityUtils.compareStrings(res, mac);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	private byte[] calculate(InputStream input, byte[] password, HashAlgorithm algorithm) {
		logger.debug("calculate");
		Digest digest = new Hashing().createHash(algorithm);
		if (this.hasError()) {
			return null;
		}
		byte[] buffer = new byte[8192];
		int n;
		HMac engine = new HMac(digest);

		try {
			engine.init(new KeyParameter(password));
			byte[] resBytes = new byte[engine.getMacSize()];
			while ((n = input.read(buffer)) > 0) {
				engine.update(buffer, 0, n);
			}
			engine.doFinal(resBytes, 0);
			return resBytes;
		} catch (Exception e) {
			this.error.setError("HM002", e.getMessage());
			logger.error("calculate", e);
			return null;
		}
	}

}
