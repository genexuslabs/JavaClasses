package com.genexus.db.driver;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.amazonaws.HttpMethod;
import com.genexus.Application;
import com.genexus.util.GXService;
import com.genexus.util.StorageUtils;
import com.genexus.StructSdtMessages_Message;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ExternalProviderS3 extends ExternalProviderBase implements ExternalProvider  {
	private static Logger logger = LogManager.getLogger(ExternalProviderS3.class);

	static final String NAME = "AWSS3";
	static final String ACCESS_KEY = "ACCESS_KEY";
	static final String SECRET_ACCESS_KEY = "SECRET_KEY";
	static final String STORAGE_CUSTOM_ENDPOINT = "CUSTOM_ENDPOINT";
	static final String STORAGE_ENDPOINT = "ENDPOINT";
	static final String BUCKET = "BUCKET_NAME";
	static final String REGION = "REGION";

	//Keep it for compatibility reasons
	@Deprecated
	static final String ACCESS_KEY_ID_DEPRECATED = "STORAGE_PROVIDER_ACCESSKEYID";
	@Deprecated
	static final String SECRET_ACCESS_KEY_DEPRECATED = "STORAGE_PROVIDER_SECRETACCESSKEY";
	@Deprecated
	static final String STORAGE_CUSTOM_ENDPOINT_DEPRECATED = "STORAGE_CUSTOM_ENDPOINT";
	@Deprecated
	static final String STORAGE_ENDPOINT_DEPRECATED = "STORAGE_ENDPOINT";
	@Deprecated
	static final String BUCKET_DEPRECATED = "BUCKET_NAME";
	@Deprecated
	static final String FOLDER_DEPRECATED = "FOLDER_NAME";
	@Deprecated
	static final String REGION_DEPRECATED = "STORAGE_PROVIDER_REGION";


    static final String ACCELERATED = "s3-accelerate.amazonaws.com";
    static final String DUALSTACK = "s3-accelerate.dualstack.amazonaws.com";
	static final String DEFAULT_REGION = "us-east-1";

    private AmazonS3 client;
    private String bucket;
    private String folder;
    private String endpointUrl = ".s3.amazonaws.com/";
	private int defaultExpirationMinutes = DEFAULT_EXPIRATION_MINUTES;

	private Boolean pathStyleUrls = false;

	public String getName(){
		return NAME;
	}

	public ExternalProviderS3(String service) throws Exception {
		this(Application.getGXServices().get(service));
	}

	public ExternalProviderS3() throws Exception{
		super(null, NAME);
		initialize();
	}

	public ExternalProviderS3(GXService providerService) throws Exception{
    	super(providerService, NAME);
		initialize();
    }

	private void initialize() throws Exception{
		String accessKey = getEncryptedPropertyValue(ACCESS_KEY, ACCESS_KEY_ID_DEPRECATED);
		String secretKey = getEncryptedPropertyValue(SECRET_ACCESS_KEY, SECRET_ACCESS_KEY_DEPRECATED);
		String bucket = getEncryptedPropertyValue(BUCKET, BUCKET_DEPRECATED);
		String folder = getPropertyValue(FOLDER, FOLDER_DEPRECATED, "");
		String region = getPropertyValue(REGION, REGION_DEPRECATED, DEFAULT_REGION);
		String endpointValue = getPropertyValue(STORAGE_ENDPOINT, STORAGE_ENDPOINT_DEPRECATED, "");
		if (endpointValue.equals("custom")) {
			endpointValue = getPropertyValue(STORAGE_CUSTOM_ENDPOINT, STORAGE_CUSTOM_ENDPOINT_DEPRECATED);
		}

		try {
			defaultExpirationMinutes = Integer.parseInt(getPropertyValue(DEFAULT_EXPIRATION, DEFAULT_EXPIRATION_DEPRECATED, Integer.toString(defaultExpirationMinutes)));
		} catch (Exception e) {
		}

		if (this.client == null) {
			if (region.length() == 0) {
				region = DEFAULT_REGION;
			}
			this.bucket = bucket;
			this.folder = folder;
			this.client = buildS3Client(accessKey, secretKey, endpointValue, region);

			bucketExists();
			ensureFolder(folder);
		}
	}

	private AmazonS3 buildS3Client(String accessKey, String secretKey, String endpoint, String region) {
		AmazonS3 s3Client;
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials));

		if (endpoint.length() > 0 && !endpoint.contains(".amazonaws.com")) {
			pathStyleUrls = true;

			s3Client = builder
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
				.disableChunkedEncoding()
				.enablePathStyleAccess()
				.build();
			endpointUrl = endpoint;
		}
		else {
			pathStyleUrls = false;
			s3Client = builder
				.withRegion(region)
				.build();
			if (endpoint.equals(ACCELERATED)) {
				s3Client.setS3ClientOptions(S3ClientOptions.builder().setAccelerateModeEnabled(true).build());
			}
			else if (endpoint.equals(DUALSTACK)) {
				s3Client.setS3ClientOptions(S3ClientOptions.builder().enableDualstack().setAccelerateModeEnabled(true).build());
			}
		}
		return s3Client;
	}

    private void bucketExists() {
        if (!client.doesBucketExistV2(bucket)) {
            logger.debug(String.format("Bucket %s doesn't exist, please create the bucket", bucket));
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
        return getResourceUrl(newName, acl, defaultExpirationMinutes);
    }

    public String copy(String objectUrl, String newName, String tableName, String fieldName, ResourceAccessControlList acl) {
        String resourceFolderName = ensureFolder(folder, tableName, fieldName);
        String resourceKey = resourceFolderName + StorageUtils.DELIMITER + newName;

        try {
			objectUrl = new URI(objectUrl).getPath();
		}
        catch (Exception e){
			logger.error("Failed to Parse Storage Object URI for Copy operation", e);
		}

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("Table", tableName);
        metadata.addUserMetadata("Field", fieldName);
        metadata.addUserMetadata("KeyValue", StorageUtils.encodeNonAsciiCharacters(resourceKey));

        CopyObjectRequest request = new CopyObjectRequest(bucket, objectUrl, bucket, resourceKey);
        request.setNewObjectMetadata(metadata);
        request.setCannedAccessControlList(internalToAWSACL(acl));
        client.copyObject(request);

		return getResourceUrl(resourceKey, acl, defaultExpirationMinutes);
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
		return (!pathStyleUrls) ? String.format("https://%s%s", this.bucket, this.endpointUrl):
			String.format("%s/%s/",  this.endpointUrl, this.bucket);
	}
}
//http://192.168.254.78:9000/java-classes-unittests/text.txt