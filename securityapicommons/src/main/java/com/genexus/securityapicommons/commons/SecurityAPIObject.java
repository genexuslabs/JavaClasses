package com.genexus.securityapicommons.commons;

public abstract class SecurityAPIObject {

	protected Error error;

	public SecurityAPIObject() {
		error = new Error();
	}

	public Error getError() {
		return error;
	}

	public boolean hasError() {
		return error.existsError();
	}

	public String getErrorCode() {
		return error.getCode();
	}

	public String getErrorDescription() {
		return error.getDescription();
	}


}
