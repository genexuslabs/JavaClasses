package com.genexus;

import java.io.File;

public interface IGXCompressor {
	static void compress(File[] files, String path, CompressionFormat format, int dictionarySize) {}
	static void compress(File folder, String path, CompressionFormat format, int dictionarySize) {}
	static Compression newCompression(String path, CompressionFormat format, int dictionarySize) { return new Compression();}
	static void decompress(File file, String path) {}
}
