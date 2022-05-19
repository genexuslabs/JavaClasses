package com.genexus.cloud.serverless.aws.handler;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LambdaFunctionConfiguration {

	@JsonProperty("entryPointClassName")
	private String entryPointClassName = null;

	public LambdaFunctionConfiguration() {

	}
	public LambdaFunctionConfiguration(String entryPointClassName) {
		this.entryPointClassName = entryPointClassName;
	}

	public String getEntryPointClassName() {
		return entryPointClassName;
	}

	public void setEntryPointClassName(String entryPointClassName) {
		this.entryPointClassName = entryPointClassName;
	}

	public boolean isValidConfiguration () {
		return getEntryPointClassName() != null;
	}
}
