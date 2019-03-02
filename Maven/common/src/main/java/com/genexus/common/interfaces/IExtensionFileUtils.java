package com.genexus.common.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.genexus.common.classes.AbstractGXFile;

public interface IExtensionFileUtils {

	String readFileToString(File fileSource, String normalizeEncodingName) throws IOException;

	List<String> readLines(File fileSource, String normalizeEncodingName) throws IOException;

	void writeStringToFile(File fileSource, String value, String encoding, boolean append) throws IOException;

	void writeLines(File fileSource, String encoding, Vector value, boolean append) throws IOException;

	String getTempFileName(String string);

	void copyFile(File file, File file2) throws IOException;

	AbstractGXFile createFile(String absolutePath, boolean b, boolean c);

}
