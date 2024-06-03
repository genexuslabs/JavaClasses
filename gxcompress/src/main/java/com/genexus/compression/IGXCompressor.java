package com.genexus.compression;

import java.util.Vector;

public interface IGXCompressor {
	static int compressFiles(Vector<String> files, String path, String format) {return 0;}
	static int compressFolder(String folder, String path, String format) {return 0;}
	static Compression newCompression(String path, String format, int dictionarySize) { return new Compression();}
	static int decompress(String file, String path) {return 0;}
}
