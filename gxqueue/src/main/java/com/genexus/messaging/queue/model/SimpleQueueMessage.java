package com.genexus.messaging.queue.model;

import com.genexus.util.GXProperties;

public class SimpleQueueMessage {
	private String messageId;
	private String messageBody;
	private GXProperties messageAttributes = new GXProperties();
	private String messageHandleId;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public GXProperties getMessageAttributes() {
		return messageAttributes;
	}

	public void setMessageAttributes(GXProperties messageAttributes) {
		this.messageAttributes = messageAttributes;
	}

	public String getMessageHandleId() {
		return messageHandleId;
	}

	public void setMessageHandleId(String messageHandleId) {
		this.messageHandleId = messageHandleId;
	}
}
