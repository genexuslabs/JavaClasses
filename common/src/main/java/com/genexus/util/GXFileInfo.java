package com.genexus.util;
import java.io.*;
import java.util.Vector;
import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.IOCase;

import java.util.Date;

public class GXFileInfo implements IGXFileInfo {

	private File fileSource;
	private boolean isDirectory;

	public GXFileInfo(File file){
		this(file, false);
	}
	public GXFileInfo(File file, boolean isDirectory){
		fileSource = file;
		this.isDirectory = isDirectory;
	}
	public String getPath(){
		if (fileSource.isFile()){
			String absoluteName = getAbsolutePath();
			if (!absoluteName.equals(""))
				return new File(absoluteName).getParent();
			else
				return "";  
		}else{
			return fileSource.getPath();
		}
	}
	public boolean exists(){
		if ((isDirectory && fileSource.isDirectory()) || (!isDirectory && fileSource.isFile())) {
			return fileSource.exists();
		}
		return false;
	}
	public boolean isFile(){
		return fileSource.isFile();
	}
	public boolean isDirectory(){
		return fileSource.isDirectory();
	}
	public boolean mkdir(){
		return fileSource.mkdir();
	}
	public String[] list(){
		return fileSource.list();
	}
	public boolean createNewFile() throws IOException{
		return fileSource.createNewFile();
	}
    public boolean createNewFile(InputStream input) throws IOException{
    	fromBytes(SpecificImplementation.GXutil.toByteArray(input));
		return true;
	}
	public boolean delete(){
		return fileSource.delete();
	}
	public String getParent(){
		return fileSource.getParent();
	}
	public boolean renameTo(String fileName){
            boolean ok= fileSource.renameTo(new File(fileName));
            if(ok)
                fileSource = new File(fileName);
            return ok;
	}
	public String getName(){
		return fileSource.getName();
	}
	public String getFilePath(){
		return fileSource.getAbsolutePath();
	}	
	public String getAbsolutePath(){
		return fileSource.getAbsolutePath();
	}
	public long length(){
		return fileSource.length();
	}
	public Date lastModified(){
		return new Date(fileSource.lastModified());
	}
        public void copy(String origin, String destination)throws java.io.IOException{
           SpecificImplementation.FileUtils.copyFile(new java.io.File(origin), new java.io.File(destination));
        }
	public GXFileCollection listFiles(String strFilter){
		GXFileCollection gxfiles = null;

		File[] files;
		if (strFilter.isEmpty())
			files = fileSource.listFiles();
		else {
			strFilter = "*" + strFilter;
			FileFilter fileFilter = new WildcardFileFilter(strFilter, IOCase.INSENSITIVE);
			files = fileSource.listFiles(fileFilter);
		}
		if (files != null) 
		{
			gxfiles = new GXFileCollection();
			for (int i = 0; i < files.length; i++) 
			{
				if (files[i].isFile())
					gxfiles.add(SpecificImplementation.FileUtils.createFile(files[i].getAbsolutePath(), false, true));
			}
		}
		return gxfiles;
	}
	public GXFileCollection listFiles(){
		return listFiles(null);
	}

	public GXDirectoryCollection listDirectories(){
		GXDirectoryCollection gxdirectories = null;
		File[] files = fileSource.listFiles();
		if (files != null) 
		{
			gxdirectories = new GXDirectoryCollection();
			for (int i = 0; i < files.length; i++) 
			{
				if (files[i].isDirectory())
					gxdirectories.add(new GXDirectory(files[i].getAbsolutePath()));
			}
		}
		return gxdirectories;
	}
        
        public InputStream getStream(){
            try {
                return new FileInputStream(fileSource);
            } catch (FileNotFoundException ex) {
                return null;
            }
        }
        
		public String getSeparator() 
		{
			return File.separator;
    }

	public byte[] toBytes() throws IOException{
		byte[] data = new byte[0];
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileSource));
		data = CommonUtil.readToByteArray(bis);
		bis.close();
		return data;		
	}
	public void fromBytes(byte[] data) throws IOException
	{
		OutputStream destination = new BufferedOutputStream(new FileOutputStream(fileSource));
		destination.write(data, 0, data.length);
		destination.close();	
	}
	public String readAllText(String encoding)throws IOException{
		return SpecificImplementation.FileUtils.readFileToString(fileSource, CommonUtil.normalizeEncodingName(encoding));
	}
	public java.util.List<String> readLines(String encoding) throws IOException{
		return SpecificImplementation.FileUtils.readLines(fileSource, CommonUtil.normalizeEncodingName(encoding));
	}
	public void writeStringToFile(String value, String encoding, boolean append) throws Exception{
		SpecificImplementation.FileUtils.writeStringToFile(fileSource, value, encoding, append);
	}
	public void writeLines(String encoding, Vector value, boolean append) throws Exception{
		SpecificImplementation.FileUtils.writeLines(fileSource, encoding, value, append);
	}
	public File getFileInstance(){
		return fileSource;
	}
}
