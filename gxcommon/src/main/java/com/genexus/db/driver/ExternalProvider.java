package com.genexus.db.driver;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import com.genexus.StructSdtMessages_Message;


public interface ExternalProvider {

	void download(String externalFileName, String localFile, ResourceAccessControlList acl);

	String upload(String localFile, String externalFileName, ResourceAccessControlList acl);

	String upload(String externalFileName, InputStream input, ResourceAccessControlList acl);

	String get(String externalFileName, ResourceAccessControlList acl, int expirationMinutes);

	void delete(String objectName, ResourceAccessControlList acl);

	String rename(String objectName, String newName, ResourceAccessControlList acl);

	String copy(String objectName, String newName, ResourceAccessControlList acl);

	String copy(String objectUrl, String newName, String tableName, String fieldName, ResourceAccessControlList acl);

	long getLength(String objectName, ResourceAccessControlList acl);

	Date getLastModified(String objectName, ResourceAccessControlList acl);

	boolean exists(String objectName, ResourceAccessControlList acl);

	String getDirectory(String directoryName);

	boolean existsDirectory(String directoryName);

	void createDirectory(String directoryName);

	void deleteDirectory(String directoryName);

	void renameDirectory(String directoryName, String newDirectoryName);

	List<String> getFiles(String directoryName, String filter);

	List<String> getFiles(String directoryName);

	List<String> getSubDirectories(String directoryName);

	InputStream getStream(String objectName, ResourceAccessControlList acl);

	boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg);

	String getObjectNameFromURL(String url);
}
