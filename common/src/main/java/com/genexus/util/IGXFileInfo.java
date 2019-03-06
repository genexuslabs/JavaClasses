package com.genexus.util;
import java.io.*;
import java.util.Vector;
import com.genexus.CommonUtil;
import java.util.Date;

public interface IGXFileInfo {

	public String getPath(); 
	public boolean exists();
	public boolean isFile();
	public boolean isDirectory();
	public boolean mkdir();
	public boolean createNewFile() throws IOException;
    public boolean createNewFile(InputStream input) throws IOException;
	public boolean delete();
	public String getParent();
	public boolean renameTo(String filename);
	public String getName();
	public String getFilePath();
	public String getAbsolutePath();
	public long length();
	public Date lastModified();
	public void copy(String origin, String destination) throws java.io.IOException;
	public GXFileCollection listFiles(String filter);
	public GXFileCollection listFiles();
	public String[] list();
	public GXDirectoryCollection listDirectories();
	public byte [] toBytes() throws IOException;
	public void fromBytes(byte[] data) throws IOException;
	public String readAllText(String encoding) throws IOException;
	public java.util.List<String> readLines(String encoding) throws IOException;
	public void writeStringToFile(String value, String encoding, boolean append) throws Exception;
	public void writeLines(String encoding, Vector value, boolean append) throws Exception;
	public File getFileInstance();
	public InputStream getStream();
	public String getSeparator();
}
