package com.genexus.db.driver;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.*;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.genexus.StructSdtMessages_Message;
import com.genexus.util.GXService;
import com.genexus.util.StorageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.*;

public class ExternalProviderAzureStorageLatest extends ExternalProviderBase implements ExternalProvider {
	private static Logger logger = LogManager.getLogger(ExternalProviderAzureStorageLatest.class);

	static final String NAME = "AZUREBS"; //Azure Blob Storage

	static final String ACCOUNT = "ACCOUNT_NAME";
	static final String ACCESS_KEY = "ACCESS_KEY";
	static final String PUBLIC_CONTAINER = "PUBLIC_CONTAINER_NAME";
	static final String PRIVATE_CONTAINER = "PRIVATE_CONTAINER_NAME";

	private boolean useManagedIdentity;

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

	private BlobServiceClient blobServiceClient;
	private BlobContainerClient publicContainerClient;
	private BlobContainerClient privateContainerClient;

	private int defaultExpirationMinutes = DEFAULT_EXPIRATION_MINUTES;

	private String privateContainerName;
	private String publicContainerName;

	private void init() throws Exception {
		try {
			account = getEncryptedPropertyValue(ACCOUNT, ACCOUNT_DEPRECATED);
		} catch (Exception ex) {
			logger.error("Error initializing Azure Storage: unable to get account", ex);
			throw ex;
		}

		try {
			key = getEncryptedPropertyValue(ACCESS_KEY, KEY_DEPRECATED);
			if (key.isEmpty()) {
				logger.info("ACCESS_KEY empty — using Managed Identity");
				useManagedIdentity = true;
			}
		} catch (Exception ex) {
			if (key == null) {
				logger.info("ACCESS_KEY null — using Managed Identity");
				useManagedIdentity = true;
			}
		}

		try {
			String privateContainerNameValue = getEncryptedPropertyValue(PRIVATE_CONTAINER, PRIVATE_CONTAINER_DEPRECATED);
			String publicContainerNameValue = getEncryptedPropertyValue(PUBLIC_CONTAINER, PUBLIC_CONTAINER_DEPRECATED);

			privateContainerName = privateContainerNameValue.toLowerCase();
			publicContainerName = publicContainerNameValue.toLowerCase();

			if (useManagedIdentity) {
				initWithManagedIdentity();
			} else {
				initWithAccountKey();
			}
		}
		catch (Exception ex) {
			handleAndLogException("Initialization error", ex);
		}
	}
	
	private void initWithAccountKey() {
		// Create BlobServiceClient with account key
		StorageSharedKeyCredential credential = new StorageSharedKeyCredential(account, key);
		blobServiceClient = new BlobServiceClientBuilder()
			.endpoint(String.format("https://%s.blob.core.windows.net", account))
			.credential(credential)
			.buildClient();
			
		initContainerClients();
	}
	
	private void initWithManagedIdentity() {
		// Create a DefaultAzureCredential
		DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
		
		// Create BlobServiceClient using the credential
		blobServiceClient = new BlobServiceClientBuilder()
			.endpoint(String.format("https://%s.blob.core.windows.net", account))
			.credential(credential)
			.buildClient();
			
		initContainerClients();
	}
	
	private void initContainerClients() {
		// Create container clients and ensure containers exist
		publicContainerClient = blobServiceClient.getBlobContainerClient(publicContainerName);
		if (!publicContainerClient.exists()) {
			publicContainerClient = blobServiceClient.createBlobContainer(publicContainerName);
			publicContainerClient.setAccessPolicy(PublicAccessType.BLOB, null);
		}
		
		privateContainerClient = blobServiceClient.getBlobContainerClient(privateContainerName);
		if (!privateContainerClient.exists()) {
			privateContainerClient = blobServiceClient.createBlobContainer(privateContainerName);
		}
	}

	public ExternalProviderAzureStorageLatest() throws Exception {
		super();
		init();
	}

	public ExternalProviderAzureStorageLatest(GXService providerService) throws Exception {
		super(providerService);
		init();
	}

	public String getName() {
		return NAME;
	}

	public void download(String externalFileName, String localFile, ResourceAccessControlList acl) {
		try {
			BlobClient blobClient = getBlobClient(externalFileName, acl);
			blobClient.downloadToFile(localFile, true);
		} catch (Exception ex) {
			handleAndLogException("Invalid URI or error downloading file", ex);
		}
	}
	
	private BlobClient getBlobClient(String fileName, ResourceAccessControlList acl) {
		BlobClient blobClient;
		if (isPrivateAcl(acl)) {
			blobClient = privateContainerClient.getBlobClient(fileName);
		} else {
			blobClient = publicContainerClient.getBlobClient(fileName);
		}
		return blobClient;
	}

