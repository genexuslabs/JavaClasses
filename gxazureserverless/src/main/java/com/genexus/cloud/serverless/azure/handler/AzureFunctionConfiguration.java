package com.genexus.cloud.serverless.azure.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genexus.cloud.serverless.ServerlessFunctionConfiguration;
import com.genexus.cloud.serverless.exception.FunctionConfigurationException;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.LogManager;

import java.util.List;
import java.util.Optional;

public class AzureFunctionConfiguration extends ServerlessFunctionConfiguration {
	private final static ILogger logger = LogManager.initialize(".", AzureFunctionConfiguration.class);
	private final static String FUNCTION_CONFIG_PATH = "gxazmappings.json";
	@JsonProperty("FunctionName")
	private String functionName;
	@JsonProperty("GXEntrypoint")
	private String gxEntrypoint;

	public AzureFunctionConfiguration()
	{
	}
	public AzureFunctionConfiguration(String functionName, String gxEntrypoint)
	{
		this.functionName = functionName;
		this.gxEntrypoint = gxEntrypoint;
	}
	public void setFunctionName(String functionName) {

		this.functionName = functionName;
	}
	public void setGXEntrypoint(String gxEntrypoint) {

		this.gxEntrypoint = gxEntrypoint;
	}

	public String getFunctionName() {
		return functionName;
	}
	public String getGXEntrypoint() {

		return gxEntrypoint;
	}
	@Override
	public boolean isValidConfiguration () {
		return functionName != null && !functionName.trim().isEmpty() && gxEntrypoint != null && !gxEntrypoint.trim().isEmpty();
	}

	@Override
	public String getGXClassName() {
		return getGXEntrypoint();
	}

}
