package com.genexus.common.interfaces;

import com.genexus.diagnostics.core.ILogger;

public interface IExtensionLogManager {

	ILogger getLogger(Class<?> clazz);

	ILogger getLogger(String className);

}
