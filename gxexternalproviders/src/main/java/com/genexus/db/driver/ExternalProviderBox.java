package com.genexus.db.driver;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxAPIResponseException;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxResource;
import com.box.sdk.BoxSearch;
import com.box.sdk.BoxSearchParameters;
import com.box.sdk.BoxSharedLink;
import com.box.sdk.BoxSharedLink.Access;
import com.box.sdk.BoxSharedLink.Permissions;
import com.genexus.Application;
import com.genexus.StructSdtMessages_Message;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;
import com.genexus.util.StorageUtils;

import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalProviderBox implements ExternalProvider {
	private static final String CONFIG_FILE = "CONFIG_FILE";

	private BoxAPIConnection api;

	private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(ExternalProviderBox.class);

	public ExternalProviderBox() {
		GXServices services = Application.getGXServices();
		GXService providerService = services.get(GXServices.STORAGE_SERVICE);

		String configFile = providerService.getProperties().get(CONFIG_FILE);
		File file = new File(services.configBaseDirectory(), configFile);
		debugTrace("configFile = \"%s\"", file.getAbsolutePath());

		BoxConfig config;
		try {
			Reader reader = new FileReader(file);
			config = BoxConfig.readFrom(reader);
		} catch (IOException ex) {
			throw handleException("read config", ex);
		}

		api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config);
	}

	@Override
	public void download(String externalFileName, String localFile, boolean isPrivate) {
		debugTrace("download(\"%s\", \"%s\", %s)", externalFileName, localFile, isPrivate);

		try {
			BoxFile.Info file = getBoxFileFromPath(externalFileName, isPrivate);
			OutputStream out = new FileOutputStream(localFile);
			file.getResource().download(out);
			out.close();
		} catch (IOException ex) {
			throw handleException("download", ex);
		}
	}

	@Override
	public String upload(String localFile, String externalFileName, boolean isPrivate) {
		try {
			File file = new File(localFile);
			FileInputStream fileStream = new FileInputStream(file);
			BoxFolder folder = getOrCreateBoxFolderFromPath(externalFileName, true);
			String fileName = getFileNameFromPath(externalFileName);

			BoxFile.Info fileInfo = folder.uploadFile(fileStream, fileName, file.length(), null);
			fileStream.close();
			String link = getBoxFileLink(fileInfo.getResource(), isPrivate);
			debugTrace("upload(\"%s\", \"%s\", %s) = %s", localFile, externalFileName, isPrivate, link);
			return link;
		} catch (IOException ex) {
			throw handleException("upload", ex);
		}
	}

	@Override
	public String upload(String externalFileName, InputStream input, boolean isPrivate) {
		try {
			BoxFolder folder = getOrCreateBoxFolderFromPath(externalFileName, true);
			String fileName = getFileNameFromPath(externalFileName);

			BoxFile.Info fileInfo = folder.uploadFile(input, fileName);
			String link = getBoxFileLink(fileInfo.getResource(), isPrivate);
			debugTrace("upload_Input(\"%s\", %s) = %s", externalFileName, isPrivate, link);
			return link;
		} catch (Exception ex) {
			throw handleException("upload_input", ex);
		}
	}

	private String getBoxFileLink(BoxFile file, boolean isPrivate) {
		return getBoxFileLink(file, isPrivate, null);
	}

	private String getBoxFileLink(BoxFile file, boolean isPrivate, Date expiration) {
		boolean visible = !isPrivate;
		Permissions permissions = new Permissions();
		permissions.setCanPreview(visible);
		permissions.setCanDownload(visible);
		BoxSharedLink link = file.createSharedLink(visible ? Access.OPEN : Access.COMPANY, expiration, permissions);
		return visible ? link.getDownloadURL() : null;
	}

	private String getFileNameFromPath(String path) {
		String[] names = path.split(StorageUtils.DELIMITER);
		return names != null ? names[names.length - 1] : null;
	}

	private BoxFolder getOrCreateBoxFolderFromPath(String path, boolean isFilePath) {
		return getBoxFolderFromPath(path, isFilePath, true);
	}

	private BoxFolder getBoxFolderFromPath(String path, boolean isFilePath, boolean createIfMissing) {
		BoxFolder root = BoxFolder.getRootFolder(api);
		BoxFolder.Info folder = null;
		String folderID = root.getID();

		String lastToken = null;

		for (String token : path.split(StorageUtils.DELIMITER)) {
			String dirName = lastToken;
			if (dirName != null) {
				folder = getBoxFolder(folderID, dirName, createIfMissing);
				if (folder == null)
					return null;
				else
					folderID = folder.getID();
			}
			lastToken = token;
		}
		if (!isFilePath && lastToken != null) {
			folder = getBoxFolder(folderID, lastToken, createIfMissing);
			if (folder == null)
				return null;
			else
				folderID = folder.getID();
		}
		return folder.getResource();
	}

	private BoxFolder getBoxFolderFromPath(String path, boolean isFilePath) {
		return getBoxFolderFromPath(path, isFilePath, false);
	}

	private BoxFolder.Info getOrCreateBoxFolder(String parentID, String folderName) {
		return getBoxFolder(parentID, folderName, true);
	}

	private BoxFolder.Info getBoxFolder(String parentID, String folderName, boolean createIfMissing) {
		BoxFolder.Info childFolder = getBoxFolder(parentID, folderName);
		if (childFolder == null && createIfMissing) {
			childFolder = new BoxFolder(api, parentID).createFolder(folderName);
		}
		return childFolder;
	}

	private BoxFolder.Info getBoxFolder(String parentID, String folderName) {
		BoxSearch find = new BoxSearch(api);
		BoxSearchParameters query = new BoxSearchParameters();
		query.setQuery(folderName);
		query.setType(BoxResource.getResourceType(BoxFolder.class));
		query.setAncestorFolderIds(Arrays.asList(parentID));

		for (BoxItem.Info info : find.searchRange(0, 200, query)) {
			String foundItemParentID = info.getParent().getID();
			String foundItemName = info.getName();

			if (parentID.equals(foundItemParentID) && folderName.equals(foundItemName)) {
				if (BoxFolder.Info.class.isInstance(info)) {
					return BoxFolder.Info.class.cast(info);
				}
			}
		}

		return null;
	}

	private BoxFile.Info getBoxFile(String parentID, String fileName, String... fields) {
		BoxFolder folder = new BoxFolder(api, parentID);
		String fileID = null;
		try {
			folder.canUpload(fileName, 1024);
		} catch (BoxAPIResponseException bex) {
			if (bex.getResponseCode() == 409) {
				fileID = getIdFromConflict(bex.getResponse());
			}
		}
		if (fileID != null) {
			BoxFile file = new BoxFile(api, fileID);
			if (fields != null && fields.length > 0)
				return file.getInfo(fields);
			else
				return file.getInfo();
		} else {
			return null;
		}
	}

	private String getIdFromConflict(String message) {
		String id = "";
		Pattern p = Pattern.compile("\"id\":\"[0-9]+\"");
		Matcher m = p.matcher(message);
		if (m.find()) {
			String sub = m.group();
			id = sub.substring("\"id\":".length() + 1, sub.length() - 1);
		}
		return id;
	}

	private BoxFile.Info getBoxFileFromPath(String filePath, boolean isPrivate, String... fields) {
		BoxFolder folder = getBoxFolderFromPath(filePath, true);
		String fileName = getFileNameFromPath(filePath);
		return folder != null && fileName != null ? getBoxFile(folder.getID(), fileName, fields) : null;
	}

	@Override
	public String get(String externalFileName, boolean isPrivate, int expirationMinutes) {
		Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60));
		BoxFile.Info fileInfo = getBoxFileFromPath(externalFileName, isPrivate);
		String link = getBoxFileLink(fileInfo.getResource(), isPrivate, expiration);
		debugTrace("get(\"%s\", %s, %d) = %s", externalFileName, isPrivate, expirationMinutes, link);
		return link;
	}

	@Override
	public void delete(String objectName, boolean isPrivate) {
		try {
			debugTrace("delete(\"%s\", %s)", objectName, isPrivate);
			BoxFile.Info file = getBoxFileFromPath(objectName, isPrivate);

			file.getResource().delete();
		} catch (Exception ex) {
			throw handleException("delete", ex);
		}
	}

	@Override
	public String rename(String objectName, String newName, boolean isPrivate) {
		try {
			debugTrace("rename(\"%s\", \"%s\", %s)", objectName, newName, isPrivate);
			BoxFile.Info fileInfo = getBoxFileFromPath(objectName, isPrivate);
			if (fileInfo == null)
				return null;

			BoxFile file = fileInfo.getResource();
			file.rename(newName);

			String newUrl = getBoxFileLink(file, isPrivate);
			debugTrace("rename(\"%s\", \"%s\", %s) = \"%s\"", objectName, newName, isPrivate, newUrl);
			return newUrl;
		} catch (Exception ex) {
			throw handleException("rename", ex);
		}
	}

	@Override
	public String copy(String objectName, String newName, boolean isPrivate) {
		try {
			BoxFile.Info file = getBoxFileFromPath(objectName, isPrivate);

			String newUrl = internalCopy(file.getID(), file.getParent().getID(), newName, isPrivate);
			debugTrace("copy(\"%s\", \"%s\", %s) = \"%s\"", objectName, newName, isPrivate, newUrl);
			return newUrl;
		} catch (Exception ex) {
			throw handleException("copy", ex);
		}
	}

	@Override
	public String copy(String objectUrl, String newName, String tableName, String fieldName, boolean isPrivate) {
		try {
			BoxFolder root = BoxFolder.getRootFolder(api);
			BoxFolder.Info table = getOrCreateBoxFolder(root.getID(), tableName);
			BoxFolder.Info field = getOrCreateBoxFolder(table.getID(), fieldName);
			BoxFile.Info file = getBoxFileFromPath(objectUrl, isPrivate);

			String newUrl = internalCopy(file.getID(), field.getID(), newName, isPrivate);
			debugTrace("copy(\"%s\", \"%s\", \"%s\", \"%s\", %s) = \"%s\"", objectUrl, newName, tableName, fieldName,
					isPrivate, newUrl);
			return newUrl;
		} catch (Exception ex) {
			throw handleException("copy", ex);
		}
	}

	private String internalCopy(String fileID, String destinationFolderID, String newName, boolean isPrivate) {
		BoxFile file = new BoxFile(api, fileID);
		BoxFolder destination = new BoxFolder(api, destinationFolderID);
		BoxFile.Info copiedFile = file.copy(destination, newName);
		debugTrace("copy(source: %s) = %s", fileID, copiedFile.getID());
		return getBoxFileLink(copiedFile.getResource(), isPrivate);
	}

	@Override
	public long getLength(String objectName, boolean isPrivate) {
		try {
			BoxFile.Info fileInfo = getBoxFileFromPath(objectName, isPrivate, "size");
			long size = fileInfo.getSize();
			debugTrace("getLength(name: \"%s\", isPrivate: %s) = %s", objectName, isPrivate, size);
			return size;
		} catch (Exception ex) {
			throw handleException("getLength", ex);
		}
	}

	@Override
	public Date getLastModified(String objectName, boolean isPrivate) {
		try {
			BoxFile.Info fileInfo = getBoxFileFromPath(objectName, isPrivate, "modified_at");
			Date modifiedAt = fileInfo.getModifiedAt();
			debugTrace("getLastModified(name: \"%s\", isPrivate: %s) = %s", objectName, isPrivate, modifiedAt);
			return modifiedAt;
		} catch (Exception ex) {
			throw handleException("getLastModified", ex);
		}
	}

	@Override
	public boolean exists(String objectName, boolean isPrivate) {
		try {
			boolean exists = getBoxFileFromPath(objectName, isPrivate) != null;
			debugTrace("exists(\"%s\", %s) = %s", objectName, isPrivate, exists);
			return exists;
		} catch (Exception ex) {
			throw handleException("exists", ex);
		}
	}

	@Override
	public String getDirectory(String directoryName) {
		// What should return?
		return existsDirectory(directoryName) ? directoryName : null;
	}

	@Override
	public boolean existsDirectory(String directoryName) {
		boolean exists = getBoxFolderFromPath(directoryName, false) != null;
		debugTrace("existsDirectory(\"%s\") = %s", directoryName, exists);
		return exists;
	}

	@Override
	public void createDirectory(String directoryName) {
		debugTrace("createDirectory(\"%s\")", directoryName);
		getOrCreateBoxFolderFromPath(directoryName, false);
	}

	@Override
	public void deleteDirectory(String directoryName) {
		debugTrace("deleteDirectory(\"%s\")", directoryName);
		BoxFolder folder = getBoxFolderFromPath(directoryName, false);
		if (folder != null)
			folder.delete(true);
	}

	@Override
	public void renameDirectory(String directoryName, String newDirectoryName) {
		debugTrace("renameDirectory(\"%s\", \"%s\")", directoryName, newDirectoryName);
		BoxFolder folder = getBoxFolderFromPath(directoryName, false);
		if (folder != null)
			folder.rename(newDirectoryName);
	}

	@Override
	public List<String> getFiles(String directoryName, String filter) {
		debugTrace("getFiles(\"%s\", \"%s\")", directoryName, filter);
		List<String> files = new ArrayList<>();
		try {
			BoxFolder folder = getBoxFolderFromPath(directoryName, false);
			for (BoxItem.Info info : folder.getChildren("name")) {
				if (info instanceof BoxFile.Info)
					files.add(info.getName());
			}
			return files;
		} catch (Exception ex) {
			throw handleException("getFiles", ex);
		}
	}

	@Override
	public List<String> getFiles(String directoryName) {
		return getFiles(directoryName, "");
	}

	@Override
	public List<String> getSubDirectories(String directoryName) {
		debugTrace("getSubDirectories(\"%s\")", directoryName);
		List<String> directories = new ArrayList<>();
		try {
			BoxFolder folder = getBoxFolderFromPath(directoryName, false);
			for (BoxItem.Info info : folder.getChildren("name")) {
				if (info instanceof BoxFolder.Info)
					directories.add(info.getName());
			}
			return directories;
		} catch (Exception ex) {
			throw handleException("getDirectories", ex);
		}
	}

	@Override
	public InputStream getStream(String objectName, boolean isPrivate) {
		debugTrace("getStream(\"%s\", %s)", objectName, isPrivate);

		try {
			BoxFile.Info fileInfo = getBoxFileFromPath(objectName, isPrivate);
			if (fileInfo == null)
				return null;
	
			/// Copied from BoxFile.download()
			URL url = BoxFile.CONTENT_URL_TEMPLATE.build(api.getBaseURL(), fileInfo.getID());
			BoxAPIRequest request = new BoxAPIRequest(api, url, "GET");
			BoxAPIResponse response = request.send();
			return response.getBody();
		} catch (Exception ex) {
			throw handleException("getStream", ex);
		}
	}

	@Override
	public boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg) {
		return false;
	}

	private RuntimeException handleException(String action, Exception ex) {
		logger.error(String.format("Failed to %s", action), ex);
		return new RuntimeException(ex);
	}

	private void debugTrace(String message, Object... args) {
		// logger.debug(String.format(message, args));
	}
}
