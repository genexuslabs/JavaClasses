package com.genexus.cloud.serverless.exception;

public class FunctionRuntimeException extends RuntimeException {

	public FunctionRuntimeException(String errorMessage, Throwable e) {
		super(errorMessage, e);
	}

}
