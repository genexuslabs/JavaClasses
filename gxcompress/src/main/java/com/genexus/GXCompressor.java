package com.genexus;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.Deflater;

public class GXCompressor implements IGXCompressor {
	@Override
	public void compress(File[] files, String path, CompressionFormat format, String password, CompressionMethod method, DictionarySize dictionarySize) {
		switch (format) {
			case ZIP:
				compressToZip(files, path, method, dictionarySize);
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

	private int convertCompressionMethodToLevel(CompressionMethod method) {
		switch (method) {
			case FASTEST:
				return Deflater.BEST_SPEED;
			case BEST:
				return Deflater.BEST_COMPRESSION;
			default:
				return Deflater.DEFAULT_COMPRESSION;
		}
	}

	private void compressToZip(File[] files, String outputPath, CompressionMethod method, DictionarySize dictionarySize) {
		// Set up the zip output stream to write to the specified output path
		try (OutputStream fos = Files.newOutputStream(Paths.get(outputPath));
			 ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {

			// Setting the compression method and level
			zos.setMethod(ZipArchiveOutputStream.DEFLATED);
			zos.setLevel(convertCompressionMethodToLevel(method));

			// Add each file as a new entry to the zip file
			for (File file : files) {
				if (file.exists()) {
					addFileToZip(zos, file, "");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to compress files", e);
		}
	}

	private void addFileToZip(ZipArchiveOutputStream zos, File file, String base) throws IOException {
		String entryName = base + file.getName();
		if (file.isDirectory()) {
			// If it's a directory, recursively add its contents
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToZip(zos, child, entryName + "/");
				}
			}
		} else {
			// If it's a file, add it as an entry
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

	private void compressToSevenZ(File[] files, String outputPath) {
		File outputSevenZFile = new File(outputPath);
		try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(outputSevenZFile)) {
			for (File file : files) {
				addToSevenZArchive(sevenZOutput, file, "");
			}
		} catch (IOException e) {
			throw new RuntimeException("Error creating 7z archive", e);
		}
	}

	private void addToSevenZArchive(SevenZOutputFile sevenZOutput, File file, String base) throws IOException {
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
			// Recursively add directory contents
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addToSevenZArchive(sevenZOutput, child, base + file.getName() + "/");
				}
			}
		}
	}

	private void compressToTar(File[] files, String outputPath) {
		File outputTarFile = new File(outputPath);
		try (FileOutputStream fos = new FileOutputStream(outputTarFile);
			 TarArchiveOutputStream taos = new TarArchiveOutputStream(fos)) {

			// Configure TAR options
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

	private void addFileToTar(TarArchiveOutputStream taos, File file, String base) throws IOException {
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

	private void compressToGzip(File[] files, String outputPath) {
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
}