	private boolean isPrivateAcl(ResourceAccessControlList acl) {
		// If default ACL is private, use always private.
		return this.defaultAcl == ResourceAccessControlList.Private || acl == ResourceAccessControlList.Private;
	}

	public String upload(String localFile, String externalFileName, ResourceAccessControlList acl) {
		try {
			externalFileName = getExternalFileName(externalFileName);
			BlobClient blobClient = getBlobClient(externalFileName, acl);
			blobClient.uploadFromFile(localFile, true);
			return getResourceUrl(externalFileName, acl, defaultExpirationMinutes);
		} catch (Exception ex) {
			handleAndLogException("Error uploading file", ex);
			return "";
		}
	}

	public String upload(String externalFileName, InputStream input, ResourceAccessControlList acl) {
		//https://docs.azure.cn/en-us/storage/blobs/storage-blob-upload-java
		try (ExternalProviderHelper.InputStreamWithLength streamInfo =
					 ExternalProviderHelper.getInputStreamContentLength(input)) {
			externalFileName = getExternalFileName(externalFileName);
			BlockBlobClient blobClient =
					getBlobClient(externalFileName, acl).getBlockBlobClient();
			String contentType =
					(externalFileName.endsWith(".tmp") &&
							"application/octet-stream".equals(streamInfo.detectedContentType))
							? "image/jpeg"
							: streamInfo.detectedContentType;

			BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
			blobClient.uploadWithResponse(
					streamInfo.inputStream,
					streamInfo.contentLength,
					headers,
					null,
					null,
					null,
					null,
					null,
					Context.NONE
			);

			return getResourceUrl(externalFileName, acl, DEFAULT_EXPIRATION_MINUTES);
		}
		catch (Exception ex) {
			handleAndLogException("Error uploading file", ex);
			return "";
		}
	}

	public String get(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) {
		try {
			externalFileName = getExternalFileName(externalFileName);
			if (exists(externalFileName, acl)) {
				return getResourceUrl(externalFileName, acl, expirationMinutes);
			}
		} catch (Exception ex) {
			handleAndLogException("Error getting file", ex);
			return "";
		}
		return "";
	}

	private String getResourceUrl(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) {

		externalFileName = getExternalFileName(externalFileName);
		if (isPrivateAcl(acl)) {
			return getPrivate(externalFileName, expirationMinutes);
		} else {
			BlobClient blobClient = publicContainerClient.getBlobClient(externalFileName);
			//getBlobClient method returns URL encoded
			//https://azuresdkdocs.z19.web.core.windows.net/java/azure-storage-blob/12.30.0/com/azure/storage/blob/BlobContainerClient.html#getBlobClient(java.lang.String)
			////https://github.com/Azure/azure-sdk-for-java/issues/21610
			return blobClient.getBlobUrl();
		}
	}

	private String getPrivate(String externalFileName, int expirationMinutes) {
		try {
			externalFileName = getExternalFileName(externalFileName);
			BlobClient blobClient = privateContainerClient.getBlobClient(externalFileName);
			expirationMinutes = expirationMinutes > 0 ? expirationMinutes : defaultExpirationMinutes;
			OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(expirationMinutes);
			// Permissions (read)
			BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
			BlobServiceSasSignatureValues values =
				new BlobServiceSasSignatureValues(expiryTime, permission);
			String sasToken;
			if (!useManagedIdentity) {
				sasToken = blobClient.generateSas(values);
			} else {
				BlobServiceClient blobServiceClient = privateContainerClient.getServiceClient();
				OffsetDateTime start = OffsetDateTime.now().minusMinutes(5);
				//https://learn.microsoft.com/en-us/azure/storage/blobs/storage-blob-user-delegation-sas-create-java?tabs=container
				UserDelegationKey userDelegationKey =
					blobServiceClient.getUserDelegationKey(start, expiryTime);
				sasToken = blobClient.generateUserDelegationSas(values, userDelegationKey);
			}
			return blobClient.getBlobUrl() + "?" + sasToken;
		} catch (Exception ex) {
			handleAndLogException("Error getting private file", ex);
			return "";
		}
	}

