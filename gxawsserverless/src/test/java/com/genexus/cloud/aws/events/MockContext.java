package com.genexus.cloud.aws.events;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class MockContext implements Context {
	@Override
	public String getAwsRequestId() {
		return "1";
	}

	@Override
	public String getLogGroupName() {
		return "";
	}

	@Override
	public String getLogStreamName() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "";
	}

	@Override
	public String getFunctionVersion() {
		return "";
	}

	@Override
	public String getInvokedFunctionArn() {
		return "";
	}

	@Override
	public CognitoIdentity getIdentity() {
		return null;
	}

	@Override
	public ClientContext getClientContext() {
		return null;
	}

	@Override
	public int getRemainingTimeInMillis() {
		return 200;
	}

	@Override
	public int getMemoryLimitInMB() {
		return 100;
	}

	@Override
	public LambdaLogger getLogger() {
		return null;
	}

}

