package com.genexus.compression;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Compression {
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Compression.class);

	private String path;
	private String format;
	private List<File> filesToCompress;

	public Compression() {}

	public Compression(String path, String format) {
		this.path = path;
		this.format = format;
		this.filesToCompress = new ArrayList<>();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void addFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			filesToCompress.add(file);
		} else {
			log.error("File does not exist: {}", file.getAbsolutePath());
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

	public int save() {
		if (filesToCompress.isEmpty()) {
			log.error("No files have been added for compression.");
			return -4;
		}
		Vector<String> paths = new Vector<>();
		for (File file : filesToCompress) {
			paths.add(file.getPath());
		}
		return GXCompressor.compressFiles(paths, path, format);
	}


	public void clear() {
		this.path = "";
		this.format = "";
		this.filesToCompress = new ArrayList<>();
	}
}