package com.genexus.gam.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Base64;

@SuppressWarnings("LoggingSimilarMessage")
public class Pkce {

	private static final Logger logger = LogManager.getLogger(Pkce.class);

	public static String create(int len, String option) {
		logger.trace("create");
		byte[] code_verifier_bytes = getRandomBytes(len);
		String code_verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(code_verifier_bytes);
		switch (option.toUpperCase().trim()) {
			case "S256":
				byte[] digest = hash(new SHA256Digest(), code_verifier.getBytes(StandardCharsets.US_ASCII));
				return MessageFormat.format("{0},{1}", code_verifier, Base64.getUrlEncoder().withoutPadding().encodeToString(digest));
			case "PLAIN":
				return MessageFormat.format("{0},{1}", code_verifier, code_verifier);
			default:
				logger.error("Unknown PKCE option");
				return "";
		}

	}

	public static boolean verify(String code_verifier, String code_challenge, String option) {
		logger.trace("verify");
		switch (option.toUpperCase().trim()) {
			case "S256":
				byte[] digest = hash(new SHA256Digest(), code_verifier.trim().getBytes(StandardCharsets.US_ASCII));
				return Base64.getUrlEncoder().withoutPadding().encodeToString(digest).equals(code_challenge.trim());
			case "PLAIN":
				return code_challenge.trim().equals(code_verifier.trim());
			default:
				logger.error("Unknown PKCE option");
				return false;
		}
	}

	private static byte[] hash(Digest digest, byte[] inputBytes) {
		byte[] retValue = new byte[digest.getDigestSize()];
		digest.update(inputBytes, 0, inputBytes.length);
		digest.doFinal(retValue, 0);
		return retValue;
	}

	private static byte[] getRandomBytes(int len) {
		logger.trace("getRandomBytes");
		SecureRandom secureRandom = new SecureRandom();
		byte[] bytes = new byte[len];
		secureRandom.nextBytes(bytes);
		return bytes;
	}
}
