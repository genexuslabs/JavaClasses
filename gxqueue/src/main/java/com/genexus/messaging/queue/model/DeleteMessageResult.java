package com.genexus.messaging.queue.model;

public class DeleteMessageResult extends MessageId {
	public static String DELETED = "Deleted";

	private String messageDeleteStatus = UNKNOWN;

	public String getMessageDeleteStatus() {
		return messageDeleteStatus;
	}

	public void setMessageDeleteStatus(String messageDeleteStatus) {
		this.messageDeleteStatus = messageDeleteStatus;
	}
}
