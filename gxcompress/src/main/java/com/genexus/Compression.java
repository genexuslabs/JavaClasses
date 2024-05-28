package com.genexus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Compression {
	private String path;
	private CompressionFormat format;
	private String password;
	private CompressionMethod method;
	private DictionarySize dictionarySize;

	private List<File> filesToCompress;

	public Compression() {}

	public Compression(String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize) {
		this.path = path;
		this.format = format;
		this.password = password;
		this.method = method;
		this.dictionarySize = dictionarySize;
		this.filesToCompress = new ArrayList<>();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setFormat(CompressionFormat format) {
		this.format = format;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setMethod(CompressionMethod method) {
		this.method = method;
	}

	public void setDictionarySize(DictionarySize dictionarySize) {
		this.dictionarySize = dictionarySize;
	}

	public void addFile(File file) {
		if (file.exists()) {
			filesToCompress.add(file);
		} else {
			System.out.println("File does not exist: " + file.getAbsolutePath());
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
			System.out.println("Folder does not exist or is not a directory: " + folder.getAbsolutePath());
		}
	}

	public void save() {
		if (filesToCompress.isEmpty()) {
			System.out.println("No files have been added for compression.");
			return;
		}
		File[] filesArray = filesToCompress.toArray(new File[0]);
		try {
			GXCompressor.compress(filesArray, path, format, password, method, dictionarySize);
			System.out.println("Compression successful to: " + path);
		} catch (IllegalArgumentException e) {
			System.err.println("Compression failed: " + e.getMessage());
		}
	}

	public void close() {
		filesToCompress.clear();
	}
}