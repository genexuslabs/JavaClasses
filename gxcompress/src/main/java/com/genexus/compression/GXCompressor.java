package com.genexus.compression;

import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.StructSdtMessages_Message;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.logging.log4j.Logger;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.*;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class GXCompressor implements IGXCompressor {

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(GXCompressor.class);

	private static final String GENERIC_ERROR = "An error occurred during the compression/decompression process: ";
	private static final String NO_FILES_ADDED = "No files have been added for compression.";
	private static final String FILE_NOT_EXISTS = "File does not exist: ";
	private static final String UNSUPPORTED_FORMAT = " is an unsupported format. Supported formats are zip, 7z, tar, gz and jar.";
	private static final String EMPTY_FILE = "The selected file is empty: ";
	private static final String DIRECTORY_ATTACK = "Potential directory traversal attack detected: ";
	private static final String MAX_FILESIZE_EXCEEDED = "The files selected for compression exceed the maximum permitted file size of ";

	private static void storageMessages(String error, GXBaseCollection<SdtMessages_Message> messages) {
		try {
			StructSdtMessages_Message struct = new StructSdtMessages_Message();
			struct.setDescription(error);
			struct.setType((byte) 1);
			SdtMessages_Message msg = new SdtMessages_Message(struct);
			messages.add(msg);
		} catch (Exception e) {
			log.error("Failed to store the following error message: {}", error, e);
		}
	}
	
	public static Boolean compress(ArrayList<String> files, String path, long maxCombinedFileSize, GXBaseCollection<SdtMessages_Message>[] messages) {
		if (files.isEmpty()){
			log.error(NO_FILES_ADDED);
			storageMessages(NO_FILES_ADDED, messages[0]);
			return false;
		}
		long totalSize = 0;
		File[] toCompress = new File[files.size()];
		int index = 0;
		for (String filePath : files) {
			File file = new File(filePath);
			try {
				String normalizedPath = file.getCanonicalPath();
				if (!file.exists()) {
					log.error("{}{}", FILE_NOT_EXISTS, filePath);
					storageMessages(FILE_NOT_EXISTS + filePath, messages[0]);
					continue;
				}
				if (normalizedPath.contains(File.separator + ".." + File.separator) ||
					normalizedPath.endsWith(File.separator + "..") ||
					normalizedPath.startsWith(".." + File.separator)) {
					log.error(DIRECTORY_ATTACK + "{}", filePath);
					storageMessages(DIRECTORY_ATTACK + filePath, messages[0]);
					return false;
				}
				long fileSize = file.length();
				totalSize += fileSize;
				if (totalSize > maxCombinedFileSize && maxCombinedFileSize > -1) {
					log.error(MAX_FILESIZE_EXCEEDED + "{}", maxCombinedFileSize);
					storageMessages(MAX_FILESIZE_EXCEEDED + maxCombinedFileSize, messages[0]);
					return false;
				}
				toCompress[index++] = file;
			} catch (IOException e) {
				log.error("Error normalizing path for file: {}", filePath, e);
			}
		}
		String format = CommonUtil.getFileType(path).toLowerCase();
		try {
			switch (format.toLowerCase()) {
				case "zip":
					compressToZip(toCompress, path);
					break;
				case "7z":
					compressToSevenZ(toCompress, path);
					break;
				case "tar":
					compressToTar(toCompress, path);
					break;
				case "gz":
					compressToGzip(toCompress, path);
					break;
				case "jar":
					compressToJar(toCompress, path);
					break;
				default:
					log.error("{}" + UNSUPPORTED_FORMAT, format);
					storageMessages(format + UNSUPPORTED_FORMAT, messages[0]);
					return false;
			}
			return true;
		} catch (Exception e) {
			log.error(GENERIC_ERROR, e);
			storageMessages(e.getMessage(),  messages[0]);
			return false;
		}
	}
	
	public static Compression newCompression(String path, long maxCombinedFileSize, GXBaseCollection<SdtMessages_Message>[] messages) {
		return new Compression(path, maxCombinedFileSize, messages);
	}

	public static Boolean decompress(String file, String path, GXBaseCollection<SdtMessages_Message>[] messages) {
		File toCompress = new File(file);
		if (!toCompress.exists()) {
			log.error("{}{}", FILE_NOT_EXISTS, toCompress.getAbsolutePath());
			storageMessages(FILE_NOT_EXISTS + toCompress.getAbsolutePath(), messages[0]);
			return false;
		}
		if (toCompress.length() == 0L){
			log.error("{}{}", EMPTY_FILE, file);
			storageMessages(EMPTY_FILE + file, messages[0]);
			return false;
		}
		String extension = getExtension(toCompress.getName());
		try {
			switch (extension.toLowerCase()) {
				case "zip":
					decompressZip(toCompress, path);
					break;
				case "7z":
					decompress7z(toCompress, path);
					break;
				case "tar":
					decompressTar(toCompress, path);
					break;
				case "gz":
					decompressGzip(toCompress, path);
					break;
				case "jar":
					decompressJar(toCompress, path);
					break;
				default:
					log.error("{}" + UNSUPPORTED_FORMAT, extension);
					storageMessages(extension + UNSUPPORTED_FORMAT, messages[0]);
					return false;
			}
			return true;
		} catch (Exception e) {
			log.error(GENERIC_ERROR, e);
			storageMessages(e.getMessage(), messages[0]);
			return false;
		}
	}

	private static void compressToZip(File[] files, String outputPath) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(outputPath);
			 ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			for (File fileToZip : files) {
				zipFile(fileToZip, fileToZip.getName(), zipOut);
			}
		}
	}

	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (!fileName.endsWith("/")) {
				fileName += "/";
			}
			zipOut.putNextEntry(new ZipEntry(fileName));
			zipOut.closeEntry();
			File[] children = fileToZip.listFiles();
			if (children != null) {
				for (File childFile : children) {
					zipFile(childFile, fileName + childFile.getName(), zipOut);
				}
			}
		} else {
			try (FileInputStream fis = new FileInputStream(fileToZip)) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
			}
		}
	}

	private static void compressToSevenZ(File[] files, String outputPath) throws IOException {
		if (files == null || outputPath == null) {
			throw new IllegalArgumentException("Files and outputPath must not be null");
		}
		File outputFile = new File(outputPath);
		if (outputFile.exists()) {
			throw new IOException("Output file already exists");
		}
		try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(outputFile)) {
			for (File file : files) {
				if (file == null || !file.exists()) {
					continue;
				}
				addFileToSevenZ(sevenZOutput, file, file.getName());
			}
		}
	}

	private static void addFileToSevenZ(SevenZOutputFile sevenZOutput, File file, String entryName) throws IOException {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToSevenZ(sevenZOutput, child, entryName + "/" + child.getName());
				}
			}
		} else {
			SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(file, entryName);
			sevenZOutput.putArchiveEntry(entry);
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = fis.read(buffer)) > 0) {
					sevenZOutput.write(buffer, 0, len);
				}
			}
			sevenZOutput.closeArchiveEntry();
		}
	}

	private static void compressToTar(File[] files, String outputPath) throws IOException {
		if (outputPath == null || outputPath.isEmpty()) {
			throw new IllegalArgumentException("The output path must not be null or empty");
		}
		File outputFile = new File(outputPath);
		if (outputFile.exists()) {
			throw new IOException("Output file already exists");
		}
		try (FileOutputStream fos = new FileOutputStream(outputFile);
			 BufferedOutputStream bos = new BufferedOutputStream(fos);
			 TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos)) {
			for (File file : files) {
				if (file == null || !file.exists()) {
					continue;
				}
				addFileToTar(tarOut, file, file.getName());
			}
		}
	}

	private static void addFileToTar(TarArchiveOutputStream tarOut, File file, String entryName) throws IOException {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToTar(tarOut, child, entryName + "/" + child.getName());
				}
			}
		} else {
			TarArchiveEntry entry = new TarArchiveEntry(file, entryName);
			entry.setSize(file.length());
			tarOut.putArchiveEntry(entry);
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = fis.read(buffer)) != -1) {
					tarOut.write(buffer, 0, len);
				}
			}
			tarOut.closeArchiveEntry();
		}
	}

	private static void compressToGzip(File[] files, String outputPath) throws IOException {
		if (outputPath == null || outputPath.isEmpty()) {
			throw new IllegalArgumentException("Output path is null or empty");
		}
		File outputFile = new File(outputPath);
		if (outputFile.exists() && !outputFile.canWrite()) {
			throw new IOException("Cannot write to output file");
		} else {
			File parentDir = outputFile.getParentFile();
			if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
					throw new IOException("Failed to create output directory");
			}
		}
		try (FileOutputStream fos = new FileOutputStream(outputFile);
			 BufferedOutputStream bos = new BufferedOutputStream(fos);
			 GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos);
			 TarArchiveOutputStream taos = new TarArchiveOutputStream(gcos)) {
			taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
			Stack<File> stack = new Stack<>();
			Stack<String> pathStack = new Stack<>();
			for (File file : files) {
				if (file == null) continue;
				stack.push(file);
				pathStack.push("");
			}
			while (!stack.isEmpty()) {
				File currentFile = stack.pop();
				String path = pathStack.pop();
				String entryName = path + currentFile.getName();
				if (currentFile.isDirectory()) {
					File[] dirFiles = currentFile.listFiles();
					if (dirFiles != null) {
						if (dirFiles.length == 0) {
							TarArchiveEntry entry = new TarArchiveEntry(entryName + "/");
							taos.putArchiveEntry(entry);
							taos.closeArchiveEntry();
						} else {
							for (File child : dirFiles) {
								stack.push(child);
								pathStack.push(entryName + "/");
							}
						}
					}
				} else {
					TarArchiveEntry entry = new TarArchiveEntry(currentFile, entryName);
					taos.putArchiveEntry(entry);
					try (FileInputStream fis = new FileInputStream(currentFile)) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = fis.read(buffer)) != -1) {
							taos.write(buffer, 0, len);
						}
					}
					taos.closeArchiveEntry();
				}
			}
		}
	}

	private static void compressToJar(File[] files, String outputPath) throws IOException {
		if (outputPath == null || outputPath.isEmpty()) {
			throw new IllegalArgumentException("Output path is null or empty");
		}
		File outputFile = new File(outputPath);
		if (outputFile.exists()) {
			throw new IOException("Output file already exists");
		}
		try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(outputFile.toPath()))) {
			byte[] buffer = new byte[1024];
			for (File file : files) {
				if (file == null || !file.exists()) {
					continue;
				}
				String basePath = file.isDirectory() ? file.getCanonicalPath() : file.getParentFile().getCanonicalPath();
				Stack<File> stack = new Stack<>();
				stack.push(file);
				while (!stack.isEmpty()) {
					File currentFile = stack.pop();
					String entryName = currentFile.getCanonicalPath().substring(basePath.length() + 1).replace("\\", "/");
					if (currentFile.isDirectory()) {
						File[] subFiles = currentFile.listFiles();
						if (subFiles != null) {
							for (File subFile : subFiles) {
								stack.push(subFile);
							}
						}
						if (!entryName.isEmpty()) {
							if (!entryName.endsWith("/")) {
								entryName += "/";
							}
							jos.putNextEntry(new JarEntry(entryName));
							jos.closeEntry();
						}
					} else {
						FileInputStream fis = null;
						try {
							jos.putNextEntry(new JarEntry(entryName));
							fis = new FileInputStream(currentFile);
							int len;
							while ((len = fis.read(buffer)) > 0) {
								jos.write(buffer, 0, len);
							}
							jos.closeEntry();
						} finally {
							if (fis != null) {
								fis.close();
							}
						}
					}
				}
			}
		}
	}

	private static void decompressZip(File archive, String directory) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(Files.newInputStream(archive.toPath()));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = new File(directory, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				if (!newFile.isDirectory() && !newFile.mkdirs()) {
					throw new IOException("Failed to create directory " + newFile);
				}
			} else {
				File parent = newFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}

	private static void decompress7z(File archive, String directory) throws IOException {
		SevenZFile sevenZFile = new SevenZFile(archive);
		SevenZArchiveEntry entry;
		byte[] buffer = new byte[8192];
		while ((entry = sevenZFile.getNextEntry()) != null) {
			File newFile = new File(directory, entry.getName());
			if (entry.isDirectory()) {
				if (!newFile.isDirectory() && !newFile.mkdirs()) {
					throw new IOException("Failed to create directory " + newFile);
				}
			} else {
				File parent = newFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}
				OutputStream out = Files.newOutputStream(newFile.toPath());
				int bytesRead;
				while ((bytesRead = sevenZFile.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
				out.close();
			}
		}
		sevenZFile.close();
	}

	private static void decompressTar(File archive, String directory) throws IOException {
		TarArchiveInputStream tis = new TarArchiveInputStream(Files.newInputStream(archive.toPath()));
		TarArchiveEntry entry;
		byte[] buffer = new byte[8192];
		while ((entry = tis.getNextTarEntry()) != null) {
			File newFile = new File(directory, entry.getName());
			if (entry.isDirectory()) {
				if (!newFile.isDirectory() && !newFile.mkdirs()) {
					throw new IOException("Failed to create directory " + newFile);
				}
			} else {
				File parent = newFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}
				OutputStream out = Files.newOutputStream(newFile.toPath());
				int len;
				while ((len = tis.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.close();
			}
		}
		tis.close();
	}

	private static void decompressGzip(File archive, String directory) throws IOException {
		byte[] buffer = new byte[8192];
		InputStream fi = Files.newInputStream(archive.toPath());
		InputStream bi = new BufferedInputStream(fi);
		InputStream gzi = new GzipCompressorInputStream(bi);
		String fileName = archive.getName();
		if (fileName.endsWith(".gz")) {
			fileName = fileName.substring(0, fileName.length() - 3);
		}
		File outputFile = new File(directory, fileName);
		File parent = outputFile.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		OutputStream fo = Files.newOutputStream(outputFile.toPath());
		int len;
		while ((len = gzi.read(buffer)) != -1) {
			fo.write(buffer, 0, len);
		}
		fo.close();
		gzi.close();
	}

	private static void decompressJar(File archive, String directory) throws IOException {
		byte[] buffer = new byte[1024];
		JarFile jarFile = new JarFile(archive);
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			File newFile = new File(directory, entry.getName());
			if (entry.isDirectory()) {
				if (!newFile.isDirectory() && !newFile.mkdirs()) {
					throw new IOException("Failed to create directory " + newFile);
				}
			} else {
				File parent = newFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}
				InputStream is = jarFile.getInputStream(entry);
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = is.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				is.close();
			}
		}
		jarFile.close();
	}
}