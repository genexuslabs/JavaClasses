package com.genexus.compression;

public interface IGXCompressor {
	static int compress(String[] files, String path, String format, int dictionarySize) {return 0;}
	static int compress(String folder, String path, String format, int dictionarySize) {return 0;}
	static Compression newCompression(String path, String format, int dictionarySize) { return new Compression();}
	static int decompress(String file, String path) {return 0;}
}
