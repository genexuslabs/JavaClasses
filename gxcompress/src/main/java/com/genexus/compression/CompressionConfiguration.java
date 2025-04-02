package com.genexus.compression;

public class CompressionConfiguration {
	public long maxCombinedFileSize = -1;
	public long maxIndividualFileSize = -1;
	public int maxFileCount = -1;
	public String targetDirectory = "";

	public CompressionConfiguration() {}

	public CompressionConfiguration(long maxCombinedFileSize, long maxIndividualFileSize, int maxFileCount, String targetDirectory) {
		this.maxCombinedFileSize = maxCombinedFileSize;
		this.maxIndividualFileSize = maxIndividualFileSize;
		this.maxFileCount = maxFileCount;
		this.targetDirectory = (targetDirectory != null && !targetDirectory.isEmpty()) ? targetDirectory : "";
	}
}
