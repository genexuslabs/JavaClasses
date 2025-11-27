package com.genexus.compression;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.FileUtils.getFile;
import static org.apache.commons.io.FilenameUtils.getExtension;


public class CompressionUtils {

	private static final int BUFFER_SIZE = 8192;

	/**
	 * Counts the number of entries in an archive file.
	 *
	 * @param archiveFile The archive file to analyze
	 * @return The number of entries in the archive
	 * @throws IOException If an I/O error occurs
	 */
	public static int countArchiveEntries(File archiveFile) throws IOException {
		String extension = getExtension(archiveFile.getName()).toLowerCase();
		int count = 0;

		switch (extension) {
			case "zip":
				try (ZipFile zipFile = new ZipFile(archiveFile)) {
					return zipFile.size();
				}
			case "7z":
				try (SevenZFile sevenZFile = getSevenZFile(archiveFile)) {
					while (sevenZFile.getNextEntry() != null) {
						count++;
					}
					return count;
				}
			case "tar":
				try (TarArchiveInputStream tarStream = new TarArchiveInputStream(Files.newInputStream(archiveFile.toPath()))) {
					while (tarStream.getNextEntry() != null) {
						count++;
					}
					return count;
				}
			case "gz":
				return 1;
			case "jar":
				try (JarFile jarFile = new JarFile(archiveFile)) {
					return jarFile.size();
				}
			default:
				throw new IllegalArgumentException("Unsupported archive format: " + extension);
		}
	}

