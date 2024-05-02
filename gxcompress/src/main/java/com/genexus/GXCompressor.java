package com.genexus;

import java.io.File;

public class GXCompressor implements IGXCompressor {
	@Override
	public void compress(File[] files, String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize) {
		// Implementation goes here
	}

	@Override
	public void compress(File folder, String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize) {
		// Implementation goes here
	}

	@Override
	public Compression newCompression(String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize) {
		// Implementation goes here
		return new Compression(path, format, password, method, dictionarySize);
	}

	@Override
	public void decompress(File file, String path, String password) {
		// Implementation goes here
	}
}

