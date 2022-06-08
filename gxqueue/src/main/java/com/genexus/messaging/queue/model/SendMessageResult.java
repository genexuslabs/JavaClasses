package com.genexus.messaging.queue.model;

import com.genexus.util.GXProperties;

public class SendMessageResult {
	public static int FAILED = 2;
	public static int SENT = 1;
	public static int PENDING = 0;

	private String messageId;
	private String messageServerId;
	private int messageSentStatus = PENDING;

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

	public int getMessageSentStatus() {
		return messageSentStatus;
	}

	public void setMessageSentStatus(int messageSentStatus) {
		this.messageSentStatus = messageSentStatus;
	}
}


