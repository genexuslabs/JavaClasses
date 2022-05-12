package com.genexus.cloud.serverless;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genexus.cloud.serverless.aws.EventMessageProperty;

import java.util.*;

public class EventMessage {
	@JsonProperty("EventMessageId")
	private String messageId;

	@JsonProperty("EventMessageDate")
	private Date messageDate;

	@JsonProperty("EventMessageSourceType")
	private String messageSourceType;

	@JsonProperty("EventMessageData")
	private String messageData;

	@JsonProperty("EventMessageVersion")
	private String messageVersion = "1.0";

	@JsonProperty("EventMessageCustomPayload")
	private List<EventMessageProperty> messageProperties = new ArrayList<>();

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Date getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(Date messageDate) {
		this.messageDate = messageDate;
	}

	public String getMessageSourceType() {
		return messageSourceType;
	}

	public void setMessageSourceType(String messageSourceType) {
		this.messageSourceType = messageSourceType;
	}

	public String getMessageData() {
		return messageData;
	}

	public void setMessageData(String messageData) {
		this.messageData = messageData;
	}

	public String getMessageVersion() {
		return messageVersion;
	}

	public void setMessageVersion(String messageVersion) {
		this.messageVersion = messageVersion;
	}

	public List<EventMessageProperty> getMessageProperties() {
		return messageProperties;
	}

}

