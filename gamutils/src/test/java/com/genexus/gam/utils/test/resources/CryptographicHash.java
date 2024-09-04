package com.genexus.gam.utils.test.resources;

import org.apache.commons.codec.CharEncoding;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptographicHash {
	private MessageDigest alg = null;

	// private HashAlgorithm alg;
	public CryptographicHash(String algorithm) {
		if (algorithm.equals("SHA512"))
			algorithm = "SHA-512";
		// Supports algorithm = {MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512}
		try {
			alg = MessageDigest.getInstance(algorithm); // step 2
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public String ComputeHash(String data) {
		try {
			if (alg == null) alg = MessageDigest.getInstance("SHA-512");
			alg.update(data.getBytes(CharEncoding.UTF_8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		byte[] bin = alg.digest();
		org.apache.commons.codec.binary.Base64 base = new org.apache.commons.codec.binary.Base64(100000);
		return base.encodeToString(bin).trim();
	}
}
