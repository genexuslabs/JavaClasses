package com.genexus.messaging.queue.model;

public class DeleteMessageResult extends MessageId {
	public static int FAILED = 2;
	public static int DELETED = 1;
	public static int PENDING = 0;

	private int messageDeleteStatus = PENDING;

	public int getMessageDeleteStatus() {
		return messageDeleteStatus;
	}

	public void setMessageDeleteStatus(int messageDeleteStatus) {
		this.messageDeleteStatus = messageDeleteStatus;
	}
}
