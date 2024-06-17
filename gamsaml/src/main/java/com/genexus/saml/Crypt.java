package com.genexus.saml;

public class Crypt {
	public static String Decrypt(String encrypted) {
		String value = GamSamlProperties.getKeyCrypt();
		return com.genexus.util.Encryption.decrypt64(encrypted, value);
	}

	public static String Encrypt(String decrypted) {
		String value = GamSamlProperties.getKeyCrypt();
		return com.genexus.util.Encryption.encrypt64(decrypted, value);
	}

}