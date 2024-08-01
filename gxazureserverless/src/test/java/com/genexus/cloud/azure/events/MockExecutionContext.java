package com.genexus.cloud.azure.events;

import com.microsoft.azure.functions.ExecutionContext;
import java.util.logging.Logger;

public class MockExecutionContext implements ExecutionContext {
	private Logger logger;
	private String functionName;
	private String invocationId;

	public MockExecutionContext(String name, String id) {
		this.logger = Logger.getLogger(MockExecutionContext.class.getName());
		functionName = name;
		invocationId = id;
	}
	@Override
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public String getInvocationId() {
		return invocationId;
	}

	@Override
	public String getFunctionName() {
		return functionName;
	}
}
