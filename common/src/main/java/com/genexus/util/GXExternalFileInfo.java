package com.genexus.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import com.genexus.db.driver.*;
import com.genexus.common.interfaces.SpecificImplementation;

import java.util.Date;

public class GXExternalFileInfo implements IGXFileInfo {
    private static int DEFAULT_OBJECT_EXPIRATION_MINUTES = -1; //Specified by Provider
    String name;
    ExternalProvider provider;
    String url;
    boolean isFile;
    ResourceAccessControlList fileAcl;

    public GXExternalFileInfo(String storageObjectFullName, String url, ExternalProvider provider) {
		setName(storageObjectFullName);
        this.url = url;
        this.provider = provider;
        this.isFile = true;
    }

    public GXExternalFileInfo(String storageObjectFullName, String url, ExternalProvider provider, ResourceAccessControlList acl) {
        this.url = url;
        this.provider = provider;
        this.fileAcl = acl;
		setName(storageObjectFullName);
    }

     public GXExternalFileInfo(String storageObjectFullName, ExternalProvider provider, boolean isFile) {
        this.url = "";
        this.provider = provider;
        this.isFile = isFile;
		setName(storageObjectFullName);
    }

    public GXExternalFileInfo(String storageObjectFullName, ExternalProvider provider, boolean isFile, ResourceAccessControlList acl) {
        this.url = "";
        this.provider = provider;
        this.isFile = isFile;
        this.fileAcl = acl;
		setName(storageObjectFullName);
    }

     public GXExternalFileInfo(String storageObjectFullName, ExternalProvider provider) {
        this.url = "";
        this.provider = provider;
        this.isFile = true;
		setName(storageObjectFullName);
    }

    private void setName(String objectNameOrUrl) {
    	this.name = ExternalProviderCommon.getProviderObjectAbsoluteUriSafe(this.provider, objectNameOrUrl);
	}

    public String getPath() {
    	if(isDirectory())
				return provider.getDirectory(name);
			else
				return getParent();
    }

    public boolean exists() {
        if (isFile) {
            return (provider != null && provider.exists(name, fileAcl));
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
            provider.upload(name, input, fileAcl);
            return true;
        }
        return false;
    }

    public boolean delete() {
        if(isFile){
            provider.delete(name, fileAcl);
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
            provider.rename(name, fileName, fileAcl);
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
                return provider.get(name, fileAcl, DEFAULT_OBJECT_EXPIRATION_MINUTES);
        else
            return url;
    }

    public long length() {
        return provider.getLength(name, fileAcl);
    }

    public Date lastModified() {
        return provider.getLastModified(name, fileAcl);
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
        return provider.getStream(name, fileAcl);
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
        provider.copy(origin, destination, fileAcl);
    }
}
