package com.genexus.cloud.serverless;

import com.genexus.cloud.serverless.exception.FunctionConfigurationException;

public abstract class ServerlessFunctionConfiguration {

	public ServerlessFunctionConfiguration() {}
	public abstract boolean isValidConfiguration();
	public abstract String getGXClassName();
}
