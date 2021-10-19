package com.genexus.db.driver;

import com.genexus.Application;
import com.genexus.util.GXService;
import com.genexus.util.StorageUtils;
import com.genexus.StructSdtMessages_Message;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExternalProviderAzureStorage extends ExternalProviderBase implements ExternalProvider {
	private static Logger logger = LogManager.getLogger(ExternalProviderAzureStorage.class);

	static final String NAME = "AZUREBS"; //Azure Blob Storage

	static final String ACCOUNT = "ACCOUNT_NAME";
	static final String ACCESS_KEY = "ACCESS_KEY";
	static final String PUBLIC_CONTAINER = "PUBLIC_CONTAINER_NAME";
	static final String PRIVATE_CONTAINER = "PRIVATE_CONTAINER_NAME";

	@Deprecated
	static final String ACCOUNT_DEPRECATED = "ACCOUNT_NAME";
	@Deprecated
	static final String KEY_DEPRECATED = "ACCESS_KEY";
	@Deprecated
	static final String PUBLIC_CONTAINER_DEPRECATED = "PUBLIC_CONTAINER_NAME";
	@Deprecated
	static final String PRIVATE_CONTAINER_DEPRECATED = "PRIVATE_CONTAINER_NAME";

	private String account;
	private String key;
	private CloudBlobContainer publicContainer;
	private CloudBlobContainer privateContainer;
	private CloudBlobClient client;

	private int defaultExpirationMinutes = DEFAULT_EXPIRATION_MINUTES;

	private String privateContainerName;
	private String publicContainerName;

	public ExternalProviderAzureStorage(String service) throws Exception {
		this(Application.getGXServices().get(service));
	}

	public ExternalProviderAzureStorage() throws Exception {
		super(null, NAME);
		init();
	}

	public ExternalProviderAzureStorage(GXService providerService) throws Exception {
		super(providerService, NAME);
		init();
	}

	private void init() throws Exception {
		try {
			account = getEncryptedPropertyValue(ACCOUNT, ACCOUNT_DEPRECATED);
			key = getEncryptedPropertyValue(ACCESS_KEY, KEY_DEPRECATED);

			CloudStorageAccount storageAccount = CloudStorageAccount.parse(
				String.format("DefaultEndpointsProtocol=%1s;AccountName=%2s;AccountKey=%3s", "https", account, key));
			client = storageAccount.createCloudBlobClient();

			String privateContainerNameValue = getEncryptedPropertyValue(PRIVATE_CONTAINER, PRIVATE_CONTAINER_DEPRECATED);
			String publicContainerNameValue = getEncryptedPropertyValue(PUBLIC_CONTAINER, PUBLIC_CONTAINER_DEPRECATED);

			privateContainerName = privateContainerNameValue.toLowerCase();
			publicContainerName = publicContainerNameValue.toLowerCase();

			publicContainer = client.getContainerReference(publicContainerName);
			publicContainer.createIfNotExists();

			privateContainer = client.getContainerReference(privateContainerName);
			privateContainer.createIfNotExists();

			BlobContainerPermissions permissions = new BlobContainerPermissions();
			permissions.setPublicAccess(BlobContainerPublicAccessType.BLOB);
			publicContainer.uploadPermissions(permissions);
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI", ex);
		} catch (StorageException sex) {
			logger.error(sex.getMessage());
		} catch (InvalidKeyException ikex) {
			logger.error("Invalid keys", ikex);
		}
	}



	public String getName() {
		return NAME;
	}

	public void download(String externalFileName, String localFile, ResourceAccessControlList acl) {
		try {
			CloudBlockBlob blob = getCloudBlockBlob(externalFileName, acl);
			blob.downloadToFile(localFile);
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (java.io.IOException ioex) {
			logger.error("Error downloading file", ioex);
		}
	}


	private CloudBlockBlob getCloudBlockBlob(String fileName, ResourceAccessControlList acl) throws URISyntaxException, StorageException {
		CloudBlockBlob blob;
		if (isPrivateAcl(acl)) {
			blob = privateContainer.getBlockBlobReference(fileName);
		} else {
			blob = publicContainer.getBlockBlobReference(fileName);
		}
		return blob;
	}

	private boolean isPrivateAcl(ResourceAccessControlList acl) {
		// If default ACL is private, use always private.
		return this.defaultAcl == ResourceAccessControlList.Private || acl == ResourceAccessControlList.Private;
	}

	public String upload(String localFile, String externalFileName, ResourceAccessControlList acl) {
		try {
			CloudBlockBlob blob = getCloudBlockBlob(externalFileName, acl);
			blob.uploadFromFile(localFile);
			return getResourceUrl(externalFileName, acl, defaultExpirationMinutes);
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (IOException ex) {
			logger.error("Error uploading file", ex);
		}
		return "";
	}

	public String upload(String externalFileName, InputStream input, ResourceAccessControlList acl) {

		try {
			CloudBlockBlob blob = getCloudBlockBlob(externalFileName, acl);
			if (externalFileName.endsWith(".tmp")) {
				blob.getProperties().setContentType("image/jpeg");
			}
			try (BlobOutputStream blobOutputStream = blob.openOutputStream()) {
				int next = input.read();
				while (next != -1) {
					blobOutputStream.write(next);
					next = input.read();
				}
			}
			return getResourceUrl(externalFileName, acl, DEFAULT_EXPIRATION_MINUTES);

		} catch (URISyntaxException ex) {
			logger.error("Invalid URI", ex);
			return "";
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (java.io.IOException ex) {
			logger.error("Error uploading file", ex);
			return "";
		}
	}

	public String get(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) {
		try {
			if (exists(externalFileName, acl)) {
				return getResourceUrl(externalFileName, acl, expirationMinutes);
			}
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error("Error getting file", ex);
			return "";
		}
		return "";
	}

	private String getResourceUrl(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) throws URISyntaxException, StorageException {
		if (isPrivateAcl(acl)) {
			return getPrivate(externalFileName, expirationMinutes);
		} else {
			CloudBlockBlob blob = publicContainer.getBlockBlobReference(externalFileName);
			return blob.getUri().toString();
		}
	}

	private String getPrivate(String externalFileName, int expirationMinutes) {
		try {
			CloudBlockBlob blob = privateContainer.getBlockBlobReference(externalFileName);
			SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
			policy.setPermissionsFromString("r");
			Calendar date = Calendar.getInstance();
			expirationMinutes = expirationMinutes > 0 ? expirationMinutes : defaultExpirationMinutes;
			Date expire = new Date(date.getTimeInMillis() + (expirationMinutes * 60000));
			policy.setSharedAccessExpiryTime(expire);
			return blob.getUri().toString() + "?" + blob.generateSharedAccessSignature(policy, null);
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error("Error getting private file", ex);
		}
		return "";

	}

	public void delete(String objectName, ResourceAccessControlList acl) {
		try {
			CloudBlockBlob blob = getCloudBlockBlob(objectName, acl);
			blob.deleteIfExists();
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public String rename(String objectName, String newName, ResourceAccessControlList acl) {
		String ret = copy(objectName, newName, acl);
		delete(objectName, acl);
		return ret;
	}

	private String resolveObjectName(String urlOrObjectName, ResourceAccessControlList acl) {
		String objectName = getObjectNameFromURL(urlOrObjectName);
		return (objectName == null || objectName.length() == 0)? urlOrObjectName: objectName;
	}

	public String copy(String objectName, String newName, ResourceAccessControlList acl) {
		objectName = resolveObjectName(objectName, acl);
		try {
			CloudBlockBlob sourceBlob = getCloudBlockBlob(objectName, acl);
			CloudBlockBlob targetBlob = getCloudBlockBlob(newName, acl);
			targetBlob.startCopy(sourceBlob);
			return getResourceUrl(newName, acl, defaultExpirationMinutes);
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return "";
	}

	public String copy(String objectUrl, String newName, String tableName, String fieldName, ResourceAccessControlList acl) {
		objectUrl = objectUrl.replace(getUrl(), "");
		newName = tableName + "/" + fieldName + "/" + newName;
		try {
			CloudBlockBlob sourceBlob = privateContainer.getBlockBlobReference(objectUrl);  //Source will be always on the private container
			CloudBlockBlob targetBlob = getCloudBlockBlob(newName, acl);
			targetBlob.setMetadata(createObjectMetadata(tableName, fieldName, StorageUtils.encodeName(newName)));
			targetBlob.startCopy(sourceBlob);
			return getResourceUrl(newName, acl, defaultExpirationMinutes);
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return "";
	}

	private HashMap<String, String> createObjectMetadata(String table, String field, String name) {
		HashMap<String, String> metadata = new HashMap<String, String>();
		metadata.put("Table", table);
		metadata.put("Field", field);
		metadata.put("KeyValue", name);
		return metadata;
	}

	public long getLength(String objectName, ResourceAccessControlList acl) {
		try {
			CloudBlockBlob blob = getCloudBlockBlob(objectName, acl);
			blob.downloadAttributes();
			return blob.getProperties().getLength();
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
			return 0;
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public Date getLastModified(String objectName, ResourceAccessControlList acl) {
		try {
			CloudBlockBlob blob = getCloudBlockBlob(objectName, acl);
			blob.downloadAttributes();
			return blob.getProperties().getLastModified();
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
			return new Date();
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public boolean exists(String objectName, ResourceAccessControlList acl) {
		try {
			return getCloudBlockBlob(objectName, acl).exists();
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
			return false;
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public String getDirectory(String directoryName) {
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		if (existsDirectory(directoryName)) {
			return publicContainer.getName() + StorageUtils.DELIMITER + directoryName;
		} else {
			return "";
		}
	}

	public boolean existsDirectory(String directoryName) {
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			CloudBlobDirectory directory = publicContainer.getDirectoryReference(directoryName);
			for (ListBlobItem item : directory.listBlobs()) {
				String itemName = "";
				if (isFile(item)) {
					return true;
				}
				if (item instanceof CloudBlobDirectory) {
					itemName = ((CloudBlobDirectory) item).getPrefix();
					itemName = itemName.substring(0, itemName.length() - 1);
					if (!itemName.equals(directoryName)) {
						return true;
					}
				}
			}
			return false;
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
			return false;
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public void createDirectory(String directoryName) {
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			CloudBlockBlob blob = publicContainer.getBlockBlobReference(directoryName);
			blob.uploadFromByteArray(new byte[0], 0, 0);
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (java.io.IOException ioex) {
			logger.error("Error uploading file", ioex);
		}
	}

	public void deleteDirectory(String directoryName) {
		ResourceAccessControlList acl = null;
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			CloudBlobDirectory directory = publicContainer.getDirectoryReference(directoryName);
			for (ListBlobItem item : directory.listBlobs()) {
				String itemName = "";
				if (isFile(item)) {
					if (item instanceof CloudPageBlob) {
						itemName = ((CloudPageBlob) item).getName();
					} else if (item instanceof CloudBlockBlob) {
						itemName = ((CloudBlockBlob) item).getName();
					}
					delete(itemName, acl);
				}
				if (isDirectory(item)) {
					if (item instanceof CloudBlobDirectory) {
						itemName = ((CloudBlobDirectory) item).getPrefix();
					} else if (item instanceof CloudBlockBlob) {
						itemName = ((CloudBlockBlob) item).getName();
					}
					if (!itemName.equals(directoryName)) {
						deleteDirectory(itemName);
					}
				}
			}
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public void renameDirectory(String directoryName, String newDirectoryName) {
		ResourceAccessControlList acl = null;
		if (!existsDirectory(newDirectoryName)) {
			createDirectory(newDirectoryName);
		}
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		newDirectoryName = StorageUtils.normalizeDirectoryName(newDirectoryName);
		try {
			CloudBlobDirectory directory = publicContainer.getDirectoryReference(directoryName);
			for (ListBlobItem item : directory.listBlobs()) {
				String itemName = "";
				if (isFile(item)) {
					if (item instanceof CloudPageBlob) {
						itemName = ((CloudPageBlob) item).getName();
					} else if (item instanceof CloudBlockBlob) {
						itemName = ((CloudBlockBlob) item).getName();
					}
					rename(itemName, itemName.replace(directoryName, newDirectoryName), acl);
				}
				if (isDirectory(item)) {
					if (item instanceof CloudBlobDirectory) {
						itemName = ((CloudBlobDirectory) item).getPrefix();
					} else if (item instanceof CloudBlockBlob) {
						itemName = ((CloudBlockBlob) item).getName();
					}
					renameDirectory(directoryName + StorageUtils.DELIMITER + itemName, newDirectoryName + StorageUtils.DELIMITER + itemName);
				}
			}
			deleteDirectory(directoryName);
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public List<String> getFiles(String directoryName, String filter) {
		List<String> files = new ArrayList<String>();
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			CloudBlobDirectory directory = publicContainer.getDirectoryReference(directoryName);
			for (ListBlobItem item : directory.listBlobs()) {
				if (isFile(item)) {
					if (item instanceof CloudPageBlob) {
						files.add(((CloudPageBlob) item).getName());
					} else if (item instanceof CloudBlockBlob) {
						files.add(((CloudBlockBlob) item).getName());
					}
				}
			}
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return files;
	}

	public List<String> getFiles(String directoryName) {
		return getFiles(directoryName, "");
	}

	public List<String> getSubDirectories(String directoryName) {
		List<String> directories = new ArrayList<String>();
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			CloudBlobDirectory directory = publicContainer.getDirectoryReference(directoryName);
			for (ListBlobItem item : directory.listBlobs()) {
				if (isDirectory(item)) {
					if (item instanceof CloudBlobDirectory) {
						directories.add(((CloudBlobDirectory) item).getPrefix());
					} else if (item instanceof CloudBlockBlob) {
						directories.add(((CloudBlockBlob) item).getName());
					}
				}
			}
			directories.remove(directoryName);
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return directories;
	}

	public InputStream getStream(String objectName, ResourceAccessControlList acl) {
		try {
			CloudBlockBlob blob = getCloudBlockBlob(objectName, acl);
			blob.downloadAttributes();
			byte[] bytes = new byte[(int) blob.getProperties().getLength()];
			blob.downloadToByteArray(bytes, 0);

			InputStream stream = new ByteArrayInputStream(bytes);
			return stream;
		} catch (URISyntaxException ex) {
			logger.error("Invalid URI ", ex.getMessage());
		} catch (StorageException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return null;
	}

	public boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg) {
		try {
			StorageException aex = (StorageException) ex.getCause();
			msg.setId(aex.getErrorCode());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isDirectory(ListBlobItem item) {
		return (item instanceof CloudBlobDirectory) || (item instanceof CloudBlockBlob && ((CloudBlockBlob) item).getName().endsWith(StorageUtils.DELIMITER));
	}

	private boolean isFile(ListBlobItem item) {
		return (item instanceof CloudPageBlob) || (item instanceof CloudBlockBlob && !((CloudBlockBlob) item).getName().endsWith(StorageUtils.DELIMITER));
	}

	private String getUrl() {
		return "https://" + account + ".blob.core.windows.net/";
	}

	public String getObjectNameFromURL(String url) {
		String objectName = null;
		String baseUrl = this.getUrl();
		String publicContainerUrl = String.format("%s%s/", baseUrl , publicContainerName);
		String privateContainerUrl = String.format("%s%s/", baseUrl , privateContainerName);
		if (url.startsWith(publicContainerUrl)) {
			objectName = url.replace(publicContainerUrl, "");
		}
		if (url.startsWith(privateContainerUrl)) {
			objectName = url.replace(privateContainerUrl, "");
		}
		return objectName;
	}
}
