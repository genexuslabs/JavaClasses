package com.genexus.compression;

public class CompressionMessage {
	private boolean successfulCompression;
	private String message;

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

	public void setMessage(String message) {
		this.message = message;
	}
}