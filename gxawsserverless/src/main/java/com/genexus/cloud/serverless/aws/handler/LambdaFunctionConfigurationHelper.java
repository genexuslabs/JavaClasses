package com.genexus.cloud.serverless.aws.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.exception.FunctionConfigurationException;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LambdaFunctionConfigurationHelper {
	private static ILogger logger = LogManager.initialize(".", LambdaFunctionConfiguration.class);
	private static String ENTRY_POINT_CLASS_NAME_VAR = "GX_MAIN_CLASS_NAME";
	private static String FUNCTION_CONFIG_PATH = "gx-awslambda-function.json";

	public static LambdaFunctionConfiguration getFunctionConfiguration() throws FunctionConfigurationException {
		File configFile = new File(FUNCTION_CONFIG_PATH);
		LambdaFunctionConfiguration config = null;

		if (configFile.exists()) {
			try {
				String jsonConfig = new String(Files.readAllBytes(Paths.get(FUNCTION_CONFIG_PATH)));
				config = new ObjectMapper().readValue(jsonConfig, LambdaFunctionConfiguration.class);
			} catch (IOException e) {
				logger.error(String.format("Invalid lambda function configuration file: %s. Please check json content.", FUNCTION_CONFIG_PATH), e);
			}
		}
		if (config == null) {
			config = new LambdaFunctionConfiguration();
		}

		if (System.getenv(ENTRY_POINT_CLASS_NAME_VAR) != null) {
			config.setEntryPointClassName(System.getenv(ENTRY_POINT_CLASS_NAME_VAR));
		}

		if (!config.isValidConfiguration()) {
			throw new FunctionConfigurationException(String.format("Please check function configuration. Either file '%s' should be present, or '%s' Environment Variable must be defined", FUNCTION_CONFIG_PATH, ENTRY_POINT_CLASS_NAME_VAR));
		}
		return config;
	}

}
