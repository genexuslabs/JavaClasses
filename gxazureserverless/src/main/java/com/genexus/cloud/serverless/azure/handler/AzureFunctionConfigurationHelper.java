package com.genexus.cloud.serverless.azure.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.genexus.cloud.serverless.exception.FunctionConfigurationException;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class AzureFunctionConfigurationHelper {
	private final static ILogger logger = LogManager.initialize(".", AzureFunctionConfiguration.class);
	private final static String FUNCTION_CONFIG_PATH = "gxazmappings.json";
	public static List<AzureFunctionConfiguration> getFunctionsMapConfiguration() throws FunctionConfigurationException {

		File configFile = new File(FUNCTION_CONFIG_PATH);

		if (configFile.exists()) {
			try {
				String jsonConfig = new String(Files.readAllBytes(Paths.get(FUNCTION_CONFIG_PATH)));
				List<AzureFunctionConfiguration> mappings = new ObjectMapper().readValue(jsonConfig, new TypeReference<List<AzureFunctionConfiguration>>(){});
				return mappings;
			} catch (IOException e) {
				logger.error(String.format("Invalid Azure function configuration file: %s. Please check json content.", FUNCTION_CONFIG_PATH), e);
				throw new FunctionConfigurationException(String.format("Please check function configuration. File '%s' should be present", FUNCTION_CONFIG_PATH));
			}
		}
		return null;
	}

	public static String getFunctionConfigurationEntryPoint(String functionName, AzureFunctionConfiguration functionConfiguration)
	{
		return functionConfiguration.getGXClassName();
	}

	public static AzureFunctionConfiguration getAzureFunctionConfiguration(String functionName, List<AzureFunctionConfiguration> mappings)  throws FunctionConfigurationException
	{
		Optional<AzureFunctionConfiguration> config = mappings.stream()
			.filter(c -> functionName.equals(c.getFunctionName()))
			.findFirst();
		AzureFunctionConfiguration configuration = config.orElseThrow(() -> new FunctionConfigurationException(String.format("Configuration not found for Azure function %s at gxazmappings.json.",functionName)));
		return configuration;
	}

}
