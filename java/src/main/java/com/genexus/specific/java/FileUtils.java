package com.genexus.specific.java;

import com.genexus.common.classes.AbstractGXFile;
import com.genexus.db.driver.ResourceAccessControlList;
import com.genexus.util.GXFile;
import com.genexus.util.GxFileInfoSourceType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class FileUtils implements com.genexus.common.interfaces.IExtensionFileUtils {

	@Override
	public String readFileToString(File fileSource, String normalizeEncodingName) throws IOException {
		return org.apache.commons.io.FileUtils.readFileToString(fileSource, normalizeEncodingName);
	}

	@Override
	public List<String> readLines(File fileSource, String normalizeEncodingName) throws IOException {
		
		return org.apache.commons.io.FileUtils.readLines(fileSource, normalizeEncodingName);
	}

	@Override
	public void writeStringToFile(File fileSource, String value, String encoding, boolean append) throws IOException {
		org.apache.commons.io.FileUtils.writeStringToFile(fileSource, value, encoding, append);
		
	}

	@Override
	public void writeLines(File fileSource, String encoding, Vector value, boolean append) throws IOException {
		org.apache.commons.io.FileUtils.writeLines(fileSource, encoding, value, append);
	}

	@Override
	public String getTempFileName(String extension) {
		
		return com.genexus.PrivateUtilities.getTempFileName(extension);
	}

	@Override
	public void copyFile(File file, File file2) throws IOException {
		org.apache.commons.io.FileUtils.copyFile(file, file2);
		
	}

	@Override
	public AbstractGXFile createFile(String file, ResourceAccessControlList acl, boolean local) {
		return new GXFile(file, acl, local ? GxFileInfoSourceType.LocalFile: GxFileInfoSourceType.Unknown);
	}

}
