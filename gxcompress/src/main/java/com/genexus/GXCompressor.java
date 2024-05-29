package com.genexus;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.*;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class GXCompressor implements IGXCompressor {
	
	public static void compress(File[] files, String path, CompressionFormat format, int dictionarySize) {
		switch (format) {
			case ZIP:
				compressToZip(files, path, dictionarySize);
				break;
			case SEVENZ:
				compressToSevenZ(files, path);
				break;
			case TAR:
				compressToTar(files, path);
				break;
			case GZIP:
				compressToGzip(files, path);
				break;
			default:
				throw new IllegalArgumentException("Unsupported compression format: " + format);
		}
	}

	
	public static void compress(File folder, String path, CompressionFormat format, int dictionarySize) {
		if (!folder.exists()) {
			throw new IllegalArgumentException("The specified folder does not exist: " + folder.getAbsolutePath());
		}
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("The specified file is not a directory: " + folder.getAbsolutePath());
		}
		File[] files = new File[] { folder };
		compress(files, path, format, dictionarySize);
	}

	
	public static Compression newCompression(String path, CompressionFormat format,  int dictionarySize) {
		return new Compression(path, format, dictionarySize);
	}


	public static void decompress(File file, String path) {
		String extension = getExtension(file.getName());
		try {
			switch (extension.toLowerCase()) {
				case "zip":
					decompressZip(file, path);
					break;
				case "7z":
					decompress7z(file, path);
					break;
				case "tar":
					decompressTar(file, path);
					break;
				case "gz":
					decompressGzip(file, path);
					break;
				default:
					throw new IllegalArgumentException("Unsupported compression format for decompression: " + extension);
			}
		} catch (IOException ioe) {
			System.out.println("Decompression failed: " + ioe.getMessage());
		}
	}

	private static void compressToZip(File[] files, String outputPath, int dictionarySize) {
		try (OutputStream fos = Files.newOutputStream(Paths.get(outputPath));
			 ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {
			zos.setMethod(ZipArchiveOutputStream.DEFLATED);
			for (File file : files) {
				if (file.exists()) {
					addFileToZip(zos, file, "");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to compress files", e);
		}
	}

	private static void addFileToZip(ZipArchiveOutputStream zos, File file, String base) throws IOException {
		String entryName = base + file.getName();
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToZip(zos, child, entryName + "/");
				}
			}
		} else {
			ZipArchiveEntry zipEntry = new ZipArchiveEntry(file, entryName);
			zos.putArchiveEntry(zipEntry);
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] buffer = new byte[1024];
				int length;
				while ((length = fis.read(buffer)) >= 0) {
					zos.write(buffer, 0, length);
				}
			}
			zos.closeArchiveEntry();
		}
	}

	private static void compressToSevenZ(File[] files, String outputPath) {
		File outputSevenZFile = new File(outputPath);
		try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(outputSevenZFile)) {
			for (File file : files) {
				addToSevenZArchive(sevenZOutput, file, "");
			}
		} catch (IOException e) {
			throw new RuntimeException("Error creating 7z archive", e);
		}
	}

	private static void addToSevenZArchive(SevenZOutputFile sevenZOutput, File file, String base) throws IOException {
		if (file.isFile()) {
			SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(file, base + file.getName());
			sevenZOutput.putArchiveEntry(entry);
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = fis.read(buffer)) != -1) {
					sevenZOutput.write(buffer, 0, bytesRead);
				}
			}
			sevenZOutput.closeArchiveEntry();
		} else if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addToSevenZArchive(sevenZOutput, child, base + file.getName() + "/");
				}
			}
		}
	}

	private static void compressToTar(File[] files, String outputPath) {
		File outputTarFile = new File(outputPath);
		try (FileOutputStream fos = new FileOutputStream(outputTarFile);
			 TarArchiveOutputStream taos = new TarArchiveOutputStream(fos)) {
			taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
			taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
			taos.setAddPaxHeadersForNonAsciiNames(true);

			for (File file : files) {
				addFileToTar(taos, file, "");
			}
			taos.finish();
		} catch (IOException e) {
			throw new RuntimeException("Error creating TAR archive", e);
		}
	}

	private static void addFileToTar(TarArchiveOutputStream taos, File file, String base) throws IOException {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToTar(taos, child, base + file.getName() + "/");
				}
			}
		} else if (file.isFile()) {
			String entryName = base + file.getName();
			TarArchiveEntry entry = new TarArchiveEntry(file, entryName);
			entry.setSize(file.length());

			taos.putArchiveEntry(entry);
			try (FileInputStream fis = new FileInputStream(file)) {
				IOUtils.copy(fis, taos);
			}
			taos.closeArchiveEntry();
		}
	}

	private static void compressToGzip(File[] files, String outputPath) {
		if (files.length > 1) {
			throw new IllegalArgumentException("GZIP does not support multiple files. Consider archiving the files first.");
		}

		File inputFile = files[0];
		File outputFile = new File(outputPath);

		try (InputStream in = Files.newInputStream(inputFile.toPath());
			 FileOutputStream fout = new FileOutputStream(outputFile);
			 BufferedOutputStream out = new BufferedOutputStream(fout);
			 GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {

			byte[] buffer = new byte[4096];
			int n;
			while (-1 != (n = in.read(buffer))) {
				gzOut.write(buffer, 0, n);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error compressing file with GZIP", e);
		}
	}

	private static void decompressZip(File zipFile, String directory) {
		Path zipFilePath =  Paths.get(zipFile.toURI());
		Path targetDir = Paths.get(directory);
		targetDir = targetDir.toAbsolutePath();

		try (InputStream fis = Files.newInputStream(zipFilePath.toFile().toPath());
			 ZipInputStream zipIn = new ZipInputStream(fis)) {

			ZipEntry entry;
			while ((entry = zipIn.getNextEntry()) != null) {
				Path resolvedPath = targetDir.resolve(entry.getName()).normalize();

				if (!resolvedPath.startsWith(targetDir)) {
					throw new SecurityException("Zip entry is outside of the target dir: " + entry.getName());
				}

				if (entry.isDirectory()) {
					Files.createDirectories(resolvedPath);
				} else {
					Path parentDir = resolvedPath.getParent();
					if (!Files.exists(parentDir)) {
						Files.createDirectories(parentDir);
					}
					try (OutputStream out = Files.newOutputStream(resolvedPath.toFile().toPath())) {
						byte[] buffer = new byte[4096];
						int length;
						while ((length = zipIn.read(buffer)) > 0) {
							out.write(buffer, 0, length);
						}
					}
				}
				zipIn.closeEntry();
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to decompress ZIP file: " + zipFilePath, e);
		}
	}

	private static void decompress7z(File file, String outputPath) throws IOException {
		Path targetDir = Paths.get(outputPath).toAbsolutePath();

		try (SevenZFile sevenZFile = new SevenZFile(file)) {
			SevenZArchiveEntry entry = sevenZFile.getNextEntry();
			while (entry != null) {
				Path resolvedPath = targetDir.resolve(entry.getName()).normalize();

				if (!resolvedPath.startsWith(targetDir)) {
					throw new IOException("Entry is outside of the target dir: " + entry.getName());
				}

				if (entry.isDirectory()) {
					Files.createDirectories(resolvedPath);
				} else {
					File outputFile = resolvedPath.toFile();
					File parentDir = outputFile.getParentFile();
					if (!parentDir.exists() && !parentDir.mkdirs()) {
						throw new IOException("Failed to create directory: " + parentDir);
					}

					try (FileOutputStream out = new FileOutputStream(outputFile)) {
						byte[] content = new byte[(int) entry.getSize()];
						sevenZFile.read(content, 0, content.length);
						out.write(content);
					}
				}
				entry = sevenZFile.getNextEntry();
			}
		}
	}

	private static void decompressTar(File file, String outputPath) throws IOException {
		Path targetDir = Paths.get(outputPath).toAbsolutePath();
		try (InputStream fi = Files.newInputStream(file.toPath());
			 TarArchiveInputStream ti = new TarArchiveInputStream(fi)) {
			TarArchiveEntry entry;
			while ((entry = ti.getNextTarEntry()) != null) {
				File outputFile = targetDir.resolve(entry.getName()).normalize().toFile();
				if (!outputFile.toPath().startsWith(targetDir)) {
					throw new IOException("Entry is outside of the target directory: " + entry.getName());
				}
				if (entry.isDirectory()) {
					if (!outputFile.exists()) {
						if (!outputFile.mkdirs()) {
							throw new IOException("Failed to create directory: " + outputFile);
						}
					}
				} else {
					File parent = outputFile.getParentFile();
					if (!parent.exists() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory: " + parent);
					}
					try (OutputStream out = new FileOutputStream(outputFile)) {
						IOUtils.copy(ti, out);
					}
				}
			}
		}
	}

	private static void decompressGzip(File inputFile, String outputPath) throws IOException {
		// Create the output directory if it doesn't exist
		File outputDir = new File(outputPath);
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			throw new IOException("Failed to create the output directory: " + outputDir.getAbsolutePath());
		}

		// Generate the output file's name by removing the .gz extension
		String outputFileName = inputFile.getName();
		if (outputFileName.endsWith(".gz")) {
			outputFileName = outputFileName.substring(0, outputFileName.length() - 3);
		} else {
			// Handle cases where the extension is not .gz (just a safeguard)
			throw new IllegalArgumentException("The input file does not have a .gz extension.");
		}

		File outputFile = new File(outputDir, outputFileName);

		// Decompress the GZIP file
		try (FileInputStream fis = new FileInputStream(inputFile);
			 GZIPInputStream gzis = new GZIPInputStream(fis);
			 FileOutputStream fos = new FileOutputStream(outputFile)) {
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = gzis.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		}
	}


}

