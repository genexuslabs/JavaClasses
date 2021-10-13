package com.genexus.db.driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.genexus.util.GXServices;
import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.HttpMethod;
import com.ibm.cloud.objectstorage.SDKGlobalConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3Client;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cloud.objectstorage.services.s3.model.*;
import com.ibm.cloud.objectstorage.auth.BasicAWSCredentials;

import com.genexus.Application;
import com.genexus.StructSdtMessages_Message;
import com.genexus.util.GXService;
import com.genexus.util.StorageUtils;
import com.ibm.cloud.objectstorage.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;


public class ExternalProviderIBM extends ExternalProviderBase implements ExternalProvider {

	private static Logger logger = LogManager.getLogger(ExternalProviderIBM.class);

	static final String NAME = "IBMCOS";

	static final String ACCESS_KEY = "ACCESS_KEY";
	static final String SECRET_ACCESS_KEY = "SECRET_KEY";
	static final String STORAGE_ENDPOINT =  "ENDPOINT";
	static final String REGION = "REGION";
	static final String BUCKET = "BUCKET_NAME";

	@Deprecated
    static final String ACCESS_KEY_ID_DEPRECATED = "STORAGE_PROVIDER_ACCESS_KEY";
	@Deprecated
    static final String SECRET_ACCESS_KEY_DEPRECATED = "STORAGE_PROVIDER_SECRET_KEY";
	@Deprecated
    static final String COS_ENDPOINT_DEPRECATED = "STORAGE_COS_ENDPOINT";
	@Deprecated
    static final String COS_LOCATION_DEPRECATED = "STORAGE_COS_LOCATION";
	@Deprecated
    static final String BUCKET_DEPRECATED = "BUCKET_NAME";
	@Deprecated
    static final String FOLDER_DEPRECATED = "FOLDER_NAME";

    private AmazonS3 client;
    private String bucket;
    private String folder;
    private String endpointUrl;
	private int defaultExpirationMinutes = DEFAULT_EXPIRATION_MINUTES;

    /* For compatibility reasons with GX16 U6 or lower*/
    public ExternalProviderIBM() throws Exception {
        this(GXServices.STORAGE_SERVICE);
    }

    public ExternalProviderIBM(String service) throws Exception {
        this(Application.getGXServices().get(service));
    }

    public ExternalProviderIBM(GXService providerService) throws Exception {
    	super(providerService);
		init();
    }

	public String getName() {
    	return NAME;
	}
	private void init() throws Exception {
		String accessKey = getEncryptedPropertyValue(ACCESS_KEY, ACCESS_KEY_ID_DEPRECATED);
		String secretKey = getEncryptedPropertyValue(SECRET_ACCESS_KEY, SECRET_ACCESS_KEY_DEPRECATED);
		String location = getPropertyValue(REGION, COS_LOCATION_DEPRECATED, "us-south");
		String endpoint = getPropertyValue(STORAGE_ENDPOINT, COS_ENDPOINT_DEPRECATED, String.format("s3.%s.cloud-object-storage.appdomain.cloud", location));
		bucket = getEncryptedPropertyValue(BUCKET, BUCKET_DEPRECATED);
		folder = getPropertyValue(FOLDER, FOLDER_DEPRECATED, "");

		endpointUrl = endpoint;

		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setUseTcpKeepAlive(true);
		SDKGlobalConfiguration.IAM_ENDPOINT = "https://iam.cloud.ibm.com/identity/token";
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withEndpointConfiguration(new EndpointConfiguration(endpointUrl, location))
			.withPathStyleAccessEnabled(false)
			.withClientConfiguration(clientConfig).build();

		bucketExists();
		ensureFolder(folder);
	}

    private void bucketExists() {
        if (!client.doesBucketExistV2(bucket)) {
            logger.error(String.format("Bucket %s doesn't exist, please create the bucket", bucket));
        }
    }

