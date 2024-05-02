package com.genexus;

import java.io.File;

public class Compression {
	private String path;
	private CompressionFormat format;
	private String password;
	private CompressionMethod method;
	private DictionarySize dictionarySize;

	public Compression(String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize) {
		this.path = path;
		this.format = format;
		this.password = password;
		this.method = method;
		this.dictionarySize = dictionarySize;
	}

	public void addFile(File file) {
		// Implementation goes here
	}

	public void addFolder(File folder) {
		// Implementation goes here
	}

	public void save() {
		// Implementation goes here
	}

	public void close() {
		// Implementation goes here
	}
}


