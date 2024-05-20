package com.genexus.cryptography.mac;

import java.io.InputStream;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import com.genexus.cryptography.commons.HmacObject;
import com.genexus.cryptography.hash.Hashing;
import com.genexus.cryptography.hash.utils.HashAlgorithm;
import com.genexus.securityapicommons.utils.SecurityUtils;

public class Hmac extends HmacObject {

	public Hmac() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	@Override
	public String calculate(String plainText, String password, String algorithm) {
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		SecurityUtils.validateStringInput("password", password, this.error);
		SecurityUtils.validateStringInput("algorithm", algorithm, this.error);
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		byte[] pass = SecurityUtils.hexaToByte(password, this.error);
		HashAlgorithm hashAlgorithm = HashAlgorithm.getHashAlgorithm(algorithm, this.error);
		InputStream input = SecurityUtils.stringToStream(plainText, this.error);
		if (this.hasError()) { return ""; }

		byte[] resBytes = calculate(input, pass, hashAlgorithm);

		return this.hasError() ? "": Hex.toHexString(resBytes);

	}

	@Override
	public boolean verify(String plainText, String password, String mac, String algorithm) {
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		SecurityUtils.validateStringInput("password", password, this.error);
		SecurityUtils.validateStringInput("algorithm", algorithm, this.error);
		SecurityUtils.validateStringInput("mac", mac, this.error);
		if(this.hasError()) { return false;};
		/*******INPUT VERIFICATION - END*******/

		String res = calculate(plainText, password, algorithm);
		return SecurityUtils.compareStrings(res, mac);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	private byte[] calculate(InputStream input, byte[] password, HashAlgorithm algorithm) {

		Digest digest = new Hashing().createHash(algorithm);
		if (this.hasError()) { return null; }

		HMac engine = new HMac(digest);
		try {
			engine.init(new KeyParameter(password));
		} catch (Exception e) {
			this.error.setError("HM001", e.getMessage());
			return null;
		}

		byte[] buffer = new byte[8192];
		int n;
		byte[] resBytes = new byte[engine.getMacSize()];
		try {
			while ((n = input.read(buffer)) > 0) {
				engine.update(buffer, 0, n);
			}
			engine.doFinal(resBytes, 0);
		} catch (Exception e)
		{
			this.error.setError("HM002", e.getMessage());
			return null;
		}

		return resBytes;
	}

}
