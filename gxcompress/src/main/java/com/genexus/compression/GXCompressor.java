package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.StructSdtMessages_Message;
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

	private static void storageMessages(Exception ex, String error, GXBaseCollection<SdtMessages_Message> messages) {
		StructSdtMessages_Message struct = new StructSdtMessages_Message();
		if (ex != null)
			struct.setDescription(ex.getMessage());
		else
			struct.setDescription(error);
		struct.setType((byte) 1);
		SdtMessages_Message msg = new SdtMessages_Message(struct);
		messages.add(msg);
	}
	
	public static Boolean compressFiles(Vector<String> files, String path, String format, GXBaseCollection<SdtMessages_Message>[] messages) {
		if (files.isEmpty()){
			log.error("No files have been added for compression.");
			if (messages != null) storageMessages(null, "No files have been added for compression.", messages[0]);
			return false;
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
					return true;
				case SEVENZ:
					compressToSevenZ(toCompress, path);
					return true;
				case TAR:
					compressToTar(toCompress, path);
					return true;
				case GZIP:
					compressToGzip(toCompress, path);
					return true;
				case JAR:
					compressToJar(toCompress, path);
					return true;
			}
		} catch (IllegalArgumentException iae) {
			log.error("Unsupported compression format for compression: {}", format, iae);
			if (messages != null) storageMessages(null, "Unsupported compression format for compression: " + format, messages[0]);
			return false;
		} catch (Exception e) {
			log.error("An error occurred during the compression process: ", e);
			return false;
		}
		return false;
	}
	
	public static Boolean compressFolder(String folder, String path, String format, GXBaseCollection<SdtMessages_Message>[] messages) {
		File toCompress = new File(folder);
		if (!toCompress.exists()) {
			log.error("The specified folder does not exist: {}", toCompress.getAbsolutePath());
			if (messages != null) storageMessages(null, "The specified folder does not exist: " + toCompress.getAbsolutePath(), messages[0]);
			return false;
		}
		Vector<String> vector = new Vector<>();
		vector.add(folder);
		return compressFiles(vector, path, format, messages);
	}
	
	public static Compression newCompression(String path, String format, GXBaseCollection<SdtMessages_Message>[] messages) {
		return new Compression(path, format, messages);
	}

	public static Boolean decompress(String file, String path, GXBaseCollection<SdtMessages_Message>[] messages) {
		File toCompress = new File(file);
		if (!toCompress.exists()) {
			log.error("The specified archive does not exist: {}", toCompress.getAbsolutePath());
			if (messages != null) storageMessages(null, "The specified archive does not exist: " + toCompress.getAbsolutePath(), messages[0]);
			return false;
		}
		if (toCompress.length() == 0L){
            log.error("The archive located at {} is empty", file);
			if (messages != null) storageMessages(null, "The archive located at " + toCompress.getAbsolutePath() + " is empty", messages[0]);
			return false;
		}
		String extension = getExtension(toCompress.getName());
		try {
			switch (extension.toLowerCase()) {
				case "zip":
					decompressZip(toCompress, path);
					return true;
				case "7z":
					decompress7z(toCompress, path);
					return true;
				case "tar":
					decompressTar(toCompress, path);
					return true;
				case "gz":
					decompressGzip(toCompress, path);
					return true;
				case "jar":
					decompressJar(toCompress, path);
					return true;
				case "rar":
					decompressRar(toCompress, path);
					return true;
				default:
					log.error("Unsupported compression format for decompression: {}", extension);
					if (messages != null) storageMessages(null, "Unsupported compression format for decompression: " + extension, messages[0]);
					return false;
			}
		} catch (Exception e) {
			log.error("Decompression failed: ", e);
			if (messages != null) storageMessages(e, null, messages[0]);
			return false;
		}
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

