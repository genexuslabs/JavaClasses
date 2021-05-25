package com.genexus.db.driver;

import com.genexus.util.GXService;
import com.genexus.util.StorageUtils;
import com.genexus.StructSdtMessages_Message;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.cloud.storage.*;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExternalProviderGoogle extends ExternalProviderBase implements ExternalProvider  {

    private static Logger logger = LogManager.getLogger(ExternalProviderGoogle.class);

	static final String NAME = "GOOGLECS";  //Google Cloud Storage
    static final String KEY = "KEY";
    static final String APPLICATION_NAME = "APPLICATION_NAME";
    static final String BUCKET = "BUCKET_NAME";
    static final String FOLDER = "FOLDER_NAME";
    static final String PROJECT_ID = "PROJECT_ID";

    private static final int BUCKET_EXISTS = 409;
    private static final int OBJECT_NOT_FOUND = 404;

    private com.google.api.services.storage.Storage legacyClient;
    private com.google.cloud.storage.Storage betaClient; //Used only to get signed urls since it's on beta
    private String bucket;
    private String folder;
    private String projectId;
    private String url;
	private ResourceAccessControlList defaultACL = ResourceAccessControlList.PublicRead;

	public ExternalProviderGoogle() throws Exception{
		super();
		initialize();
	}

	public ExternalProviderGoogle(GXService providerService) throws Exception{
		super(providerService);
		initialize();
	}


    private void initialize() throws Exception {
		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			byte[] keyArray = getEncryptedPropertyValue(KEY,KEY).getBytes("UTF-8");
			GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(keyArray))
				.createScoped(Collections.singleton(StorageScopes.CLOUD_PLATFORM));

			legacyClient = new com.google.api.services.storage.Storage.Builder(httpTransport, jsonFactory, credential).setApplicationName(getPropertyValue(APPLICATION_NAME, APPLICATION_NAME)).build();

			projectId = getPropertyValue(PROJECT_ID, PROJECT_ID);
			betaClient = StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(new ByteArrayInputStream(keyArray)))
				.setProjectId(projectId)
				.build()
				.getService();
		} catch (GeneralSecurityException ex) {
			logger.error("Error authenticating", ex.getMessage());
		} catch (IOException ex) {
			handleIOException(ex);
		}

		bucket = getEncryptedPropertyValue(BUCKET, BUCKET);
		folder = getPropertyValue(FOLDER, FOLDER, "");

		url = String.format("https://%s.storage.googleapis.com/", bucket);
		createBucket();
		createFolder(folder);
	}

	public String getName(){
		return NAME;
	}

	private void createBucket() {
		try {
			Bucket bucket = new Bucket();
			bucket.setName(this.bucket);

			legacyClient.buckets().insert(projectId, bucket).execute();
		}
		catch (GoogleJsonResponseException ex) {
			if (ex.getStatusCode() != BUCKET_EXISTS) {
				logger.error("Error creating bucket", ex.getMessage());
			}
		} catch (IOException ex) {
			logger.error("Error creating bucket", ex.getMessage());
		}

	}

    private void createFolder(String folderName) {
        try {
            folderName = StorageUtils.normalizeDirectoryName(folderName);

            StorageObject object = new StorageObject().setName(folderName).setAcl(getACLOptions(null));
            InputStreamContent emptyContent = new InputStreamContent("application/directory", new ByteArrayInputStream(new byte[0]));
            emptyContent.setLength(0);

			com.google.api.services.storage.Storage.Objects.Insert insertRequest = legacyClient.objects().insert(bucket, object, emptyContent);

            insertRequest.execute();
        } catch (IOException ex) {
            handleIOException(ex);
        }
    }

    private Map<String, String> createObjectMetadata(String tableName, String fieldName, String resourceKey) {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Table", tableName);
        metadata.put("Field", fieldName);
        metadata.put("KeyValue", resourceKey);
        return metadata;
    }

    public void download(String externalFileName, String localFile, ResourceAccessControlList acl) {
        try {
            OutputStream out = new FileOutputStream(localFile);
			com.google.api.services.storage.Storage.Objects.Get request = legacyClient.objects().get(bucket, externalFileName);
            request.getMediaHttpDownloader().setDirectDownloadEnabled(true).download(new GenericUrl(url + StorageUtils.encodeName(externalFileName)), out);
            out.close();
        } catch (IOException e) {
            handleIOException(e);
            System.out.println(e.getMessage());
        }
    }

    public String upload(String localFile, String externalFileName, ResourceAccessControlList acl) {
        try {
            File file = new File(localFile);
            InputStreamContent contentStream = new InputStreamContent("application/octet-stream", new FileInputStream(file));
            contentStream.setLength(file.length());
            StorageObject objectMetadata = new StorageObject().setName(externalFileName);

            objectMetadata.setAcl(getACLOptions(acl));

			com.google.api.services.storage.Storage.Objects.Insert insertRequest = legacyClient.objects().insert(bucket, objectMetadata, contentStream);
            insertRequest.execute();
            return url + StorageUtils.encodeName(externalFileName);
        } catch (IOException ex) {
            handleIOException(ex);
            return "";
        }
    }

	private Acl getAcl(ResourceAccessControlList acl) {
		boolean isPrivate = isPrivateResource(acl);
		if (isPrivate) {
			return null; //Default ACL is private
		}
		else {
			return betaClient.createAcl(this.bucket, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
		}
	}

    private List<ObjectAccessControl> getACLOptions(ResourceAccessControlList acl) {
    	boolean isPrivate = isPrivateResource(acl);
        if (isPrivate)
            return Arrays.asList(new ObjectAccessControl());
        else
			return Arrays.asList(new ObjectAccessControl().setEntity("allUsers").setRole("READER"));
    }

    public String upload(String externalFileName, InputStream input, ResourceAccessControlList acl) {
        try {
            String contentType = "application/octet-stream";
            if (externalFileName.endsWith(".tmp")) {
                contentType = "image/jpeg";
            }
            InputStreamContent contentStream = new InputStreamContent(contentType, input);

            StorageObject objectMetadata = new StorageObject().setName(externalFileName);

            objectMetadata.setAcl(getACLOptions(acl));

			com.google.api.services.storage.Storage.Objects.Insert insertRequest = legacyClient.objects().insert(bucket, objectMetadata, contentStream);

            insertRequest.execute();
            return url + StorageUtils.encodeName(externalFileName);
        } catch (IOException ex) {
            handleIOException(ex);
            return "";
        }
    }

    public String get(String objectName, ResourceAccessControlList acl, int expirationMinutes) {
        try {
			legacyClient.objects().get(bucket, objectName).execute();
            if(isPrivateResource(acl)) {
				expirationMinutes = expirationMinutes > 0 ? expirationMinutes: DEFAULT_EXPIRATION_MINUTES;
				return betaClient.signUrl(BlobInfo.newBuilder(bucket, objectName).build(), expirationMinutes, TimeUnit.MINUTES).toString();
			}
            else
                return url + StorageUtils.encodeName(objectName);
        } catch (IOException ex) {
            handleIOException(ex);
            return "";
        }
    }

	private boolean isPrivateResource(ResourceAccessControlList acl) {
		return acl == ResourceAccessControlList.Private || (acl == ResourceAccessControlList.Default && this.defaultACL == ResourceAccessControlList.Private);
	}

	public void delete(String objectName, ResourceAccessControlList acl) {
		Boolean deleted = betaClient.delete(BlobId.of(this.bucket, objectName));
		if (!deleted) {
			logger.error("Could not delete resource: " + objectName);
		}
    }

    public String rename(String objectName, String newName, ResourceAccessControlList acl) {
        String newUrl = copy(objectName, newName, acl);
        delete(objectName, acl);
        return newUrl;
    }

    public String copy(String objectName, String newName, ResourceAccessControlList acl) {
        if (objectName.contains(url)) {
            objectName = objectName.replace(url, "");
        }

		Blob blob = betaClient.get(this.bucket, objectName);
		// Write a copy of the object to the target bucket
		CopyWriter copyWriter = blob.copyTo(this.bucket, newName);
		Blob copiedBlob = copyWriter.getResult();
		if (acl != ResourceAccessControlList.Private) { //Default ACL is Public..
			betaClient.createAcl(copiedBlob.getBlobId(), Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
		}
		return url + StorageUtils.encodeName(newName);
    }

    public String copy(String objectUrl, String newName, String tableName, String fieldName, ResourceAccessControlList acl) {
        try {
            String resourceFolderName = folder + "/" + tableName + "/" + fieldName;
            String resourceKey = resourceFolderName + "/" + newName;
            objectUrl = objectUrl.replace(url, "");
            objectUrl = URLDecoder.decode(objectUrl, "UTF-8");
            try {
                StorageObject newObject = new StorageObject();
                newObject.setMetadata(createObjectMetadata(tableName, fieldName, resourceKey));
                newObject.setAcl(getACLOptions(acl));
				com.google.api.services.storage.Storage.Objects.Copy request = legacyClient.objects().copy(bucket, objectUrl, bucket, resourceKey, newObject);
                request.execute();

                return url + StorageUtils.encodeName(resourceKey);
            } catch (Exception ex) {
                logger.error("Error saving file to external provider", ex.getMessage());
                return "";
            }
        } catch (UnsupportedEncodingException ex) {
            logger.error("Storage exception", ex.getMessage());
        }
        return "";
    }

    public long getLength(String objectName, ResourceAccessControlList acl) {
        try {
            return legacyClient.objects().get(bucket, objectName).execute().getSize().longValue();
        } catch (IOException ex) {
            handleIOException(ex);
            return 0;
        }
    }

    public Date getLastModified(String objectName, ResourceAccessControlList acl) {
        try {
            return new Date(legacyClient.objects().get(bucket, objectName).execute().getUpdated().getValue());
        } catch (IOException ex) {
            handleIOException(ex);
            return new Date();
        }
    }

    public boolean exists(String objectName, ResourceAccessControlList acl) {
        try {
			legacyClient.objects().get(bucket, objectName).execute();
            return true;
        } catch (GoogleJsonResponseException ex) {
            if (ex.getStatusCode() != OBJECT_NOT_FOUND) {
                logger.error("Error while checking if file exists", ex.getMessage());
            }
            return false;
        } catch (IOException ex) {
            handleIOException(ex);
            return false;
        }
    }

    public String getDirectory(String directoryName) {
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        if (existsDirectory(directoryName)) {
            return bucket + StorageUtils.DELIMITER + directoryName;
        } else {
            return "";
        }
    }

    public boolean existsDirectory(String directoryName) {
        boolean exists = false;
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        try {
			com.google.api.services.storage.Storage.Objects.List listObjects = legacyClient.objects().list(bucket);
            listObjects.setDelimiter(StorageUtils.DELIMITER);
            Objects objects;
            do {
                objects = listObjects.execute();
                if (objects.getPrefixes() != null) {
                    for (String object : objects.getPrefixes()) {
                        if (object.equals(directoryName)) {
                            exists = true;
                        }
                    }
                }
                listObjects.setPageToken(objects.getNextPageToken());
            } while (null != objects.getNextPageToken());
        } catch (IOException ex) {
            handleIOException(ex);
        }
        return exists;
    }

    public void createDirectory(String directoryName) {
        createFolder(StorageUtils.normalizeDirectoryName(directoryName));
    }

    public void deleteDirectory(String directoryName) {
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        try {
			com.google.api.services.storage.Storage.Objects.List listObjects = legacyClient.objects().list(bucket);
            listObjects.setPrefix(directoryName);
            Objects objects;
            do {
                objects = listObjects.execute();
                if (objects.getItems() != null) {
                    for (StorageObject object : objects.getItems()) {
                        if (isFile(object.getName(), "")) {
                            delete(object.getName(), null);
                        }
                    }
                }
                listObjects.setPageToken(objects.getNextPageToken());
            } while (null != objects.getNextPageToken());
            for (String subdir : getSubDirectories(directoryName)) {
                deleteDirectory(subdir);
            }
            if (exists(directoryName, null)) {
                delete(directoryName, null);
            }
        } catch (IOException ex) {
            handleIOException(ex);
        }
    }

    public void renameDirectory(String directoryName, String newDirectoryName) {
		ResourceAccessControlList acl = null;
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        newDirectoryName = StorageUtils.normalizeDirectoryName(newDirectoryName);
        try {
			com.google.api.services.storage.Storage.Objects.List listObjects = legacyClient.objects().list(bucket);
            listObjects.setPrefix(directoryName);
            Objects objects;
            do {
                objects = listObjects.execute();
                if (objects.isEmpty()) {
                    copy(directoryName, newDirectoryName, acl);
                    delete(directoryName, acl);
                }
                if (objects.getItems() != null) {
                    for (StorageObject object : objects.getItems()) {
                        copy(object.getName(), object.getName().replace(directoryName, newDirectoryName), acl);
                        delete(object.getName(), acl);
                    }
                }
                listObjects.setPageToken(objects.getNextPageToken());
            } while (null != objects.getNextPageToken());
            for (String subdir : getSubDirectories(directoryName)) {
                renameDirectory(subdir, subdir.replace(directoryName, newDirectoryName));
                deleteDirectory(subdir);
            }
            if (exists(directoryName, acl)) {
                delete(directoryName, acl);
            }
        } catch (IOException ex) {
            handleIOException(ex);
        }
    }

    public List<String> getFiles(String directoryName, String filter) {
        List<String> files = new ArrayList<String>();
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        try {
			com.google.api.services.storage.Storage.Objects.List listObjects = legacyClient.objects().list(bucket);
            listObjects.setPrefix(directoryName);
            Objects objects;
            do {
                objects = listObjects.execute();
                if (objects.getItems() != null) {
                    for (StorageObject object : objects.getItems()) {
                        if (isFile(object.getName(), "") && (filter.isEmpty() || object.getName().contains(filter))) {
                            files.add(object.getName());
                        }
                    }
                }
                listObjects.setPageToken(objects.getNextPageToken());
            } while (null != objects.getNextPageToken());
        } catch (IOException ex) {
            handleIOException(ex);
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
			com.google.api.services.storage.Storage.Objects.List listObjects = legacyClient.objects().list(bucket);
            listObjects.setPrefix(directoryName);
            listObjects.setDelimiter(StorageUtils.DELIMITER);
            Objects objects;
            do {
                objects = listObjects.execute();
                if (objects.getPrefixes() != null) {
                    for (String object : objects.getPrefixes()) {
                        directories.add(object);
                    }
                }
                listObjects.setPageToken(objects.getNextPageToken());
            } while (null != objects.getNextPageToken());
        } catch (IOException ex) {
            handleIOException(ex);
        }
        return directories;
    }

    public InputStream getStream(String objectName, ResourceAccessControlList acl){
        try {
			com.google.api.services.storage.Storage.Objects.Get request = legacyClient.objects().get(bucket, objectName);
            return request.executeMediaAsInputStream();
        } catch (IOException ex) {
            handleIOException(ex);
        }
        return null;
    }

    public boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg) {
        try {
            GoogleStorageException gex = (GoogleStorageException) ex;
            msg.setId(gex.getStatusCode());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isFile(String name, String directoryName) {
        return !name.endsWith(StorageUtils.DELIMITER) && (directoryName.isEmpty() || !name.replace(directoryName, "").contains(StorageUtils.DELIMITER));
    }

    void handleIOException(IOException ex) {
        if (canBuildException(ex)) {
            throw buildException(ex);
        }
        logger.error("Error " + ex.getClass(), ex);
    }

    boolean canBuildException(IOException ex) {
        return ex.getMessage().contains("<Message>") && ex.getMessage().contains("<Error><Code>");
    }

    GoogleStorageException buildException(IOException ex) {
        String msg="";
        if (ex.getMessage().contains("<Message>")) {
            msg =  ex.getMessage().split("<Message>")[1].split("</Message>")[0];
        }
        return new GoogleStorageException(msg, ex);
    }

	public String getObjectNameFromURL(String url) {
		String objectName = null;
		if (url.startsWith(this.getStorageUri()))
		{
			objectName = url.replace(this.getStorageUri(), "");
		}
		return objectName;
	}

	private String getStorageUri()
	{
		return url;
	}

    class GoogleStorageException extends RuntimeException {

        private String statusCode;

        GoogleStorageException(String msg, IOException ex) {
            super(msg, ex);
            if (ex.getMessage().contains("<Error><Code>")) {
                statusCode = ex.getMessage().split("<Error><Code>")[1].split("</Code>")[0];
            }
        }

        public String getStatusCode() {
            return statusCode;
        }
    }
}
