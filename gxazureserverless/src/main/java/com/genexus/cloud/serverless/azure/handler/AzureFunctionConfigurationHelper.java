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
				return new ObjectMapper().readValue(jsonConfig, new TypeReference<List<AzureFunctionConfiguration>>(){});
			} catch (IOException e) {
				logger.error(String.format("Invalid Azure function configuration file: %s. Please check json content.", FUNCTION_CONFIG_PATH), e);
				throw new FunctionConfigurationException(String.format("JSON contents of file '%s' are not valid.", FUNCTION_CONFIG_PATH));
			}
		}
		else {
			logger.error(String.format("Failure while trying to read Azure function configuration file: %s.", FUNCTION_CONFIG_PATH));
			throw new FunctionConfigurationException(String.format("File %s not found. The file is attempted to be read when there is no GX_AZURE_<FUNCTIONNAME>_CLASS environment variable pointing to the GeneXus class associated with the function.", FUNCTION_CONFIG_PATH));
		}
	}

	public static String getFunctionConfigurationEntryPoint(String functionName, AzureFunctionConfiguration functionConfiguration) {
		if (functionConfiguration != null)
			return functionConfiguration.getGXClassName();
		else return null;
	}

	public static AzureFunctionConfiguration getAzureFunctionConfiguration(String functionName, List<AzureFunctionConfiguration> mappings)  throws FunctionConfigurationException {
		Optional<AzureFunctionConfiguration> config = Optional.empty();
		if (mappings != null) {
			config = mappings.stream()
				.filter(c -> functionName.equals(c.getFunctionName()))
				.findFirst();
		}
		return config.orElseThrow(() -> new FunctionConfigurationException(String.format("Configuration not found for Azure function %s at gxazmappings.json.", functionName)));
	}
}
