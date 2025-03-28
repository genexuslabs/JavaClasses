package com.genexus.compression;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.FileUtils.getFile;
import static org.apache.commons.io.FilenameUtils.getExtension;

public class CompressionUtils {
	public static int countArchiveEntries(File toCompress) throws IOException {
		String ext = getExtension(toCompress.getName()).toLowerCase();
		switch (ext) {
			case "zip":
			case "jar": {
				int count = 0;
				try (ZipFile zip = new ZipFile(toCompress)) {
					Enumeration<? extends ZipEntry> entries = zip.entries();
					while (entries.hasMoreElements()) {
						entries.nextElement();
						count++;
					}
				}
				return count;
			}
			case "tar": {
				int count = 0;
				try (FileInputStream fis = new FileInputStream(toCompress);
					 TarArchiveInputStream tais = new TarArchiveInputStream(fis)) {
					while (tais.getNextEntry() != null) {
						count++;
					}
				}
				return count;
			}
			case "7z": {
				int count = 0;
				try (SevenZFile sevenZFile = getSevenZFile(toCompress.getAbsolutePath())) {
					while (sevenZFile.getNextEntry() != null) {
						count++;
					}
				}
				return count;
			}
			case "gz":
				return 1;
		}
		return 0;
	}
	public static boolean isArchiveSafe(File toCompress, String targetDir) throws IOException {
		String ext = getExtension(toCompress.getName()).toLowerCase();
		File target = new File(targetDir).getCanonicalFile();
		switch (ext) {
			case "zip":
			case "jar":
				try (ZipFile zip = new ZipFile(toCompress)) {
					Enumeration<? extends ZipEntry> entries = zip.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						File entryFile = new File(target, entry.getName());
						if (!entryFile.getCanonicalPath().startsWith(target.getCanonicalPath() + File.separator)) {
							return false;
						}
					}
				}
				return true;
			case "tar":
				try (FileInputStream fis = new FileInputStream(toCompress);
					 TarArchiveInputStream tais = new TarArchiveInputStream(fis)) {
					TarArchiveEntry entry;
					while ((entry = tais.getNextEntry()) != null) {
						File entryFile = new File(target, entry.getName());
						if (!entryFile.getCanonicalPath().startsWith(target.getCanonicalPath() + File.separator)) {
							return false;
						}
					}
				}
				return true;
			case "7z":
				try (SevenZFile sevenZFile = getSevenZFile(toCompress.getAbsolutePath())) {
					SevenZArchiveEntry entry;
					while ((entry = sevenZFile.getNextEntry()) != null) {
						File entryFile = new File(target, entry.getName());
						if (!entryFile.getCanonicalPath().startsWith(target.getCanonicalPath() + File.separator)) {
							return false;
						}
					}
				}
				return true;
			case "gz":
				return true;
		}
		return true;
	}

	private static SevenZFile getSevenZFile(final String specialPath) throws IOException {
		return SevenZFile.builder().setFile(getFile(specialPath)).get();
	}
}
