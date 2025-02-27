package com.genexus.gam.utils.cryptography;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class Encryption {

	private static Logger logger = LogManager.getLogger(Encryption.class);

	public static String AesGcm(String input, String key, String nonce, int macSize, boolean toEncrypt) {
		return toEncrypt ? Base64.toBase64String(internal_AesGcm(input.getBytes(StandardCharsets.UTF_8), key, nonce, macSize, toEncrypt)) : new String(internal_AesGcm(Base64.decode(input), key, nonce, macSize, toEncrypt), StandardCharsets.UTF_8);
	}

	public static byte[] internal_AesGcm(byte[] inputBytes, String key, String nonce, int macSize, boolean toEncrypt) {
		logger.debug("internal_AesGcm");
		AEADBlockCipher cipher = new GCMBlockCipher(new AESEngine());
		AEADParameters AEADparams = new AEADParameters(new KeyParameter(Hex.decode(key)), macSize, Hex.decode(nonce));
		try {
			cipher.init(toEncrypt, AEADparams);
			byte[] outputBytes = new byte[cipher.getOutputSize(inputBytes.length)];
			int length = cipher.processBytes(inputBytes, 0, inputBytes.length, outputBytes, 0);
			cipher.doFinal(outputBytes, length);
			return outputBytes;
		} catch (Exception e) {
			logger.error("Aes_gcm", e);
			return null;
		}
	}
}
