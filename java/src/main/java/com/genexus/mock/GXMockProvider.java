package com.genexus.mock;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class GXMockProvider {
	private static final ILogger logger = LogManager.getLogger(GXMockProvider.class);
	private static IGXMock provider;

	public static IGXMock getProvier() {
		return provider;
	}

	public static void setProvider(IGXMock mockProvider) {
		provider = mockProvider;
		if (mockProvider != null)
			logger.debug("Mock provider: " + mockProvider.getClass().getName());
		else
			logger.debug("Mock provider set to null");
	}
}
