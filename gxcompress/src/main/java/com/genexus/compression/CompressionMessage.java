package com.genexus.compression;

public class CompressionMessage {
	private final boolean successfulOperation;
	private final String message;

	public CompressionMessage(boolean successfulOperation, String message) {
		this.successfulOperation = successfulOperation;
		this.message = message;
	}

	public boolean isSuccessfulOperation() {
		return successfulOperation;
	}

	public String getMessage() {
		return message;
	}
}