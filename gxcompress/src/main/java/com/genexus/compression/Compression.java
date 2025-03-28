package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class Compression {
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Compression.class);

	private String absolutePath;
	private long maxCombinedFileSize;
	private GXBaseCollection<SdtMessages_Message>[] messages;
	private ArrayList<String> filesToCompress;

	public Compression() {}

	public Compression(String absolutePath, long maxCombinedFileSize, GXBaseCollection<SdtMessages_Message>[] messages) {
		this.absolutePath = absolutePath;
		this.maxCombinedFileSize = maxCombinedFileSize;
		this.messages = messages;
		filesToCompress = new ArrayList<>();
	}

	public void setAbsolutePath(String path) {
		this.absolutePath = path;
	}

	public void addElement(String filePath) {
		filesToCompress.add(filePath);
	}

	public Boolean save() {
		return GXCompressor.compress(filesToCompress, absolutePath, maxCombinedFileSize, messages);
	}

	public void clear() {
		absolutePath = "";
		filesToCompress = new ArrayList<>();
	}
}
