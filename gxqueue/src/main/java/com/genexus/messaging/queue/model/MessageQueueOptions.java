package com.genexus.messaging.queue.model;

public class MessageQueueOptions {
	private short maxNumberOfMessages;
	private boolean deleteConsumedMessages;
	private int waitTimeout;
	private int visibilityTimeout;
	private int timetoLive;
	private int delaySeconds;
	private String receiveRequestAttemptId = "";
	private boolean receiveMessageAttributes;

	public short getMaxNumberOfMessages() {
		return maxNumberOfMessages;
	}

	public void setMaxNumberOfMessages(short maxNumberOfMessages) {
		this.maxNumberOfMessages = maxNumberOfMessages;
	}

	public boolean isDeleteConsumedMessages() {
		return deleteConsumedMessages;
	}

	public void setDeleteConsumedMessages(boolean deleteConsumedMessages) {
		this.deleteConsumedMessages = deleteConsumedMessages;
	}

	public int getWaitTimeout() {
		return waitTimeout;
	}

	public void setWaitTimeout(int waitTimeout) {
		this.waitTimeout = waitTimeout;
	}

	public int getVisibilityTimeout() {
		return visibilityTimeout;
	}

	public void setVisibilityTimeout(int visibilityTimeout) {
		this.visibilityTimeout = visibilityTimeout;
	}

	public int getTimetoLive() {
		return timetoLive;
	}

	public void setTimetoLive(int timetoLive) {
		this.timetoLive = timetoLive;
	}

	public int getDelaySeconds() {
		return delaySeconds;
	}

	public void setDelaySeconds(int delaySeconds) {
		this.delaySeconds = delaySeconds;
	}

	public String getReceiveRequestAttemptId() {
		return receiveRequestAttemptId;
	}

	public void setReceiveRequestAttemptId(String receiveRequestAttemptId) {
		this.receiveRequestAttemptId = receiveRequestAttemptId;
	}

	public boolean isReceiveMessageAttributes() {
		return receiveMessageAttributes;
	}

	public void setReceiveMessageAttributes(boolean receiveMessageAttributes) {
		this.receiveMessageAttributes = receiveMessageAttributes;
	}
}
