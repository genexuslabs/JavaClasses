package com.genexus.compression;

public class CompressionMessage {
	private boolean successfulOperation;
	private String message;

	public CompressionMessage(boolean successfulOperation, String message) {
		this.successfulOperation = successfulOperation;
		this.message = message;
	}

	public boolean isSuccessfulOperation() {
		return successfulOperation;
	}

	public void setSuccessfulCompression(boolean successfulOperation) {
		this.successfulOperation = successfulOperation;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}