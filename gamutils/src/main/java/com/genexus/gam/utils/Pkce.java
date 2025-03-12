package com.genexus.gam.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.UrlBase64;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

@SuppressWarnings("LoggingSimilarMessage")
public class Pkce {

	private static final Logger logger = LogManager.getLogger(Pkce.class);

	public static String create(int len, String option) {
		logger.trace("create");
		String code_verifier = Random.alphanumeric(len);
		switch (option.toUpperCase().trim()) {
			case "S256":
				byte[] digest = hash(new SHA256Digest(), code_verifier.getBytes(StandardCharsets.UTF_8));
				return MessageFormat.format("{0},{1}", code_verifier.trim(), new String(UrlBase64.encode(digest)));
			case "PLAIN":
				return MessageFormat.format("{0},{1}", code_verifier.trim(), Encoding.toBase64Url(code_verifier.trim()));
			default:
				logger.error("Unknown PKCE option");
				return "";
		}
	}

	public static boolean verify(String code_verifier, String code_challenge, String option) {
		logger.trace("verify");
		switch (option.toUpperCase().trim()) {
			case "S256":
				byte[] digest = hash(new SHA256Digest(), code_verifier.trim().getBytes(StandardCharsets.UTF_8));
				return (new String(UrlBase64.encode(digest))).equals(code_challenge.trim());
			case "PLAIN":
				byte[] bytes_plain = UrlBase64.decode(code_challenge.trim().getBytes(StandardCharsets.UTF_8));
				return new String(bytes_plain).equals(code_verifier.trim());
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
}
