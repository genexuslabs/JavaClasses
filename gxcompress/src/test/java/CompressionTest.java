import com.genexus.CompressionFormat;
import com.genexus.CompressionMethod;
import com.genexus.GXCompressor;
import com.genexus.DictionarySize;
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
	void testCompressionAndDecompression() throws IOException {
		// Define paths
		String compressedPath = tempDir.getAbsolutePath() + File.separator + "archive.zip";
		String decompressedPath = tempDir.getAbsolutePath() + File.separator + "decompressed";

		// Compress files
		File[] filesArray = tempFiles.toArray(new File[0]);
		GXCompressor.compress(filesArray, compressedPath, CompressionFormat.ZIP, "", CompressionMethod.BEST, DictionarySize.FOUR_MB);

		// Decompress files
		File compressedFile = new File(compressedPath);
		GXCompressor.decompress(compressedFile, decompressedPath, "");

		// Check files
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
}