package com.genexus.messaging.queue.model;

import com.genexus.util.GXProperties;

public class SendMessageResult extends MessageId {
	public static int FAILED = 2;
	public static int SENT = 1;
	public static int PENDING = 0;

	private int messageSentStatus = PENDING;

	public int getMessageSentStatus() {
		return messageSentStatus;
	}

	public void setMessageSentStatus(int messageSentStatus) {
		this.messageSentStatus = messageSentStatus;
	}
}


