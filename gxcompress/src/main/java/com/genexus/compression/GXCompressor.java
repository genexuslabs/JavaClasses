package com.genexus.compression;

import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.StructSdtMessages_Message;
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
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.*;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class GXCompressor {

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(GXCompressor.class);

	private static final String GENERIC_ERROR = "An error occurred during the compression/decompression process: ";
	private static final String NO_FILES_ADDED = "No files have been added for compression.";
	private static final String FILE_NOT_EXISTS = "File does not exist: ";
	private static final String UNSUPPORTED_FORMAT = " is an unsupported format. Supported formats are zip, 7z, tar, gz and jar.";
	private static final String EMPTY_FILE = "The selected file is empty: ";
	private static final String PURGED_ARCHIVE = "After performing security checks, no valid files where left to compress";
	private static final String DIRECTORY_ATTACK = "Potential directory traversal attack detected: ";
	private static final String MAX_FILESIZE_EXCEEDED = "The file(s) selected for (de)compression exceed the maximum permitted file size of ";
	private static final String TOO_MANY_FILES = "Too many files have been added for (de)compression. Maximum allowed is ";
	private static final String ZIP_SLIP_DETECTED = "Zip slip or path traversal attack detected in archive: ";
	private static final String BIG_SINGLE_FILE = "Individual file exceeds maximum allowed size: ";
	private static final String PROCESSING_ERROR = "Error checking archive safety for file: ";
	private static final String ARCHIVE_SIZE_ESTIMATION_ERROR = "";

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

	/**
	 * Compresses specified files into an archive at the given path based on configuration parameters.
	 *
	 * @param files List of file paths to compress
	 * @param path Target path for the compressed archive
	 * @param configuration Configuration parameters for compression
	 * @param messages Collection to store output messages
	 * @return Boolean indicating success or failure of compression operation
	 */
	public static Boolean compress(ArrayList<String> files, String path, CompressionConfiguration configuration, GXBaseCollection<SdtMessages_Message>[] messages) {
		if (files.isEmpty()) {
			log.error(NO_FILES_ADDED);
			storageMessages(NO_FILES_ADDED, messages[0]);
			return false;
		}
		List<File> validFiles = new ArrayList<>();
		long totalSize = 0;
		for (String filePath : files) {
			File file = new File(filePath);
			try {
				String normalizedPath = file.getCanonicalPath();
				if (!file.exists()) {
					log.error("{}{}", FILE_NOT_EXISTS, filePath);
					storageMessages(FILE_NOT_EXISTS + filePath, messages[0]);
					continue;
				}
				File absFile = file.getAbsoluteFile();
				if (!normalizedPath.startsWith(absFile.getParentFile().getCanonicalPath())) {
					log.error(DIRECTORY_ATTACK + "{}", filePath);
					storageMessages(DIRECTORY_ATTACK + filePath, messages[0]);
					return false;
				}
				long fileSize = file.length();
				if (configuration.maxIndividualFileSize > -1 && fileSize > configuration.maxIndividualFileSize) {
					log.error(BIG_SINGLE_FILE + filePath);
					storageMessages(BIG_SINGLE_FILE + filePath, messages[0]);
					continue;
				}
				totalSize += fileSize;
				validFiles.add(file);
			} catch (IOException e) {
				log.error("Error normalizing path for file: {}", filePath, e);
				storageMessages("Error normalizing path for file: " + filePath, messages[0]);
				return false;
			}
		}
		if (validFiles.isEmpty()) {
			log.error(PURGED_ARCHIVE);
			storageMessages(PURGED_ARCHIVE, messages[0]);
			return false;
		}
		if (configuration.maxCombinedFileSize > -1 && totalSize > configuration.maxCombinedFileSize) {
			log.error(MAX_FILESIZE_EXCEEDED + configuration.maxCombinedFileSize);
			storageMessages(MAX_FILESIZE_EXCEEDED + configuration.maxCombinedFileSize, messages[0]);
			return false;
		}
		if (configuration.maxFileCount > -1 && validFiles.size() > configuration.maxFileCount) {
			log.error(TOO_MANY_FILES + configuration.maxFileCount);
			storageMessages(TOO_MANY_FILES + configuration.maxFileCount, messages[0]);
			return false;
		}
		try {
			File targetFile = new File(path);
			File targetDir = targetFile.getParentFile();
			if (path.contains("/../") || path.contains("../") || path.contains("/..")) {
				log.error(DIRECTORY_ATTACK + path);
				storageMessages(DIRECTORY_ATTACK + path, messages[0]);
				return false;
			}
			if (configuration.targetDirectory != null && !configuration.targetDirectory.isEmpty()) {
				File configTargetDir = new File(configuration.targetDirectory);
				String normalizedTargetPath = targetDir.getCanonicalPath();
				String normalizedConfigPath = configTargetDir.getCanonicalPath();
				if (!normalizedTargetPath.startsWith(normalizedConfigPath)) {
					log.error(DIRECTORY_ATTACK + path);
					storageMessages(DIRECTORY_ATTACK + path, messages[0]);
					return false;
				}
			}
		} catch (IOException e) {
			log.error("Error validating target path: {}", path, e);
			storageMessages("Error validating target path: " + path, messages[0]);
			return false;
		}
		File[] toCompress = validFiles.toArray(new File[0]);
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
					log.error(format + UNSUPPORTED_FORMAT);
					storageMessages(format + UNSUPPORTED_FORMAT, messages[0]);
					return false;
			}
			return true;
		} catch (Exception e) {
			log.error(GENERIC_ERROR, e);
			storageMessages(e.getMessage(), messages[0]);
			return false;
		}
	}

	/**
	 * Compresses files interactively, add files to a collection until the GXCompressor.compress method is executed
	 *
	 * @param path Target path for the compressed archive
	 * @param configuration Configuration parameters for decompression
	 * @param messages Collection to store output messages
	 * @return Boolean indicating success or failure of decompression operation
	 */
	public static Compression newCompression(String path, CompressionConfiguration configuration, GXBaseCollection<SdtMessages_Message>[] messages) {
		return new Compression(path, configuration, messages);
	}

	/**
	 * Decompresses an archive file to the specified path based on configuration parameters.
	 *
	 * @param file Path to the archive file to decompress
	 * @param path Target path for the decompressed files
	 * @param configuration Configuration parameters for decompression
	 * @param messages Collection to store output messages
	 * @return Boolean indicating success or failure of decompression operation
	 */
	public static Boolean decompress(String file, String path, CompressionConfiguration configuration, GXBaseCollection<SdtMessages_Message>[] messages) {
		File archiveFile = new File(file);
		if (!archiveFile.exists()) {
			log.error("{}{}", FILE_NOT_EXISTS, archiveFile.getAbsolutePath());
			storageMessages(FILE_NOT_EXISTS + archiveFile.getAbsolutePath(), messages[0]);
			return false;
		}
		if (archiveFile.length() == 0L) {
			log.error("{}{}", EMPTY_FILE, file);
			storageMessages(EMPTY_FILE + file, messages[0]);
			return false;
		}
		int fileCount;
		try {
			fileCount = CompressionUtils.countArchiveEntries(archiveFile);
			if (fileCount <= 0) {
				log.error("{}{}", EMPTY_FILE, file);
				storageMessages(EMPTY_FILE + file, messages[0]);
				return false;
			}
		} catch (Exception e) {
			log.error(PROCESSING_ERROR + file, e);
			storageMessages(PROCESSING_ERROR + file, messages[0]);
			return false;
		}
		try {
			File targetDir = new File(path);
			if (path.contains("/../") || path.contains("../") || path.contains("/..")) {
				log.error(DIRECTORY_ATTACK + path);
				storageMessages(DIRECTORY_ATTACK + path, messages[0]);
				return false;
			}
			if (configuration.targetDirectory != null && !configuration.targetDirectory.isEmpty()) {
				File configTargetDir = new File(configuration.targetDirectory);
				String normalizedTargetPath = targetDir.getCanonicalPath();
				String normalizedConfigPath = configTargetDir.getCanonicalPath();

				if (!normalizedTargetPath.startsWith(normalizedConfigPath)) {
					log.error(DIRECTORY_ATTACK + path);
					storageMessages(DIRECTORY_ATTACK + path, messages[0]);
					return false;
				}
			}
		} catch (IOException e) {
			log.error("Error validating target path: {}", path, e);
			storageMessages("Error validating target path: " + path, messages[0]);
			return false;
		}
		try {
			if (!CompressionUtils.isArchiveSafe(archiveFile, path)) {
				log.error(ZIP_SLIP_DETECTED + file);
				storageMessages(ZIP_SLIP_DETECTED + file, messages[0]);
				return false;
			}
		} catch (Exception e) {
			log.error(PROCESSING_ERROR + file, e);
			storageMessages(PROCESSING_ERROR + file, messages[0]);
			return false;
		}
		try {
			if (configuration.maxIndividualFileSize > -1) {
				long maxFileSize = CompressionUtils.getMaxFileSize(archiveFile);
				if (maxFileSize > configuration.maxIndividualFileSize) {
					log.error(BIG_SINGLE_FILE + maxFileSize + " bytes");
					storageMessages(BIG_SINGLE_FILE + maxFileSize + " bytes", messages[0]);
					return false;
				}
			}
			if (configuration.maxCombinedFileSize > -1) {
				long totalSizeEstimate = CompressionUtils.estimateDecompressedSize(archiveFile);
				if (totalSizeEstimate > configuration.maxCombinedFileSize) {
					log.error(MAX_FILESIZE_EXCEEDED + configuration.maxCombinedFileSize);
					storageMessages(MAX_FILESIZE_EXCEEDED + configuration.maxCombinedFileSize, messages[0]);
					return false;
				}
			}
		} catch (Exception e) {
			log.error(ARCHIVE_SIZE_ESTIMATION_ERROR + file, e);
			storageMessages("Error estimating archive size: " + file, messages[0]);
			return false;
		}
		if (configuration.maxFileCount > -1 && fileCount > configuration.maxFileCount) {
			log.error(TOO_MANY_FILES + configuration.maxFileCount);
			storageMessages(TOO_MANY_FILES + configuration.maxFileCount, messages[0]);
			return false;
		}
		String extension = getExtension(archiveFile.getName());
		try {
			switch (extension.toLowerCase()) {
				case "zip":
					decompressZip(archiveFile, path);
					break;
				case "7z":
					decompress7z(archiveFile, path);
					break;
				case "tar":
					decompressTar(archiveFile, path);
					break;
				case "gz":
					decompressGzip(archiveFile, path);
					break;
				case "jar":
					decompressJar(archiveFile, path);
					break;
				default:
					log.error(extension + UNSUPPORTED_FORMAT);
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
		if (files == null || files.length == 0) {
			throw new IllegalArgumentException("No files to compress");
		}
		if (outputPath == null || outputPath.isEmpty()) {
			throw new IllegalArgumentException("Output path is null or empty");
		}
		File outputFile = new File(outputPath);
		if (outputFile.exists() && !outputFile.canWrite()) {
			throw new IOException("Cannot write to output file");
		}
		File parentDir = outputFile.getParentFile();
		if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
			throw new IOException("Failed to create output directory");
		}
		boolean singleFile = files.length == 1 && files[0].isFile();
		File tempFile = File.createTempFile("compress_", ".tmp", parentDir);
		if (singleFile) {
			try (
				FileInputStream fis = new FileInputStream(files[0]);
				FileOutputStream fos = new FileOutputStream(tempFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos)
			) {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = fis.read(buffer)) != -1) {
					gcos.write(buffer, 0, len);
				}
			}
		} else {
			try (
				FileOutputStream fos = new FileOutputStream(tempFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos);
				TarArchiveOutputStream taos = new TarArchiveOutputStream(gcos)
			) {
				taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
				Stack<File> fileStack = new Stack<>();
				Stack<String> pathStack = new Stack<>();
				for (File f : files) {
					if (f != null) {
						fileStack.push(f);
						pathStack.push("");
					}
				}
				while (!fileStack.isEmpty()) {
					File currentFile = fileStack.pop();
					String path = pathStack.pop();
					String entryName = path + currentFile.getName();
					if (currentFile.isDirectory()) {
						File[] children = currentFile.listFiles();
						if (children != null && children.length > 0) {
							for (File child : children) {
								fileStack.push(child);
								pathStack.push(entryName + "/");
							}
						} else {
							TarArchiveEntry entry = new TarArchiveEntry(entryName + "/");
							taos.putArchiveEntry(entry);
							taos.closeArchiveEntry();
						}
					} else {
						TarArchiveEntry entry = new TarArchiveEntry(currentFile, entryName);
						taos.putArchiveEntry(entry);
						try (FileInputStream fis = new FileInputStream(currentFile)) {
							byte[] buffer = new byte[8192];
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
		if (!tempFile.exists()) {
			throw new IOException("Failed to create the archive");
		}
		String finalName = outputPath;
		if (singleFile) {
			if (!finalName.toLowerCase().endsWith(".gz")) {
				finalName += ".gz";
			}
		} else {
			if (finalName.toLowerCase().endsWith(".tar.gz")) {
				// do nothing
			} else if (finalName.toLowerCase().endsWith(".gz")) {
				finalName = finalName.substring(0, finalName.length() - 3) + ".tar.gz";
			} else {
				finalName += ".tar.gz";
			}
		}
		File finalFile = new File(finalName);
		if (finalFile.exists() && !finalFile.delete()) {
			throw new IOException("Failed to delete existing file with desired name");
		}
		if (!tempFile.renameTo(finalFile)) {
			throw new IOException("Failed to rename archive to desired name");
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
		while ((entry = tis.getNextEntry()) != null) {
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
		if (!archive.exists() || !archive.isFile()) {
			throw new IllegalArgumentException("The archive file does not exist or is not a file.");
		}
		File targetDir = new File(directory);
		if (!targetDir.exists() || !targetDir.isDirectory()) {
			throw new IllegalArgumentException("The specified directory does not exist or is not a directory.");
		}
		File tempFile = File.createTempFile("decompressed_", ".tmp");
		try (
			FileInputStream fis = new FileInputStream(archive);
			GZIPInputStream gzipInputStream = new GZIPInputStream(fis);
			FileOutputStream fos = new FileOutputStream(tempFile)
		) {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = gzipInputStream.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
		}

		boolean isTar = false;
		try (FileInputStream tempFis = new FileInputStream(tempFile);
			 TarArchiveInputStream testTar = new TarArchiveInputStream(tempFis)) {
			TarArchiveEntry testEntry = testTar.getNextEntry();
			if (testEntry != null) {
				isTar = true;
			}
		} catch (IOException ignored) {}
		if (isTar) {
			try (FileInputStream tarFis = new FileInputStream(tempFile);
				 TarArchiveInputStream tarInput = new TarArchiveInputStream(tarFis)) {

				TarArchiveEntry entry;
				while ((entry = tarInput.getNextEntry()) != null) {
					File outFile = new File(targetDir, entry.getName());
					if (entry.isDirectory()) {
						if (!outFile.exists() && !outFile.mkdirs()) {
							throw new IOException("Failed to create directory: " + outFile);
						}
					} else {
						File parent = outFile.getParentFile();
						if (!parent.exists() && !parent.mkdirs()) {
							throw new IOException("Failed to create directory: " + parent);
						}
						try (FileOutputStream os = new FileOutputStream(outFile)) {
							byte[] buffer = new byte[8192];
							int count;
							while ((count = tarInput.read(buffer)) != -1) {
								os.write(buffer, 0, count);
							}
						}
					}
				}
			}
		} else {
			String name = archive.getName();
			if (name.toLowerCase().endsWith(".gz")) {
				name = name.substring(0, name.length() - 3);
			}
			File singleOutFile = new File(targetDir, name);
			if (!tempFile.renameTo(singleOutFile)) {
				try (
					FileInputStream in = new FileInputStream(tempFile);
					FileOutputStream out = new FileOutputStream(singleOutFile)
				) {
					byte[] buffer = new byte[8192];
					int len;
					while ((len = in.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
				}
			}
		}
		if (!tempFile.delete()) {
			tempFile.deleteOnExit();
		}
	}

	private static void decompressJar(File archive, String directory) throws IOException {
		if (!archive.exists() || !archive.isFile()) {
			throw new IOException("Invalid archive file.");
		}
		File targetDir = new File(directory);
		if (!targetDir.exists()) {
			if (!targetDir.mkdirs()) {
				throw new IOException("Failed to create target directory.");
			}
		}
		try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(archive.toPath()))) {
			JarEntry entry;
			while ((entry = jarInputStream.getNextJarEntry()) != null) {
				File outputFile = new File(targetDir, entry.getName());
				if (entry.isDirectory()) {
					if (!outputFile.exists() && !outputFile.mkdirs()) {
						throw new IOException("Failed to create directory: " + outputFile.getAbsolutePath());
					}
				} else {
					File parent = outputFile.getParentFile();
					if (!parent.exists() && !parent.mkdirs()) {
						throw new IOException("Failed to create parent directory: " + parent.getAbsolutePath());
					}
					try (FileOutputStream fos = new FileOutputStream(outputFile)) {
						byte[] buffer = new byte[1024];
						int bytesRead;
						while ((bytesRead = jarInputStream.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
					}
				}
				jarInputStream.closeEntry();
			}
		}
	}
}
