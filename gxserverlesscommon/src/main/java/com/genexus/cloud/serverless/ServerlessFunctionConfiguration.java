package com.genexus.cloud.serverless;

public abstract class ServerlessFunctionConfiguration {
	public ServerlessFunctionConfiguration() {}
	public abstract boolean isValidConfiguration();
	public abstract String getGXClassName();
}