	public void delete(String objectName, ResourceAccessControlList acl) {
		try {
			BlobClient blobClient = getBlobClient(objectName, acl);
			blobClient.deleteIfExists();
		} catch (Exception ex) {
			handleAndLogException("Error deleting file", ex);
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
			BlobClient sourceBlob = getBlobClient(objectName, acl);
			BlobClient targetBlob = getBlobClient(newName, acl);
			String sourceBlobUrl;
			if (useManagedIdentity) {
				//Needs RBAC permissions: https://learn.microsoft.com/en-us/azure/storage/blobs/authorize-access-azure-active-directory
				sourceBlobUrl = sourceBlob.getBlobUrl();
			} else {
				if (isPrivateAcl(acl)) {
					BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
					BlobServiceSasSignatureValues values =
						new BlobServiceSasSignatureValues(
							OffsetDateTime.now().plusMinutes(5),
							permission
						);

					sourceBlobUrl = sourceBlob.getBlobUrl() + "?" + sourceBlob.generateSas(values);
				} else {
					sourceBlobUrl = sourceBlob.getBlobUrl();
				}
			}
			targetBlob.beginCopy(sourceBlobUrl, null);
			return getResourceUrl(newName, acl, defaultExpirationMinutes);
		} catch (Exception ex) {
			handleAndLogException("Error copying file", ex);
			return "";
		}
	}

