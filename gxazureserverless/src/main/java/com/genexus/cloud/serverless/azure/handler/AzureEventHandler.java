package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.exception.FunctionConfigurationException;
import com.genexus.cloud.serverless.GXProcedureExecutor;
import com.genexus.cloud.serverless.ServerlessBaseEventHandler;

import java.util.List;

public class AzureEventHandler extends ServerlessBaseEventHandler<AzureFunctionConfiguration> {
	public AzureEventHandler() throws Exception {
		super();
	}
	protected void SetupServerlessMappings(String functionName) throws Exception {
		logger.debug("Initializing Function configuration");
		try {
			if ((functionConfiguration == null) || (!functionConfiguration.isValidConfiguration())) {
				functionConfiguration = getFunctionConfiguration(functionName);
			}
			if (functionConfiguration != null && functionConfiguration.isValidConfiguration())
				entryPointClass = Class.forName(functionConfiguration.getGXClassName());

		} catch (Exception e) {
			logger.error(String.format("Failed to initialize Application"), e);
			throw e;
		}
		if (entryPointClass != null)
			executor = new GXProcedureExecutor(entryPointClass);
		else
		{
			logger.error(String.format("Invalid EntryPoint Class for function"));
			throw new Exception("Failed to initialize Application. Check whether gxazmappings.json is located in the right place, with the correct format, and specifies an entry point for your function.");
		}

	}
	@Override
	protected AzureFunctionConfiguration createFunctionConfiguration(String functionName, String className) {
		return new AzureFunctionConfiguration(functionName,className);
	}

	@Override
	protected AzureFunctionConfiguration createFunctionConfiguration(String className) {
		return new AzureFunctionConfiguration("",className);
	}

	@Override
	protected AzureFunctionConfiguration createFunctionConfiguration() {

		return new AzureFunctionConfiguration();
	}
	@Override
	protected AzureFunctionConfiguration getFunctionConfiguration(String functionName) throws FunctionConfigurationException {
		List<AzureFunctionConfiguration> mappings = AzureFunctionConfigurationHelper.getFunctionsMapConfiguration();
		if (mappings != null)
			return AzureFunctionConfigurationHelper.getAzureFunctionConfiguration(functionName,mappings);
		return null;
	}
	@Override
	protected void InitializeServerlessConfig() throws Exception {
	}

}

