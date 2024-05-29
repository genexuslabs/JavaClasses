package com.genexus.compression;

import java.io.File;

public interface IGXCompressor {
	static int compress(File[] files, String path, String format, int dictionarySize) {return 0;}
	static int compress(File folder, String path, String format, int dictionarySize) {return 0;}
	static Compression newCompression(String path, String format, int dictionarySize) { return new Compression();}
	static int decompress(File file, String path) {return 0;}
}
