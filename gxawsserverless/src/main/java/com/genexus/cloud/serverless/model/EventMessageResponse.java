package com.genexus.cloud.serverless.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventMessageResponse {
	@JsonProperty("Handled")
	private boolean handled = false;

	@JsonProperty("ErrorMessage")
	private String errorMessage = "";

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
