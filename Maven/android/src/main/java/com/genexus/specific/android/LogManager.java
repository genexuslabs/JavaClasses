package com.genexus.specific.android;

import com.genexus.common.interfaces.IExtensionLogManager;
import com.genexus.diagnostics.core.ILogger;

public class LogManager implements IExtensionLogManager {

	@Override
	public boolean initialize(String logBasePath) {
		return true;
	}

	@Override
	public ILogger getLogger(Class<?> clazz) {
		return new com.genexus.diagnostics.core.provider.AndroidLogger();
	}

	@Override
	public ILogger getLogger(String className) {
		return new com.genexus.diagnostics.core.provider.AndroidLogger();
	}

}
