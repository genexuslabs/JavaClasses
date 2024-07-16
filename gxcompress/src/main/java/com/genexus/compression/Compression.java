package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.StructSdtMessages_Message;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Vector;

public class Compression {
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Compression.class);

	private String path;
	private String format;
	private GXBaseCollection<SdtMessages_Message>[] messages;
	private Vector<String> filesToCompress;

	public Compression() {}

	public Compression(String path, String format, GXBaseCollection<SdtMessages_Message>[] messages) {
		this.path = path;
		this.format = format;
		this.messages = messages;
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
			if (messages != null) storageMessages("File does not exist: " + filePath, messages[0]);
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
			if (messages != null) storageMessages("Folder does not exist or is not a directory: " + folder.getAbsolutePath(), messages[0]);
		}
	}

	public Boolean save() {
		return GXCompressor.compressFiles(filesToCompress, path, format, this.messages);
	}

	public void clear() {
		this.path = "";
		this.format = "";
		this.filesToCompress = new Vector<>();
	}

	private static void storageMessages(String error, GXBaseCollection<SdtMessages_Message> messages) {
		StructSdtMessages_Message struct = new StructSdtMessages_Message();
		struct.setDescription(error);
		struct.setType((byte) 1);
		SdtMessages_Message msg = new SdtMessages_Message(struct);
		messages.add(msg);
	}
}
