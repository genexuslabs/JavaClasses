package com.genexus.messaging.queue.model;

public class DeleteMessageResult extends MessageId {
	public static String DELETED = "Deleted";
	public static String FAILED = "Failed";
	public static String SENT = "Sent";
	public static String UNKNOWN = "Unknown";

	private String messageDeleteStatus = UNKNOWN;

	public String getMessageDeleteStatus() {
		return messageDeleteStatus;
	}

	public void setMessageDeleteStatus(String messageDeleteStatus) {
		this.messageDeleteStatus = messageDeleteStatus;
	}
}
