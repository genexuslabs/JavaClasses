package com.genexus.compression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Vector;

public class CompressionTest {

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
		int result = GXCompressor.compressFiles(files, outputPath, "ZIP");
		assertEquals(0, result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToSevenZ() {
		String outputPath = new File(testDirectory, "output.7z").getAbsolutePath();
		int result = GXCompressor.compressFiles(files, outputPath, "SEVENZ");
		assertEquals(0, result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToTar() {
		String outputPath = new File(testDirectory, "output.tar").getAbsolutePath();
		int result = GXCompressor.compressFiles(files, outputPath, "TAR");
		assertEquals(0, result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToGzip() {
		String outputPath = new File(testDirectory, "output.gz").getAbsolutePath();
		Vector<String> singleFileCollection = new Vector<>();
		singleFileCollection.add(files.get(0));
		int result = GXCompressor.compressFiles(singleFileCollection, outputPath, "GZIP");
		assertEquals(0, result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testUnsupportedFormat() {
		String outputPath = new File(testDirectory, "output.unknown").getAbsolutePath();
		int result = GXCompressor.compressFiles(files, outputPath, "UNKNOWN");
		assertEquals(-1, result);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThrowExceptionForUnsupportedFormat() {
		String outputPath = new File(testDirectory, "output.unsupported").getAbsolutePath();
		GXCompressor.compressFiles(files, outputPath, "UNSUPPORTED");
	}
}
