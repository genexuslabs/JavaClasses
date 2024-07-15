package com.genexus.compression;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Vector;

import static org.junit.Assert.*;

public class TestCompression {

	private Vector<String> files;
	private File testDirectory;

	@Before
	public void setUp() throws IOException {
		testDirectory = Files.createTempDirectory("testCompressor").toFile();
		files = new Vector<>();
		String content = "This is a sample text to test the compression functionality.";
		for (int i = 0; i < 3; i++) {
			File file = new File(testDirectory, "testFile" + i + ".txt");
			try (PrintWriter out = new PrintWriter(file)) {
				out.println(content);
			}
			files.add(file.getAbsolutePath());
		}
	}

	@After
	public void tearDown() {
		for (String filePath : files) {
			new File(filePath).delete();
		}
		testDirectory.delete();
	}

	@Test
	public void testCompressToZip() {
		String outputPath = new File(testDirectory, "output.zip").getAbsolutePath();
		CompressionMessage result = GXCompressor.compressFiles(files, outputPath, "ZIP");
		assertTrue(result.isSuccessfulOperation());
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToSevenZ() {
		String outputPath = new File(testDirectory, "output.7z").getAbsolutePath();
		CompressionMessage result = GXCompressor.compressFiles(files, outputPath, "SEVENZ");
		assertTrue(result.isSuccessfulOperation());
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToTar() {
		String outputPath = new File(testDirectory, "output.tar").getAbsolutePath();
		CompressionMessage result = GXCompressor.compressFiles(files, outputPath, "TAR");
		assertTrue(result.isSuccessfulOperation());
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToGzip() {
		String outputPath = new File(testDirectory, "output.gz").getAbsolutePath();
		Vector<String> singleFileCollection = new Vector<>();
		singleFileCollection.add(files.get(0));
		CompressionMessage result = GXCompressor.compressFiles(singleFileCollection, outputPath, "GZIP");
		assertTrue(result.isSuccessfulOperation());
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToJar() {
		String outputPath = new File(testDirectory, "output.jar").getAbsolutePath();
		CompressionMessage result = GXCompressor.compressFiles(files, outputPath, "JAR");
		assertTrue(result.isSuccessfulOperation());
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testUnsupportedFormat() {
		String outputPath = new File(testDirectory, "output.unknown").getAbsolutePath();
		CompressionMessage result = GXCompressor.compressFiles(files, outputPath, "UNKNOWN");
		assertFalse(result.isSuccessfulOperation());
	}
}
