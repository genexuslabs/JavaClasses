package com.genexus.diagnostics.core;

import com.genexus.common.interfaces.SpecificImplementation;

public class LogManager {

	private static boolean _initialized = false;

	public static boolean initialize(String logBasePath) {
		if (!_initialized)
		{
			if (SpecificImplementation.LogManager != null)
				_initialized = SpecificImplementation.LogManager.initialize(logBasePath);
			else
				_initialized = true;
		}
		return _initialized;
	}

	public static ILogger getLogger(final Class<?> clazz) {
		return SpecificImplementation.LogManager.getLogger(clazz);
	}

	public static ILogger getLogger(String className) {
		return SpecificImplementation.LogManager.getLogger(className);
	}
}
