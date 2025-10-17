package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;

import java.util.ArrayList;

public class Compression {

	private String destinationPath;
	private CompressionConfiguration compressionConfiguration;
	private GXBaseCollection<SdtMessages_Message>[] messages;
	private ArrayList<String> filesToCompress;

	public Compression() {}

	public Compression(String destinationPath, CompressionConfiguration configuration, GXBaseCollection<SdtMessages_Message>[] messages) {
		this.destinationPath = destinationPath;
		this.compressionConfiguration = configuration;
		this.messages = messages;
		filesToCompress = new ArrayList<>();
	}

	public void setDestinationPath(String path) {
		this.destinationPath = path;
	}

	public void addElement(String filePath) {
		filesToCompress.add(filePath);
	}

	public Boolean save() {
		return GXCompressor.compress(filesToCompress, destinationPath, compressionConfiguration, messages);
	}

	public void clear() {
		destinationPath = "";
		filesToCompress = new ArrayList<>();
		messages = null;
		compressionConfiguration = new CompressionConfiguration();
	}
}
