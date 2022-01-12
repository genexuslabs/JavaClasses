package com.genexus.diagnostics.core;

import com.genexus.common.interfaces.SpecificImplementation;

public class LogManager {

	public static ILogger getLogger(final Class<?> clazz) {
		return SpecificImplementation.LogManager.getLogger(clazz);
	}

	public static ILogger getLogger(String className) {
		return SpecificImplementation.LogManager.getLogger(className);
	}
}
