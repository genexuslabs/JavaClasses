package com.genexus.db.driver;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import com.genexus.StructSdtMessages_Message;

public interface ExternalProvider {
   
    void download(String externalFileName, String localFile, boolean isPrivate);

    String upload(String localFile, String externalFileName, boolean isPrivate);
    
    String upload(String externalFileName, InputStream input, boolean isPrivate);

    String get(String externalFileName, boolean isPrivate, int expirationMinutes);

    void delete(String objectName, boolean isPrivate);

    String rename(String objectName, String newName, boolean isPrivate);

    String copy(String objectName, String newName, boolean isPrivate);
    
    String copy(String objectUrl, String newName, String tableName, String fieldName, boolean isPrivate);

    long getLength(String objectName, boolean isPrivate);

    Date getLastModified(String objectName, boolean isPrivate);
    
    boolean exists(String objectName, boolean isPrivate);

    String getDirectory(String directoryName);

    boolean existsDirectory(String directoryName);

    void createDirectory(String directoryName);

    void deleteDirectory(String directoryName);

    void renameDirectory(String directoryName, String newDirectoryName);

    List<String> getFiles(String directoryName, String filter);
    
    List<String> getFiles(String directoryName);

    List<String> getSubDirectories(String directoryName);
    
    InputStream getStream(String objectName, boolean isPrivate);
    
    boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg);
}
