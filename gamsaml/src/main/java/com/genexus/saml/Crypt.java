package com.genexus.saml;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class Crypt {
	public static final ILogger logger = LogManager.getLogger(Crypt.class);

	private static Crypt instance = null;

	private Crypt() {
	}

	public static Crypt getInstance() {
		if (instance == null) {
			instance = new Crypt();
		}
		return instance;
	}


	public String Decrypt(String encrypted) {
		logger.debug("[init] - Decrypt - Encrypted = " + encrypted);
		String valor = GamSamlProperties.getKeyCrypt();
		return com.genexus.util.Encryption.decrypt64(encrypted, valor);
	}

	public String Encrypt(String decrypted) {
		String valor = GamSamlProperties.getKeyCrypt();
		return com.genexus.util.Encryption.encrypt64(decrypted, valor);
	}

}