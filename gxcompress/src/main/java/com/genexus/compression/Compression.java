package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.stream.Stream;

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
		filesToCompress = new Vector<>();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void addFile(String filePath) {
		filesToCompress.add(filePath);
	}

	public void addFolder(String folderPath) {
		Path path = Paths.get(folderPath);
		try (Stream<Path> stream = Files.walk(path)) {
			stream.filter(Files::isRegularFile)
				.forEach(p -> addFile(p.toAbsolutePath().toString()));
		} catch (IOException e) {
			log.error("Failed to process directory: {}", folderPath, e);
		}
	}

	public Boolean save() {
		return GXCompressor.compressFiles(filesToCompress, path, format, messages);
	}

	public void clear() {
		path = "";
		format = "";
		filesToCompress = new Vector<>();
	}
}
