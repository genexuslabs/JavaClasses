package com.genexus;

import java.io.File;

public interface IGXCompressor {
	void compress(File[] files, String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize);
	void compress(File folder, String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize);
	Compression newCompression(String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize);
	void decompress(File file, String path, String password);
}
