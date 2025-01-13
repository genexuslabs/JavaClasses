package com.genexus.cloud.serverless.azure.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genexus.cloud.serverless.ServerlessFunctionConfiguration;

public class AzureFunctionConfiguration extends ServerlessFunctionConfiguration {

	@JsonProperty("FunctionName")
	private String functionName;
	@JsonProperty("GXEntrypoint")
	private String gxEntrypoint;

	public AzureFunctionConfiguration() {
	}
	public AzureFunctionConfiguration(String functionName, String gxEntrypoint) {
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
