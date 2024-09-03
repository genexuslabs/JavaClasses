package com.genexus.cloud.serverless.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventMessageResponse {
	@JsonProperty("HandleFailure")
	private boolean hasFailed = false;

	@JsonProperty("ErrorMessage")
	private String errorMessage = "";

	public boolean hasFailed() {
		return hasFailed;
	}

	public void setAsFailed(boolean hasFailed) {
		this.hasFailed = hasFailed;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
