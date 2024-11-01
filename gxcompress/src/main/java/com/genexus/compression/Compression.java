package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Compression {
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Compression.class);

	private String absolutePath;
	private GXBaseCollection<SdtMessages_Message>[] messages;
	private ArrayList<String> filesToCompress;

	public Compression() {}

	public Compression(String absolutePath, GXBaseCollection<SdtMessages_Message>[] messages) {
		this.absolutePath = absolutePath;
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
		return GXCompressor.compress(filesToCompress, absolutePath, messages);
	}

	public void clear() {
		absolutePath = "";
		filesToCompress = new ArrayList<>();
	}
}
