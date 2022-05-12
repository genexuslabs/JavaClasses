package com.genexus.cloud.serverless.aws;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventMessageProperty {

	public EventMessageProperty(String pId, String pValue) {
		propertyId = pId;
		propertyValue = pValue;
	}

	@JsonProperty("PropertyId")
	private String propertyId;

	@JsonProperty("PropertyValue")
	private String propertyValue;
}
