package com.genexus.diagnostics.core;

import com.genexus.common.interfaces.SpecificImplementation;

public class LogManager {

	public static ILogger getLogger(final Class<?> clazz) {
		if (SpecificImplementation.LogManager != null)
			return SpecificImplementation.LogManager.getLogger(clazz);

		return null;
	}

	public static ILogger getLogger(String className) {
		if (SpecificImplementation.LogManager != null)
			return SpecificImplementation.LogManager.getLogger(className);

		return null;
	}
}
