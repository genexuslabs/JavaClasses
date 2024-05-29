package com.genexus.compression;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompressionTest {
	private List<File> tempFiles = new ArrayList<>();
	private File tempDir;

	@BeforeEach
	void setUp() throws IOException {
		tempDir = Files.createTempDirectory("compressionTest").toFile();
		for (int i = 0; i < 3; i++) {
			File tempFile = new File(tempDir, "testFile" + i + ".txt");
			try (FileWriter writer = new FileWriter(tempFile)) {
				writer.write("This is a test file number " + i);
			}
			tempFiles.add(tempFile);
		}
	}

	@AfterEach
	void tearDown() {
		for (File file : tempFiles) {
			file.delete();
		}
		tempDir.delete();
	}

	@Test
	void testCompressionAndDecompressionZIP() throws IOException {
		String compressedPath = tempDir.getAbsolutePath() + File.separator + "archive.zip";
		String decompressedPath = tempDir.getAbsolutePath() + File.separator + "decompressed";
		File[] filesArray = tempFiles.toArray(new File[0]);
		GXCompressor.compress(filesArray, compressedPath, "zip", 4);
		File compressedFile = new File(compressedPath);
		GXCompressor.decompress(compressedFile, decompressedPath);
		File decompressedDir = new File(decompressedPath);
		File[] decompressedFiles = decompressedDir.listFiles();
		assertNotNull(decompressedFiles);
		assertEquals(tempFiles.size(), decompressedFiles.length);
		for (File original : tempFiles) {
			File decompressedFile = new File(decompressedDir, original.getName());
			assertTrue(decompressedFile.exists());
			assertArrayEquals(Files.readAllBytes(original.toPath()), Files.readAllBytes(decompressedFile.toPath()));
		}
		compressedFile.delete();
		for (File file : decompressedFiles) {
			file.delete();
		}
		decompressedDir.delete();
	}

	@Test
	void testCompressionAndDecompressionTAR() throws IOException {
		String compressedPath = tempDir.getAbsolutePath() + File.separator + "archive.tar";
		String decompressedPath = tempDir.getAbsolutePath() + File.separator + "decompressed";
		File[] filesArray = tempFiles.toArray(new File[0]);
		GXCompressor.compress(filesArray, compressedPath, "tar", 128);
		File compressedFile = new File(compressedPath);
		GXCompressor.decompress(compressedFile, decompressedPath);
		File decompressedDir = new File(decompressedPath);
		File[] decompressedFiles = decompressedDir.listFiles();
		assertNotNull(decompressedFiles);
		assertEquals(tempFiles.size(), decompressedFiles.length);
		for (File original : tempFiles) {
			File decompressedFile = new File(decompressedDir, original.getName());
			assertTrue(decompressedFile.exists());
			assertArrayEquals(Files.readAllBytes(original.toPath()), Files.readAllBytes(decompressedFile.toPath()));
		}
		compressedFile.delete();
		for (File file : decompressedFiles) {
			file.delete();
		}
		decompressedDir.delete();
	}

	@Test
	void testCompressionAndDecompressionSEVENZ() throws IOException {
		String compressedPath = tempDir.getAbsolutePath() + File.separator + "archive.7z";
		String decompressedPath = tempDir.getAbsolutePath() + File.separator + "decompressed";
		File[] filesArray = tempFiles.toArray(new File[0]);
		GXCompressor.compress(filesArray, compressedPath, "sevenz", 32);
		File compressedFile = new File(compressedPath);
		GXCompressor.decompress(compressedFile, decompressedPath);
		File decompressedDir = new File(decompressedPath);
		File[] decompressedFiles = decompressedDir.listFiles();
		assertNotNull(decompressedFiles);
		assertEquals(tempFiles.size(), decompressedFiles.length);
		for (File original : tempFiles) {
			File decompressedFile = new File(decompressedDir, original.getName());
			assertTrue(decompressedFile.exists());
			assertArrayEquals(Files.readAllBytes(original.toPath()), Files.readAllBytes(decompressedFile.toPath()));
		}
		compressedFile.delete();
		for (File file : decompressedFiles) {
			file.delete();
		}
		decompressedDir.delete();
	}

	@Test
	void testCompressionAndDecompressionGZIP() throws IOException {
		String compressedPath = tempDir.getAbsolutePath() + File.separator + "archive.gz";
		String decompressedDirPath = tempDir.getAbsolutePath();
		File singleTestFile = new File(tempDir, "testFile.txt");
		try (FileWriter writer = new FileWriter(singleTestFile)) {
			writer.write("This is a test file");
		}
		GXCompressor.compress(new File[]{singleTestFile}, compressedPath, "gzip", 4);
		File compressedFile = new File(compressedPath);
		assertTrue(compressedFile.exists());
		GXCompressor.decompress(compressedFile, decompressedDirPath);
		File decompressedFile = new File(decompressedDirPath, singleTestFile.getName());
		assertTrue(decompressedFile.exists());
		assertArrayEquals(Files.readAllBytes(singleTestFile.toPath()), Files.readAllBytes(decompressedFile.toPath()));
		compressedFile.delete();
		decompressedFile.delete();
	}

}