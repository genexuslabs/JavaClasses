package com.genexus.gam.utils.test.resources.securityapicommons.commons;

public abstract class SecurityAPIObject {

	protected Error error;

	/**
	 * SecurityObject constructor
	 */
	public SecurityAPIObject() {
		error = new Error();
	}

	/**
	 * @return Error type
	 */
	public Error getError() {
		return error;
	}

	/**
	 * @return error exists boolean
	 */
	public boolean hasError() {
		return error.existsError();
	}

	/**
	 * @return error code string
	 */
	public String getErrorCode() {
		return error.getCode();
	}

	/**
	 * @return error description string
	 */
	public String getErrorDescription() {
		return error.getDescription();
	}


}