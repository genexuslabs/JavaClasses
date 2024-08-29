package com.genexus.gam.utils.cryptography;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;

public class Hash {

	private static Logger logger = LogManager.getLogger(Hash.class);

	public static String sha512(String plainText) {
		logger.debug("sha512");
		return internalHash(new SHA512Digest(), plainText);
	}

	public static String sha256(String plainText)
	{
		logger.debug("sha256");
		return internalHash(new SHA256Digest(), plainText);
	}

	private static String internalHash(Digest digest, String plainText)
	{
		logger.debug("internalHash");
		if (plainText.isEmpty()) {
			logger.error("hash plainText is empty");
			return "";
		}
		byte[] inputBytes = plainText.getBytes(StandardCharsets.UTF_8);
		byte[] retValue = new byte[digest.getDigestSize()];
		digest.update(inputBytes, 0, inputBytes.length);
		digest.doFinal(retValue, 0);
		return Base64.toBase64String(retValue);
	}
}