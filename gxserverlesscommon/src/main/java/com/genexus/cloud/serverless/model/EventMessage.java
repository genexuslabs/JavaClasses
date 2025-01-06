package com.genexus.cloud.serverless.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventMessage {
	@JsonProperty("EventMessageId")
	private String messageId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
	@JsonProperty("EventMessageDate")
	private Date messageDate;

	@JsonProperty("EventMessageSourceType")
	private String messageSourceType;

	@JsonProperty("EventMessageData")
	private String messageData;

	@JsonProperty("EventMessageVersion")
	private String messageVersion = "1.0";

	@JsonProperty("EventMessageProperties")
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

