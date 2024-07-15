package com.genexus.compression;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Vector;

public class Compression {
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Compression.class);

	private String path;
	private String format;
	private Vector<String> filesToCompress;

	public Compression() {}

	public Compression(String path, String format) {
		this.path = path;
		this.format = format;
		this.filesToCompress = new Vector<>();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void addFile(String filePath) {
		if (new File(filePath).exists()) {
			filesToCompress.add(filePath);
		} else {
			log.error("File does not exist: {}", filePath);
		}
	}

	public void addFolder(String folderPath) {
		File folder = new File(folderPath);
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						addFolder(file.getAbsolutePath());
					} else {
						addFile(file.getAbsolutePath());
					}
				}
			}
		} else {
			log.error("Folder does not exist or is not a directory: {}", folder.getAbsolutePath());
		}
	}

	public CompressionMessage save() {
		if (filesToCompress.isEmpty()) {
			log.error("No files have been added for compression.");
			return new CompressionMessage(false, "No files have been added for compression.");
		}
		return GXCompressor.compressFiles(filesToCompress, path, format);
	}


	public void clear() {
		this.path = "";
		this.format = "";
		this.filesToCompress = new Vector<>();
	}
}
