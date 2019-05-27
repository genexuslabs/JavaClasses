package com.genexus.specific.java;

import com.genexus.common.interfaces.IExtensionLogManager;
import com.genexus.diagnostics.core.ILogger;

public class LogManager implements IExtensionLogManager {
	static {
		configure(".");
	}

	public static void configure(String logBasePath) {
		org.apache.logging.log4j.core.lookup.MapLookup.setMainArguments(new String[] {logBasePath});
		//If we do not disable StatusLogger => Log4J throws message logging when log4j.xml is not found
		org.apache.logging.log4j.status.StatusLogger.getLogger().setLevel(org.apache.logging.log4j.Level.OFF);
	}

	@Override
	public ILogger getLogger(Class<?> clazz) {
		return new com.genexus.diagnostics.core.provider.Log4J2Logger(clazz);
	}

	@Override
	public ILogger getLogger(String className) {
		return new com.genexus.diagnostics.core.provider.Log4J2Logger(className);
	}
}
