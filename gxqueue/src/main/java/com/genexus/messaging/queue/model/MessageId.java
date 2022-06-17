package com.genexus.messaging.queue.model;

public abstract class MessageId {
	public static String FAILED = "Failed";
	public static String UNKNOWN = "Unknown";

	private String messageId;
	private String messageServerId;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageServerId() {
		return messageServerId;
	}

	public void setMessageServerId(String messageServerId) {
		this.messageServerId = messageServerId;
	}

}
