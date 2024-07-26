package com.genexus.gam.utils;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;

public class Hash {
	public static final ILogger logger = LogManager.getLogger(Hash.class);

	public static String sha512(String plainText) {
		if (plainText.isEmpty()) {
			logger.error("sha512 plainText is empty");
			return "";
		}
		byte[] inputBytes = plainText.getBytes(StandardCharsets.UTF_8);
		Digest alg = new SHA512Digest();
		byte[] retValue = new byte[alg.getDigestSize()];
		alg.update(inputBytes, 0, inputBytes.length);
		alg.doFinal(retValue, 0);
		return Base64.toBase64String(retValue);
	}
}