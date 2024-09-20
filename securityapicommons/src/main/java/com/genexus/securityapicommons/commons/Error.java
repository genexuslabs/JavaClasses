
package com.genexus.securityapicommons.commons;

public class Error {

	private boolean exists;
	private String code;
	private String description;

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public Error() {
		this.exists = false;
		this.code = "";
		this.description = "";
	}

	public Error(String code, String description) {
		this.code = code;
		this.description = description;
		this.exists = true;
	}

	public void setError(String errorCode, String errorDescription) {
		this.exists = true;
		this.code = errorCode;
		this.description = errorDescription;

	}

	public boolean existsError() {
		return this.exists;
	}

	public void cleanError() {
		this.exists = false;
		this.code = "";
		this.description = "";
	}

}
