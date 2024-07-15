package com.genexus.compression;

import java.util.Vector;

public interface IGXCompressor {
	static CompressionMessage compressFiles(Vector<String> files, String path, String format) {return null;}
	static CompressionMessage compressFolder(String folder, String path, String format) {return null;}
	static Compression newCompression(String path, String format, int dictionarySize) { return new Compression();}
	static CompressionMessage decompress(String file, String path) {return null;}
}
