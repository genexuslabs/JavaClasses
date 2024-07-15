package com.genexus.compression;

import org.apache.logging.log4j.Logger;

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
import com.github.junrar.Junrar;
import com.github.junrar.exception.RarException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.*;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class GXCompressor implements IGXCompressor {

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(GXCompressor.class);
	
	public static CompressionMessage compressFiles(Vector<String> files, String path, String format) {
		if (files.isEmpty()){
			log.error("No files have been added for compression.");
			return new CompressionMessage(false, "No files have been added for compression.");
		}
		File[] toCompress = new File[files.size()];
		int index = 0;
		for (String filePath : files) {
			toCompress[index++] = new File(filePath);
		}
		try {
			CompressionFormat compressionFormat = getCompressionFormat(format);
			switch (compressionFormat) {
				case ZIP:
					compressToZip(toCompress, path);
					return getSuccessfulCompressionMessage();
				case SEVENZ:
					compressToSevenZ(toCompress, path);
					return getSuccessfulCompressionMessage();
				case TAR:
					compressToTar(toCompress, path);
					return getSuccessfulCompressionMessage();
				case GZIP:
					compressToGzip(toCompress, path);
					return getSuccessfulCompressionMessage();
				case JAR:
					compressToJar(toCompress, path);
					return getSuccessfulCompressionMessage();
			}
		} catch (IllegalArgumentException iae) {
			log.error("Unsupported compression format for compression: {}", format);
			return new CompressionMessage(false, "Unsupported compression format for compression: " + format);
		} catch (Exception e) {
			log.error("An error occurred during the compression process: ", e);
			return new CompressionMessage(false, "An error occurred during the compression process");
		}
		return new CompressionMessage(false, "An error occurred during the compression process");
	}
	
	public static CompressionMessage compressFolder(String folder, String path, String format) {
		File toCompress = new File(folder);
		if (!toCompress.exists()) {
			log.error("The specified folder does not exist: {}", toCompress.getAbsolutePath());
			return new CompressionMessage(false, "The specified folder does not exist: " + folder);
		}
		Vector<String> vector = new Vector<>();
		vector.add(folder);
		return compressFiles(vector, path, format);
	}
	
	public static Compression newCompression(String path, String format) {
		return new Compression(path, format);
	}

	public static CompressionMessage decompress(String file, String path) {
		File toCompress = new File(file);
		if (!toCompress.exists()) {
			log.error("The specified archive does not exist: {}", toCompress.getAbsolutePath());
			return new CompressionMessage(false, "The specified archive does not exist: " + file);
		}
		if (toCompress.length() == 0L){
            log.error("The archive located at {} is empty", file);
			return new CompressionMessage(false, "The archive located at " + file + " is empty");
		}
		String extension = getExtension(toCompress.getName());
		try {
			switch (extension.toLowerCase()) {
				case "zip":
					decompressZip(toCompress, path);
					return getSuccessfulCompressionMessage();
				case "7z":
					decompress7z(toCompress, path);
					return getSuccessfulCompressionMessage();
				case "tar":
					decompressTar(toCompress, path);
					return getSuccessfulCompressionMessage();
				case "gz":
					decompressGzip(toCompress, path);
					return getSuccessfulCompressionMessage();
				case "jar":
					decompressJar(toCompress, path);
					return getSuccessfulCompressionMessage();
				case "rar":
					decompressRar(toCompress, path);
					return getSuccessfulCompressionMessage();
				default:
					log.error("Unsupported compression format for decompression: {}", extension);
					return new CompressionMessage(false, "Unsupported compression format for decompression");
			}
		} catch (Exception e) {
			log.error("Decompression failed: {}", e.getMessage());
			return new CompressionMessage(false, "Decompression failed");
		}
	}

	private static CompressionMessage getSuccessfulCompressionMessage(){
		return new CompressionMessage(true, "The operation was successful");
	}

	private static void compressToZip(File[] files, String outputPath) {
		try (OutputStream fos = Files.newOutputStream(Paths.get(outputPath));
			 ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {
			zos.setMethod(ZipArchiveOutputStream.DEFLATED);
			for (File file : files) {
				if (file.exists()) {
					addFileToZip(zos, file, "");
				}
			}
		} catch (IOException e) {
			log.error("Error while compressing to zip", e);
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

	private static void compressToSevenZ(File[] files, String outputPath) throws RuntimeException {
		File outputSevenZFile = new File(outputPath);
		try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(outputSevenZFile)) {
			for (File file : files) {
				addToSevenZArchive(sevenZOutput, file, "");
			}
		} catch (IOException e) {
			log.error("Error while compressing to 7z", e);
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

	private static void compressToTar(File[] files, String outputPath) throws RuntimeException {
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
			log.error("Error while compressing to tar", e);
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

	private static void compressToGzip(File[] files, String outputPath) throws RuntimeException {
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
			log.error("Error while compressing to gzip", e);
			throw new RuntimeException("Error compressing file with GZIP", e);
		}
	}

	private static void decompressZip(File zipFile, String directory) throws RuntimeException{
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
			log.error("Error while decompressing to zip", e);
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
				if (entry.isDirectory() && !outputFile.exists() && !outputFile.mkdirs()) {
					throw new IOException("Failed to create directory: " + outputFile);
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
		File outputDir = new File(outputPath);
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			throw new IOException("Failed to create the output directory: " + outputDir.getAbsolutePath());
		}
		String outputFileName = inputFile.getName();
		if (outputFileName.endsWith(".gz"))
			outputFileName = outputFileName.substring(0, outputFileName.length() - 3);
		else
			throw new IllegalArgumentException("The input file does not have a .gz extension.");
		File outputFile = new File(outputDir, outputFileName);
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

	private static void compressToJar(File[] files, String outputPath) throws IOException {
		JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(outputPath)));
		byte[] buffer = new byte[1024];
		for (File file : files) {
			FileInputStream fis = new FileInputStream(file);
			jos.putNextEntry(new JarEntry(file.getName()));
			int length;
			while ((length = fis.read(buffer)) > 0) {
				jos.write(buffer, 0, length);
			}
			fis.close();
			jos.closeEntry();
		}
		jos.close();
	}

	public static void decompressJar(File jarFile, String outputPath) throws IOException {
		if (!jarFile.exists()) {
			throw new IOException("The jar file does not exist.");
		}
		File outputDir = new File(outputPath);
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			throw new IOException("Failed to create output directory.");
		}
		try (JarInputStream jis = new JarInputStream(Files.newInputStream(jarFile.toPath()))) {
			JarEntry entry;
			byte[] buffer = new byte[1024];
			while ((entry = jis.getNextJarEntry()) != null) {
				File outputFile = new File(outputDir, entry.getName());
				if (entry.isDirectory() && !outputFile.exists() && !outputFile.mkdirs()) {
					throw new IOException("Failed to create directory " + outputFile.getAbsolutePath());
				} else {
					try (FileOutputStream fos = new FileOutputStream(outputFile)) {
						int len;
						while ((len = jis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					}
				}
				jis.closeEntry();
			}
		}
	}

	public static void decompressRar(File rarFile, String destinationPath) throws RarException, IOException{
		Junrar.extract(rarFile, new File(destinationPath));
	}

	private static CompressionFormat getCompressionFormat(String format) {
		try {
			return CompressionFormat.valueOf(format.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid compression format: " + format);
		}
	}

}