	public String copy(String objectUrl, String newName, String tableName, String fieldName, ResourceAccessControlList acl) {

		objectUrl = objectUrl.replace(getUrl(), "");
		newName = tableName + "/" + fieldName + "/" + newName;

		try {
			// Source is always in the private container
			BlobClient sourceBlob = privateContainerClient.getBlobClient(objectUrl);
			BlobClient targetBlob = getBlobClient(newName, acl);

			Map<String, String> metadata =
				createObjectMetadata(tableName, fieldName, StorageUtils.encodeName(newName));

			String sourceBlobUrl;
			if (useManagedIdentity) {
				sourceBlobUrl = sourceBlob.getBlobUrl();
			} else {

				BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
				BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
					OffsetDateTime.now().plusMinutes(5), permission);

				sourceBlobUrl = sourceBlob.getBlobUrl() + "?" + sourceBlob.generateSas(values);
			}
			targetBlob.beginCopy(sourceBlobUrl, null);
			targetBlob.setMetadata(metadata);
			return getResourceUrl(newName, acl, defaultExpirationMinutes);

		} catch (Exception ex) {
			handleAndLogException("Error copying file", ex);
			return "";
		}
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
			BlobClient blobClient = getBlobClient(objectName, acl);
			BlobProperties properties = blobClient.getProperties();
			return properties.getBlobSize();
		} catch (Exception ex) {
			handleAndLogException("Error getting file length", ex);
			return 0;
		}
	}

	public Date getLastModified(String objectName, ResourceAccessControlList acl) {
		try {
			BlobClient blobClient = getBlobClient(objectName, acl);
			BlobProperties properties = blobClient.getProperties();
			return Date.from(properties.getLastModified().toInstant());
		} catch (Exception ex) {
			handleAndLogException("Error getting last modified date", ex);
			return new Date();
		}
	}

	public boolean exists(String objectName, ResourceAccessControlList acl) {
		try {
			BlobClient blobClient = getBlobClient(objectName, acl);
			return blobClient.exists();
		} catch (Exception ex) {
			handleAndLogException("Error checking if file exists", ex);
			return false;
		}
	}

	public String getDirectory(String directoryName) {
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		if (existsDirectory(directoryName)) {
			return publicContainerClient.getBlobContainerName() + StorageUtils.DELIMITER + directoryName;
		} else {
			return "";
		}
	}

	public boolean existsDirectory(String directoryName) {
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			// List all blobs with the directory prefix
			ListBlobsOptions options = new ListBlobsOptions().setPrefix(directoryName);
			// Check if there are any blobs with this prefix
			boolean exists = false;
			for (BlobItem blobItem : publicContainerClient.listBlobs(options, null)) {
				String name = blobItem.getName();
				if (!name.equals(directoryName)) {
					// If we found any blob that isn't just the directory marker itself
					exists = true;
					break;
				}
			}
			return exists;
		} catch (Exception ex) {
			handleAndLogException("Error checking if directory exists", ex);
			return false;
		}
	}

	public void createDirectory(String directoryName) {
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			// Create a blob with empty content to mark the directory
			BlobClient blobClient = publicContainerClient.getBlobClient(directoryName);
			byte[] emptyContent = new byte[0];
			blobClient.upload(new ByteArrayInputStream(emptyContent), emptyContent.length, true);
		} catch (Exception ex) {
			handleAndLogException("Error creating directory", ex);
		}
	}

	public void deleteDirectory(String directoryName) {
		ResourceAccessControlList acl = null;
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			// List all blobs with the directory prefix
			ListBlobsOptions options = new ListBlobsOptions().setPrefix(directoryName);
			
			// Delete all blobs in the directory
			for (BlobItem blobItem : publicContainerClient.listBlobs(options, null)) {
				String name = blobItem.getName();
				if (name.startsWith(directoryName)) {
					if (name.endsWith(StorageUtils.DELIMITER)) {
						// This is a "subdirectory"
						if (!name.equals(directoryName)) {
							deleteDirectory(name);
						}
					} else {
						// This is a file
						delete(name, acl);
					}
				}
			}
		} catch (Exception ex) {
			handleAndLogException("Error deleting directory", ex);
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
			// List all blobs with the directory prefix
			ListBlobsOptions options = new ListBlobsOptions().setPrefix(directoryName);
			
			// Copy and rename all blobs in the directory
			for (BlobItem blobItem : publicContainerClient.listBlobs(options, null)) {
				String name = blobItem.getName();
				if (name.startsWith(directoryName)) {
					if (name.endsWith(StorageUtils.DELIMITER)) {
						// This is a "subdirectory"
						if (!name.equals(directoryName)) {
							String subdirName = name.substring(directoryName.length());
							renameDirectory(name, newDirectoryName + subdirName);
						}
					} else {
						// This is a file, rename it
						String newName = name.replace(directoryName, newDirectoryName);
						rename(name, newName, acl);
					}
				}
			}
			// Delete the original directory
			deleteDirectory(directoryName);
		} catch (Exception ex) {
			handleAndLogException("Error renaming directory", ex);
		}
	}

	public List<String> getFiles(String directoryName, String filter) {
		List<String> files = new ArrayList<String>();
		directoryName = StorageUtils.normalizeDirectoryName(directoryName);
		try {
			// List all blobs with the directory prefix
			ListBlobsOptions options = new ListBlobsOptions().setPrefix(directoryName);
			
			// Add all file names to the list
			for (BlobItem blobItem : publicContainerClient.listBlobs(options, null)) {
				String name = blobItem.getName();
				if (name.startsWith(directoryName) && !name.endsWith(StorageUtils.DELIMITER)) {
					// This is a file, add it to the list
					files.add(name);
				}
			}
		} catch (Exception ex) {
			handleAndLogException("Error getting files", ex);
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
			// List all blobs with the directory prefix
			ListBlobsOptions options = new ListBlobsOptions().setPrefix(directoryName);
			
			// Get all subdirectory names
			Set<String> dirSet = new HashSet<String>();
			for (BlobItem blobItem : publicContainerClient.listBlobs(options, null)) {
				String name = blobItem.getName();
				if (name.startsWith(directoryName) && !name.equals(directoryName)) {
					// Get the subdirectory name
					String remainingPath = name.substring(directoryName.length());
					int slashIndex = remainingPath.indexOf(StorageUtils.DELIMITER);
					
					if (slashIndex >= 0) {
						// This is a subdirectory or a file in a subdirectory
						String subdirName = directoryName + remainingPath.substring(0, slashIndex + 1);
						dirSet.add(subdirName);
					}
				}
			}
			directories.addAll(dirSet);
		} catch (Exception ex) {
			handleAndLogException("Error getting subdirectories", ex);
		}
		return directories;
	}

	public InputStream getStream(String objectName, ResourceAccessControlList acl) {
		try {
			BlobClient blobClient = getBlobClient(objectName, acl);
			return blobClient.openInputStream();
		} catch (Exception ex) {
			handleAndLogException("Error getting stream", ex);
			return null;
		}
	}

	public boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg) {
		try {
			// Extract error information from the SDK exceptions
			String errorMessage = ex.getMessage();
			if (errorMessage != null) {
				msg.setId("AzureError");
				msg.setDescription(errorMessage);
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
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

	private String getExternalFileName(String externalFileName)
	{
		//Defensive code, as externalFileName may have a leading / and this causes a double / at blob uri
		//The latest Azure SDK is strict at uri format and encodes special characters
		if (externalFileName == "")
			return externalFileName;
		return externalFileName.startsWith("/") ? externalFileName.substring(1) : externalFileName;
	}

	private void handleAndLogException(String message, Exception ex) {
		if (ex instanceof BlobStorageException) {
			logger.error("Azure Storage error: {} (Status: {}, Code: {})",
				((BlobStorageException) ex).getServiceMessage(), ((BlobStorageException) ex).getStatusCode(), ((BlobStorageException) ex).getErrorCode());
		} else if (ex instanceof ClientAuthenticationException) {
			logger.error("Authentication error: {}", ex.getMessage());
		} else if (ex instanceof HttpRequestException) {
			logger.error("Connection error: {}", ex.getMessage());
		} else if (ex instanceof URISyntaxException) {
			logger.error("Invalid URI: {}", ex.getMessage());
		} else if (ex instanceof IOException) {
			logger.error(message, ex);
		} else {
			logger.error("Unexpected storage error", ex);
		}
		throw new RuntimeException(ex.getMessage(), ex);
	}
}
