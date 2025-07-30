package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.GXProcedureExecutor;
import com.genexus.cloud.serverless.ServerlessBaseEventHandler;
import com.genexus.cloud.serverless.exception.FunctionConfigurationException;
import com.genexus.cloud.serverless.helpers.GlobalConfigurationCache;

public class AzureEventHandler extends ServerlessBaseEventHandler<AzureFunctionConfiguration> {
	public AzureEventHandler() throws Exception {
		super();
	}
	protected void setupServerlessMappings(String functionName) throws FunctionConfigurationException, ClassNotFoundException {
		logger.debug("Initializing Function configuration");

		//First use Environment variable, then try reading from the gxazmappings.json file
		String envvar = String.format("GX_AZURE_%s_CLASS", functionName.trim().toUpperCase());
		if (System.getenv(envvar) != null) {
			String gxObjectClassName = System.getenv(envvar);
			entryPointClass = Class.forName(gxObjectClassName);
			functionConfiguration = new AzureFunctionConfiguration(functionName, gxObjectClassName);
		}
		else {
			try {
				functionConfiguration = GlobalConfigurationCache.getInstance().getAzureFunctionConfiguration(functionName);
				entryPointClass = Class.forName(functionConfiguration.getGXClassName());
			} catch (Exception e) {
				logger.error(String.format("Failed to initialize Application configuration for %s", functionName), e);
				throw e;
			}
		}
		if (entryPointClass != null)
			executor = new GXProcedureExecutor(entryPointClass);
		else {
			logger.error(String.format("GeneXus Entry point class for function %s was not specified. Set %s Environment Variable.",functionName,envvar));
			throw new FunctionConfigurationException(String.format("GeneXus Entry point class for function %s was not specified. Set %s Environment Variable.",functionName,envvar));
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
	protected void InitializeServerlessConfig(){
	}
}

