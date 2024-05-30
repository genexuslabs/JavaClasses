package com.genexus.saml;

import java.security.SecureRandom;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.opensaml.core.config.InitializationService;


public class SamlBootstrap {

	public static final ILogger logger = LogManager.getLogger(SamlBootstrap.class);

	private static SamlBootstrap instance = null;


	private static final SecureRandom iDGenerator = new SecureRandom();

	private SamlBootstrap() throws GamSamlException {

		try {
			InitializationService.initialize();
			byte[] idBytes = new byte[16];
			iDGenerator.nextBytes(idBytes);
			logger.debug("[constructor] iDGenerator: " + iDGenerator.toString());
		} catch (Exception ex) {
			logger.error("[constructor] ", ex);

			throw new GamSamlException(ex);
		}

	}


	public SecureRandom getIdGenerator() {
		return iDGenerator;
	}


	public static SamlBootstrap getInstance() throws GamSamlException {
		logger.debug("[getInstance]");
		if (instance == null) {
			instance = new SamlBootstrap();
		}
		return instance;
	}

}