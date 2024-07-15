package com.genexus.compression;

public class CompressionMessage {
	private final boolean successfulOperation;
	private final String message;

	public CompressionMessage(boolean successfulCompression, String message) {
		this.successfulCompression = successfulCompression;
		this.message = message;
	}

	public boolean isSuccessfulCompression() {
		return successfulCompression;
	}

	public void setSuccessfulCompression(boolean successfulCompression) {
		this.successfulCompression = successfulCompression;
	}

	public String getMessage() {
		return message;
	}
}