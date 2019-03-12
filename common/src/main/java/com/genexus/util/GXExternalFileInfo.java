package com.genexus.util;

import java.io.*;
import java.util.Vector;
import com.genexus.db.driver.*;
import com.genexus.*;
import com.genexus.common.interfaces.SpecificImplementation;

import java.util.Date;

public class GXExternalFileInfo implements IGXFileInfo {

    String name;
    ExternalProvider provider;
    String url;
    boolean isFile;
    boolean isPrivate;

    public GXExternalFileInfo(String storageObjectFullName, String url, ExternalProvider provider) {
        name = storageObjectFullName;
        this.url = url;
        this.provider = provider;
        this.isFile = true;
    }

    public GXExternalFileInfo(String storageObjectFullName, String url, ExternalProvider provider, boolean isFile) {
        name = storageObjectFullName;
        this.url = url;
        this.provider = provider;
        this.isFile = isFile;
    }

     public GXExternalFileInfo(String storageObjectFullName, ExternalProvider provider, boolean isFile) {
        name = storageObjectFullName;
        this.url = "";
        this.provider = provider;
        this.isFile = isFile;
    }

    public GXExternalFileInfo(String storageObjectFullName, ExternalProvider provider, boolean isFile, boolean isPrivate) {
        name = storageObjectFullName;
        this.url = "";
        this.provider = provider;
        this.isFile = isFile;
        this.isPrivate = isPrivate;
    }

     public GXExternalFileInfo(String storageObjectFullName, ExternalProvider provider) {
        name = storageObjectFullName;
        this.url = "";
        this.provider = provider;
        this.isFile = true;
    }

    public String getPath() {
    	if(isDirectory())
				return provider.getDirectory(name);
			else
				return getParent();
    }

    public boolean exists() {
        if (isFile) {
            return (provider != null && provider.exists(name, isPrivate));
        } else {
            return (provider != null && provider.existsDirectory(name));
        }
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isDirectory() {
        return !isFile;
    }

    public boolean mkdir() {
        return false;
    }

    public String[] list() {
        return new String[0];
    }

    public boolean createNewFile() throws IOException {
        if (isDirectory()) {
            provider.createDirectory(name);
            return true;
        }
        return false;
    }

    public boolean createNewFile(InputStream input) throws IOException{
        if (isFile()) {
            provider.upload(name, input, isPrivate);
            return true;
        }
        return false;
    }

    public boolean delete() {
        if(isFile){
            provider.delete(name, isPrivate);
        }else{
            provider.deleteDirectory(name);
        }
        return true;
    }

    public String getParent() {
        if (name.contains("/")) {
            return name.substring(0, name.lastIndexOf("/"));
        } else {
            return "";
        }
    }

    public boolean renameTo(String fileName) {
        if(isFile){
            provider.rename(name, fileName, isPrivate);
            name=fileName;
        }else{
            provider.renameDirectory(name, fileName);
            name=fileName;
        }
        return true;
    }

    public String getName() {
			if (isDirectory() || !name.contains("/"))	{
				return name;
			}else{
				return name.substring(name.lastIndexOf("/") + 1, name.length());
			}
		}

    public String getFilePath() {
    		return name;
    }

    public String getAbsolutePath() {
        if(url.isEmpty())
            if(isDirectory())
                return provider.getDirectory(name);
            else
                return provider.get(name, isPrivate, 0);
        else
            return url;
    }

    public long length() {
        return provider.getLength(name, isPrivate);
    }

    public Date lastModified() {
        return provider.getLastModified(name, isPrivate);
    }

    public GXFileCollection listFiles(String filter) {
    	return (GXFileCollection) SpecificImplementation.GXExternalFileInfo.listFiles(filter, provider, name);
    }

    public GXFileCollection listFiles() {
    	return (GXFileCollection) SpecificImplementation.GXExternalFileInfo.listFiles(null, provider, name);
    }

    public GXDirectoryCollection listDirectories() {
        GXDirectoryCollection dirs = new GXDirectoryCollection ();
        for(String dir: provider.getSubDirectories(name)){
            dirs.add(new GXDirectory(new GXExternalFileInfo(dir, provider, false)));
        }
        return dirs;
    }

    public InputStream getStream(){
        return provider.getStream(name, isPrivate);
    }

	public String getSeparator(){
		return StorageUtils.DELIMITER;
	}

    public byte[] toBytes() throws IOException {
		InputStream stream  =getStream();
		byte[] bytes = SpecificImplementation.GXutil.toByteArray(stream);
		stream.close();
        return bytes;
    }

    public void fromBytes(byte[] data) throws IOException {

    }

    public String readAllText(String encoding) throws IOException {
        return "";
    }

    public java.util.List<String> readLines(String encoding) throws IOException {
        return null;
    }

    public void writeStringToFile(String value, String encoding, boolean append) throws Exception {
    }

    public void writeLines(String encoding, Vector value, boolean append) throws Exception {
    }

    public File getFileInstance() {
        return null;
    }

    public void copy(String origin, String destination){
        provider.copy(origin, destination, isPrivate);
    }
}
