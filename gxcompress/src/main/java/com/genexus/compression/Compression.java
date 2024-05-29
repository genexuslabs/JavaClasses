package com.genexus.compression;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Compression {
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Compression.class);

	private String path;
	private String format;
	private int dictionarySize;
	private List<File> filesToCompress;

	public Compression() {}

	public Compression(String path, String format, int dictionarySize) {
		this.path = path;
		this.format = format;
		this.dictionarySize = dictionarySize;
		this.filesToCompress = new ArrayList<>();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setDictionarySize(int dictionarySize) {
		this.dictionarySize = dictionarySize;
	}

	public void addFile(File file) {
		if (file.exists()) {
			filesToCompress.add(file);
		} else {
			log.error("File does not exist: {}", file.getAbsolutePath());
		}
	}

	public void addFolder(File folder) {
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						addFolder(file);
					} else {
						addFile(file);
					}
				}
			}
		} else {
			log.error("Folder does not exist or is not a directory: {}", folder.getAbsolutePath());
		}
	}

	public int save() {
		if (filesToCompress.isEmpty()) {
			log.error("No files have been added for compression.");
			return -3;
		}
		File[] filesArray = filesToCompress.toArray(new File[0]);
		int compressionResult;
		try {
			compressionResult = GXCompressor.compress(filesArray, path, format, dictionarySize);
		} catch (IllegalArgumentException e) {
			compressionResult = -1;
			log.error("Compression failed: {}", e.getMessage());
		}
		return compressionResult;
	}

	public void close() {
		this.path = "";
		this.format = "";
		this.dictionarySize = 0;
		this.filesToCompress = new ArrayList<>();
	}
}