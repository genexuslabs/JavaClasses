package com.genexus.gam.utils.test.resources.securityapicommons.commons;

public class Error {

	private boolean exists;
	private String code;
	private String description;

	/**
	 * @return error code string
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return error description string
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Error constructor
	 */
	public Error() {
		this.exists = false;
		this.code = "";
		this.description = "";
	}

	public Error(String code, String description)
	{
		this.code = code;
		this.description = description;
		this.exists = true;
	}

	/**
	 * Set error values
	 *
	 * @param errorCode
	 *            String error internal code
	 * @param errorDescription
	 *            String error internal description
	 */
	public void setError(String errorCode, String errorDescription) {
		this.exists = true;
		this.code = errorCode;
		this.description = errorDescription;

	}

	/**
	 * If an error exists
	 *
	 * @return 1 if an error exists, 0 if not
	 */
	public boolean existsError() {
		return this.exists;
	}

	/**
	 * Sets initial parameters
	 */
	public void cleanError() {
		this.exists = false;
		this.code = "";
		this.description = "";
	}

}