	/**
	 * Checks if an archive is safe to extract (no path traversal/zip slip).
	 *
	 * @param archiveFile The archive file to check
	 * @param targetDir The target directory for extraction
	 * @return true if the archive is safe, false otherwise
	 * @throws IOException If an I/O error occurs
	 */
	public static boolean isArchiveSafe(File archiveFile, String targetDir) throws IOException {
		String extension = getExtension(archiveFile.getName()).toLowerCase();
		File targetPath = new File(targetDir).getCanonicalFile();

		switch (extension) {
			case "zip":
				try (ZipFile zipFile = new ZipFile(archiveFile)) {
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						if (!isEntryPathSafe(targetPath, entry.getName())) {
							return false;
						}
					}
				}
				return true;
			case "7z":
				try (SevenZFile sevenZFile = getSevenZFile(archiveFile)) {
					SevenZArchiveEntry entry;
					while ((entry = sevenZFile.getNextEntry()) != null) {
						if (!isEntryPathSafe(targetPath, entry.getName())) {
							return false;
						}
					}
				}
				return true;
			case "tar":
				try (TarArchiveInputStream tarStream = new TarArchiveInputStream(Files.newInputStream(archiveFile.toPath()))) {
					TarArchiveEntry entry;
					while ((entry = tarStream.getNextEntry()) != null) {
						if (!isEntryPathSafe(targetPath, entry.getName())) {
							return false;
						}
					}
				}
				return true;
			case "gz":
				String fileName = archiveFile.getName();
				if (fileName.endsWith(".gz") && fileName.length() > 3) {
					String extractedName = fileName.substring(0, fileName.length() - 3);
					return isEntryPathSafe(targetPath, extractedName);
				}
				return true;
			case "jar":
				try (JarFile jarFile = new JarFile(archiveFile)) {
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (!isEntryPathSafe(targetPath, entry.getName())) {
							return false;
						}
					}
				}
				return true;
			default:
				throw new IllegalArgumentException("Unsupported archive format: " + extension);
		}
	}

	/**
	 * Gets the maximum file size of any entry in the archive.
	 *
	 * @param archiveFile The archive file to analyze
	 * @return The size of the largest file in the archive
	 * @throws IOException If an I/O error occurs
	 */
	public static long getMaxFileSize(File archiveFile) throws IOException {
		String extension = getExtension(archiveFile.getName()).toLowerCase();
		long maxSize = 0;

		switch (extension) {
			case "zip":
				try (ZipFile zipFile = new ZipFile(archiveFile)) {
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						if (!entry.isDirectory() && entry.getSize() > maxSize) {
							maxSize = entry.getSize();
						}
					}
				}
				break;
			case "7z":
				try (SevenZFile sevenZFile = getSevenZFile(archiveFile)) {
					SevenZArchiveEntry entry;
					while ((entry = sevenZFile.getNextEntry()) != null) {
						if (!entry.isDirectory() && entry.getSize() > maxSize) {
							maxSize = entry.getSize();
						}
					}
				}
				break;
			case "tar":
				try (TarArchiveInputStream tarStream = new TarArchiveInputStream(Files.newInputStream(archiveFile.toPath()))) {
					TarArchiveEntry entry;
					while ((entry = tarStream.getNextEntry()) != null) {
						if (!entry.isDirectory() && entry.getSize() > maxSize) {
							maxSize = entry.getSize();
						}
					}
				}
				break;
			case "gz":
				try (GZIPInputStream gzStream = new GZIPInputStream(Files.newInputStream(archiveFile.toPath()))) {
					byte[] buffer = new byte[BUFFER_SIZE];
					long size = 0;
					int n;
					while ((n = gzStream.read(buffer)) != -1) {
						size += n;
					}
					maxSize = size;
				}
				break;
			case "jar":
				try (JarFile jarFile = new JarFile(archiveFile)) {
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (!entry.isDirectory() && entry.getSize() > maxSize) {
							maxSize = entry.getSize();
						}
					}
				}
				break;
			default:
				throw new IllegalArgumentException("Unsupported archive format: " + extension);
		}

		return maxSize;
	}

	/**
	 * Estimates the total size of all files after decompression.
	 *
	 * @param archiveFile The archive file to analyze
	 * @return The estimated total size after decompression
	 * @throws IOException If an I/O error occurs
	 */
	public static long estimateDecompressedSize(File archiveFile) throws IOException {
		String extension = getExtension(archiveFile.getName()).toLowerCase();
		long totalSize = 0;

		switch (extension) {
			case "zip":
				try (ZipFile zipFile = new ZipFile(archiveFile)) {
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						if (!entry.isDirectory()) {
							long size = entry.getSize();
							if (size != -1) {
								totalSize += size;
							} else {
								totalSize += entry.getCompressedSize() * 3;
							}
						}
					}
				}
				break;
			case "7z":
				try (SevenZFile sevenZFile = getSevenZFile(archiveFile)) {
					SevenZArchiveEntry entry;
					while ((entry = sevenZFile.getNextEntry()) != null) {
						if (!entry.isDirectory()) {
							totalSize += entry.getSize();
						}
					}
				}
				break;
			case "tar":
				try (TarArchiveInputStream tarStream = new TarArchiveInputStream(Files.newInputStream(archiveFile.toPath()))) {
					TarArchiveEntry entry;
					while ((entry = tarStream.getNextEntry()) != null) {
						if (!entry.isDirectory()) {
							totalSize += entry.getSize();
						}
					}
				}
				break;
			case "gz":
				try (RandomAccessFile raf = new RandomAccessFile(archiveFile, "r")) {
					raf.seek(raf.length() - 4);
					int b4 = raf.read();
					int b3 = raf.read();
					int b2 = raf.read();
					int b1 = raf.read();
					if (b1 != -1 && b2 != -1 && b3 != -1 && b4 != -1) {
						long size = ((long) b1 << 24) | ((long) b2 << 16) | ((long) b3 << 8) | b4;
						if (size > 0) {
							totalSize = size;
						} else {
							totalSize = archiveFile.length() * 5;
						}
					} else {
						totalSize = archiveFile.length() * 5;
					}
				} catch (Exception e) {
					totalSize = archiveFile.length() * 5;
				}
				break;
			case "jar":
				try (JarFile jarFile = new JarFile(archiveFile)) {
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (!entry.isDirectory()) {
							long size = entry.getSize();
							if (size != -1) {
								totalSize += size;
							} else {
								totalSize += entry.getCompressedSize() * 3;
							}
						}
					}
				}
				break;
			default:
				throw new IllegalArgumentException("Unsupported archive format: " + extension);
		}

		return totalSize;
	}

	private static SevenZFile getSevenZFile(File archiveFile) throws IOException {
		return SevenZFile.builder().setFile(archiveFile).get();
	}

	private static boolean isEntryPathSafe(File targetPath, String entryName) throws IOException {
		File destinationFile = new File(targetPath, entryName).getCanonicalFile();
		return destinationFile.getPath().startsWith(targetPath.getPath() + File.separator) || destinationFile.getPath().equals(targetPath.getPath());
	}

	public static boolean isPathTraversal(String dir, String fName) {
		try {
			Path path = Paths.get(dir).resolve(fName);
			return !path.toAbsolutePath().equals(path.toRealPath());
		}catch (Exception e){
			return true;
		}
	}
}