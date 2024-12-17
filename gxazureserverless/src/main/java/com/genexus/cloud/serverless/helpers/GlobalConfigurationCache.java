package com.genexus.cloud.serverless.helpers;

import com.genexus.cloud.serverless.azure.handler.AzureFunctionConfiguration;
import com.genexus.cloud.serverless.azure.handler.AzureFunctionConfigurationHelper;
import com.genexus.cloud.serverless.exception.FunctionConfigurationException;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.LogManager;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalConfigurationCache {

	private final static ILogger logger = LogManager.initialize(".", GlobalConfigurationCache.class);
	private static final ConcurrentHashMap<String, String> functionConfigCache = new ConcurrentHashMap<>();
	private static boolean initialized = false;
	private static volatile GlobalConfigurationCache CONFIGURATION_CACHE;

	private GlobalConfigurationCache() throws FunctionConfigurationException {
		initializeCache();
	}
	
	public static GlobalConfigurationCache getInstance() throws FunctionConfigurationException {
		GlobalConfigurationCache globalConfigurationCache = CONFIGURATION_CACHE;
		if (globalConfigurationCache == null) {
			synchronized (GlobalConfigurationCache.class) {
				if (globalConfigurationCache == null) {
					CONFIGURATION_CACHE = globalConfigurationCache = new GlobalConfigurationCache();
				}
			}
		}
		return globalConfigurationCache;
	}
	private synchronized void initializeCache() throws FunctionConfigurationException {
		if (!initialized) {
			List<AzureFunctionConfiguration> mappings = AzureFunctionConfigurationHelper.getFunctionsMapConfiguration();
			if (mappings != null) {
				for (AzureFunctionConfiguration azureFunctionConfiguration : mappings) {
					if (azureFunctionConfiguration.isValidConfiguration())
						functionConfigCache.put(azureFunctionConfiguration.getFunctionName(), azureFunctionConfiguration.getGXClassName());
				}
				initialized = true;
			}
			else
			{
				logger.error(String.format("Global configuration cache could not be initialized."));
			}
		}
	}
	public String getData(String functionName) {
		return functionConfigCache.get(functionName);
	}
	public AzureFunctionConfiguration getAzureFunctionConfiguration(String functionName) {
		return new AzureFunctionConfiguration(functionName,functionConfigCache.get(functionName));
	}
}
