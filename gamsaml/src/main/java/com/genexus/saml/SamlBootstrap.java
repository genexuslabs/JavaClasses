package com.genexus.saml;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.opensaml.core.config.InitializationService;


public class SamlBootstrap {

	public static final ILogger logger = LogManager.getLogger(SamlBootstrap.class);

	private static SamlBootstrap instance = null;

	private SamlBootstrap() throws GamSamlException {
		try {
			InitializationService.initialize();
		} catch (Exception ex) {
			logger.error("[constructor] ", ex);
			throw new GamSamlException(ex);
		}
	}

	public static SamlBootstrap getInstance() throws GamSamlException {
		logger.debug("[getInstance]");
		if (instance == null) {
			instance = new SamlBootstrap();
		}
		return instance;
	}

}