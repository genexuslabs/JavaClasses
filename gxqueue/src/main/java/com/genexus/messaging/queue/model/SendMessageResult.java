package com.genexus.messaging.queue.model;

public class SendMessageResult extends MessageId {
	public static String DELETED = "Deleted";
	public static String FAILED = "Failed";
	public static String SENT = "Sent";
	public static String UNKNOWN = "Unknown";

	private String messageSentStatus = UNKNOWN;

	public String getMessageSentStatus() {
		return messageSentStatus;
	}

	public void setMessageSentStatus(String messageSentStatus) {
		this.messageSentStatus = messageSentStatus;
	}
}