    private String ensureFolder(String... pathPart) {
		String folderName = buildPath(pathPart);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, StorageUtils.normalizeDirectoryName(folderName), emptyContent, metadata);
        client.putObject(putObjectRequest);
        return folderName;
    }

    public void download(String externalFileName, String localFile, ResourceAccessControlList acl) {
        try {
            S3Object object = client.getObject(new GetObjectRequest(bucket, externalFileName));
            try (InputStream objectData = object.getObjectContent()) {
				try (OutputStream outputStream = new FileOutputStream(new File(localFile))){
					int read;
					byte[] bytes = new byte[1024];
					while ((read = objectData.read(bytes)) != -1) {
						outputStream.write(bytes, 0, read);
					}
				}
			}
        } catch (FileNotFoundException ex) {
            logger.error("Error while downloading file to the external provider", ex);
        } catch (IOException ex) {
            logger.error("Error while downloading file to the external provider", ex);
        }
    }

    public String upload(String localFile, String externalFileName, ResourceAccessControlList acl) {
        client.putObject(new PutObjectRequest(bucket, externalFileName, new File(localFile)).withCannedAcl(internalToAWSACL(acl)));
        return getResourceUrl(externalFileName, acl, defaultExpirationMinutes);
    }

    private CannedAccessControlList internalToAWSACL(ResourceAccessControlList acl) {
		if (acl == ResourceAccessControlList.Default) {
			acl = this.defaultAcl;
		}

		CannedAccessControlList accessControl = CannedAccessControlList.Private;
        if (acl == ResourceAccessControlList.Private) {
            accessControl = CannedAccessControlList.Private;
        }
		else if (acl == ResourceAccessControlList.PublicRead) {
			accessControl = CannedAccessControlList.PublicRead;
		}
		else if (acl == ResourceAccessControlList.PublicReadWrite) {
			accessControl = CannedAccessControlList.PublicReadWrite;
		}
        return accessControl;
    }

    public String upload(String externalFileName, InputStream input, ResourceAccessControlList acl) {
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(input);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            if (externalFileName.endsWith(".tmp")) {
                metadata.setContentType("image/jpeg");
            }
            String upload = "";
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
				client.putObject(new PutObjectRequest(bucket, externalFileName, byteArrayInputStream, metadata).withCannedAcl(internalToAWSACL(acl)));
				upload = getResourceUrl(externalFileName, acl, defaultExpirationMinutes);
			}
			return upload;
        } catch (IOException ex) {
            logger.error("Error while uploading file to the external provider.", ex);
            return "";
        }
    }

    public String get(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) {
        client.getObjectMetadata(bucket, externalFileName);
		return getResourceUrl(externalFileName, acl, expirationMinutes);
	}

	private String getResourceUrl(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) {
		if (internalToAWSACL(acl) == CannedAccessControlList.Private) {
			expirationMinutes = expirationMinutes > 0 ? expirationMinutes: defaultExpirationMinutes;
			Date expiration = new Date();
			long msec = expiration.getTime();
			msec += 60000 * expirationMinutes;
			expiration.setTime(msec);

			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, externalFileName);
			generatePresignedUrlRequest.setMethod(HttpMethod.GET);
			generatePresignedUrlRequest.setExpiration(expiration);
			return client.generatePresignedUrl(generatePresignedUrlRequest).toString();
		} else {
			return ((AmazonS3Client) client).getResourceUrl(bucket, externalFileName);
		}
	}

	public void delete(String objectName, ResourceAccessControlList acl) {
        client.deleteObject(bucket, objectName);
    }

    public String rename(String objectName, String newName, ResourceAccessControlList acl) {
        String url = copy(objectName, newName, acl);
        delete(objectName, acl);
        return url;
    }

    public String copy(String objectName, String newName, ResourceAccessControlList acl) {
        CopyObjectRequest request = new CopyObjectRequest(bucket, objectName, bucket, newName);
        request.setCannedAccessControlList(internalToAWSACL(acl));
        client.copyObject(request);
        return ((AmazonS3Client) client).getResourceUrl(bucket, newName);
    }

    public String copy(String objectUrl, String newName, String tableName, String fieldName, ResourceAccessControlList acl) {
        String resourceFolderName = ensureFolder(folder, tableName, fieldName);
        String resourceKey = resourceFolderName + StorageUtils.DELIMITER + newName;
        objectUrl = objectUrl.replace("https://" + bucket + endpointUrl, "");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("Table", tableName);
        metadata.addUserMetadata("Field", fieldName);
        metadata.addUserMetadata("KeyValue", StorageUtils.encodeNonAsciiCharacters(resourceKey));

        CopyObjectRequest request = new CopyObjectRequest(bucket, objectUrl, bucket, resourceKey);
        request.setNewObjectMetadata(metadata);
        request.setCannedAccessControlList(internalToAWSACL(acl));
        client.copyObject(request);

        return ((AmazonS3Client) client).getResourceUrl(bucket, resourceKey);
    }

    private String buildPath(String... pathPart) {
		ArrayList<String> pathParts = new ArrayList<>();
		for(String part : pathPart){
			if (part.length() > 0) {
				pathParts.add(part);
			}
		}
		return String.join(StorageUtils.DELIMITER, pathParts);
	}
    public long getLength(String objectName, ResourceAccessControlList acl) {
        ObjectMetadata obj = client.getObjectMetadata(bucket, objectName);
        return obj.getInstanceLength();
    }

    public Date getLastModified(String objectName, ResourceAccessControlList acl) {
        ObjectMetadata obj = client.getObjectMetadata(bucket, objectName);
        return obj.getLastModified();
    }

    public boolean exists(String objectName, ResourceAccessControlList acl) {
        try {
            client.getObjectMetadata(bucket, objectName);
        } catch (AmazonS3Exception ex) {
            if (ex.getStatusCode() == 404) {
                return false;
            }
        }
        return true;
    }

    public String getDirectory(String directoryName) {
        if (existsDirectory(directoryName)) {
            return bucket + ":" + StorageUtils.DELIMITER + directoryName + StorageUtils.DELIMITER;
        } else {
            return "";
        }
    }

    public boolean existsDirectory(String directoryName) {
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucket).withDelimiter(StorageUtils.DELIMITER);
        List<String> directories = new ArrayList<String>();
        for (String prefix : client.listObjects(listObjectsRequest).getCommonPrefixes()) {
            directories.add(prefix);
            getAllDirectories(prefix, directories);
        }
        return directories.contains(directoryName);
    }

    public void getAllDirectories(String directoryName, List<String> directories) {
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucket).withPrefix(directoryName).withDelimiter(StorageUtils.DELIMITER);
        for (String prefix : client.listObjects(listObjectsRequest).getCommonPrefixes()) {
            directories.add(prefix);
            getAllDirectories(prefix, directories);
        }
    }

    public void createDirectory(String directoryName) {
        ensureFolder(directoryName);
    }

    public void deleteDirectory(String directoryName) {
        for (S3ObjectSummary file : client.listObjects(bucket, directoryName).getObjectSummaries()) {
            client.deleteObject(bucket, file.getKey());
        }
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucket).withDelimiter(StorageUtils.DELIMITER);
        ObjectListing list = client.listObjects(listObjectsRequest);
        List<String> toRemove = new ArrayList<String>();
        List<String> prefixes = list.getCommonPrefixes();
        for (String prefix : prefixes) {
            if (prefix.startsWith(directoryName)) {
                toRemove.add(prefix);
            }
        }
        prefixes.removeAll(toRemove);
        list.setCommonPrefixes(prefixes);
    }

    public void renameDirectory(String directoryName, String newDirectoryName) {
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        newDirectoryName = StorageUtils.normalizeDirectoryName(newDirectoryName);
        ensureFolder(newDirectoryName);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucket).withPrefix(directoryName);
        for (S3ObjectSummary file : client.listObjects(listObjectsRequest).getObjectSummaries()) {
            String newKey = file.getKey().replace(directoryName, newDirectoryName);
            rename(file.getKey(), newKey, null);
        }
        deleteDirectory(directoryName);
    }

    public List<String> getFiles(String directoryName, String filter) {
        filter = (filter == null || filter.isEmpty())? null: filter.replace("*", "");
        List<String> files = new ArrayList<String>();
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucket).withPrefix(directoryName).withDelimiter(StorageUtils.DELIMITER);
        for (S3ObjectSummary file : client.listObjects(listObjectsRequest).getObjectSummaries()) {
            String key = file.getKey();
            if (isFile(directoryName, key) && (filter == null || filter.isEmpty() || key.contains(filter))) {
                files.add(key);
            }
        }
        return files;
    }

    private boolean isFile(String directory, String name) {
        return !name.endsWith(StorageUtils.DELIMITER);
    }

    public List<String> getFiles(String directoryName) {
        return getFiles(directoryName, null);
    }

    public List<String> getSubDirectories(String directoryName) {
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucket).withPrefix(directoryName)
            .withDelimiter(StorageUtils.DELIMITER);
        ObjectListing objects = client.listObjects(listObjectsRequest);
        return objects.getCommonPrefixes();
    }

    public InputStream getStream(String objectName, ResourceAccessControlList acl) {
        S3Object object = client.getObject(new GetObjectRequest(bucket, objectName));
        return object.getObjectContent();
    }

    public boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg) {
        try {
            AmazonS3Exception aex = (AmazonS3Exception) ex;
            msg.setId(aex.getErrorCode());
            return true;
        } catch (Exception e) {
            return false;
        }
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
		return String.format("https://%s.%s/", this.bucket, this.endpointUrl);
	}

}
