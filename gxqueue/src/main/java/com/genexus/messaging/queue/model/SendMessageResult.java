package com.genexus.messaging.queue.model;

public class SendMessageResult extends MessageId {
	public static String SENT = "Sent";


	private String messageSentStatus = UNKNOWN;

	public String getMessageSentStatus() {
		return messageSentStatus;
	}

	public void setMessageSentStatus(String messageSentStatus) {
		this.messageSentStatus = messageSentStatus;
	}
}
