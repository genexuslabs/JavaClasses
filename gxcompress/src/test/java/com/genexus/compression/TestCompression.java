package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.specific.java.Connect;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class TestCompression {

	private ArrayList<String> files;
	private File testDirectory;
	private static GXBaseCollection[] msgs;

	@BeforeClass
	public static void setUpTestSuite() {
		Connect.init();
		msgs = new GXBaseCollection[]{new GXBaseCollection<>()};
		msgs[0] = new GXBaseCollection<>(SdtMessages_Message.class, "Messages.Message", "Genexus");
	}

	@Before
	public void setUpTest() throws IOException {
		testDirectory = Files.createTempDirectory("testCompressor").toFile();
		files = new ArrayList<>();
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
		Boolean result = GXCompressor.compress(files, outputPath, new CompressionConfiguration(), msgs);
		assertTrue(result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToSevenZ() {
		String outputPath = new File(testDirectory, "output.7z").getAbsolutePath();
		Boolean result = GXCompressor.compress(files, outputPath, new CompressionConfiguration(), msgs);
		assertTrue(result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToTar() {
		String outputPath = new File(testDirectory, "output.tar").getAbsolutePath();
		Boolean result = GXCompressor.compress(files, outputPath, new CompressionConfiguration(), msgs);
		assertTrue(result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToGzip() {
		String outputPath = new File(testDirectory, "output.gz").getAbsolutePath();
		ArrayList<String> singleFileCollection = new ArrayList<>();
		singleFileCollection.add(files.get(0));
		Boolean result = GXCompressor.compress(singleFileCollection, outputPath, new CompressionConfiguration(), msgs);
		assertTrue(result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testCompressToJar() {
		String outputPath = new File(testDirectory, "output.jar").getAbsolutePath();
		Boolean result = GXCompressor.compress(files, outputPath, new CompressionConfiguration(), msgs);
		assertTrue(result);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void testUnsupportedFormat() {
		String outputPath = new File(testDirectory, "output.unknown").getAbsolutePath();
		Boolean result = GXCompressor.compress(files, outputPath, new CompressionConfiguration(), msgs);
		assertFalse(result);
	}
}
