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
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
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


public class ExternalProviderIBM implements ExternalProvider {

	private static Logger logger = LogManager.getLogger(ExternalProviderIBM.class);

    static final String ACCESS_KEY = "STORAGE_PROVIDER_ACCESS_KEY";
    static final String SECRET_KEY = "STORAGE_PROVIDER_SECRET_KEY";
    static final String COS_ENDPOINT = "STORAGE_COS_ENDPOINT";
    static final String COS_LOCATION = "STORAGE_COS_LOCATION";
    static final String BUCKET = "BUCKET_NAME";
    static final String FOLDER = "FOLDER_NAME";

    private AmazonS3 client;
    private String bucket;
    private String folder;
    private String endpointUrl;

    /* For compatibility reasons with GX16 U6 or lower*/
    public ExternalProviderIBM(){
        this(GXServices.STORAGE_SERVICE);
    }

    public ExternalProviderIBM(String service) {
        this(Application.getGXServices().get(service));
    }

    public ExternalProviderIBM(GXService providerService) {
        String accessKey = ExternalProviderHelper.getServicePropertyValue(providerService, ACCESS_KEY, true);
        String secret = ExternalProviderHelper.getServicePropertyValue(providerService, SECRET_KEY, true);
        String location = ExternalProviderHelper.getServicePropertyValue(providerService, COS_LOCATION, false);
        String endpoint = ExternalProviderHelper.getServicePropertyValue(providerService, COS_ENDPOINT, false);
        String bucket = ExternalProviderHelper.getServicePropertyValue(providerService, BUCKET, true);
        String folder = ExternalProviderHelper.getServicePropertyValue(providerService, FOLDER, false);

		init(accessKey, secret, bucket, folder, location, endpoint);
    }


	public ExternalProviderIBM(String accessKey, String secretKey,  String bucketName, String folderName, String location, String endpoint) {
		init(accessKey, secretKey, bucketName, folderName, location, endpoint);
	}

	private void init(String accessKey, String secretKey, String bucketName, String folderName, String location, String endpoint) {
    	endpointUrl = endpoint;
		bucket = bucketName;
		folder = folderName;

		ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(10000);
		clientConfig.setUseTcpKeepAlive(true);
		SDKGlobalConfiguration.IAM_ENDPOINT = "https://iam.cloud.ibm.com/identity/token";
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withEndpointConfiguration(new EndpointConfiguration(endpointUrl, location)).withPathStyleAccessEnabled(true)
			.withClientConfiguration(clientConfig).build();

		bucketExists();
		ensureFolder(folder);
	}


	private void bucketExists() {
        if (!client.doesBucketExist(bucket)) {
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

    public void download(String externalFileName, String localFile, boolean isPrivate) {
        try {
            S3Object object = client.getObject(new GetObjectRequest(bucket, externalFileName));
            try (InputStream objectData = object.getObjectContent()) {
				try (OutputStream outputStream = new FileOutputStream(new File(localFile))){
					int read = 0;
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

    public String upload(String localFile, String externalFileName, boolean isPrivate) {
        PutObjectResult result = client.putObject(new PutObjectRequest(bucket, externalFileName, new File(localFile)).withCannedAcl(getUploadACL(isPrivate)));
        return ((AmazonS3Client) client).getResourceUrl(bucket, externalFileName);
    }

	private CannedAccessControlList getUploadACL(boolean isPrivate) {
		CannedAccessControlList accessControl = CannedAccessControlList.PublicRead;
        if (isPrivate) {
            accessControl = CannedAccessControlList.Private;
        }
		return accessControl;
	}

    public String upload(String externalFileName, InputStream input, boolean isPrivate) {
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
				PutObjectResult result = client.putObject(new PutObjectRequest(bucket, externalFileName, byteArrayInputStream, metadata).withCannedAcl(getUploadACL(isPrivate)));
				upload = ((AmazonS3Client) client).getResourceUrl(bucket, externalFileName);
			}
			return upload;
        } catch (IOException ex) {
            logger.error("Error while uploading file to the external provider.", ex);
            return "";
        }
    }

    public String get(String externalFileName, boolean isPrivate, int expirationMinutes) {
        client.getObjectMetadata(bucket, externalFileName);
        if (isPrivate) {
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

    public void delete(String objectName, boolean isPrivate) {
        client.deleteObject(bucket, objectName);
    }

    public String rename(String objectName, String newName, boolean isPrivate) {
        String url = copy(objectName, newName, isPrivate);
        delete(objectName, isPrivate);
        return url;
    }

    public String copy(String objectName, String newName, boolean isPrivate) {
        CopyObjectRequest request = new CopyObjectRequest(bucket, objectName, bucket, newName);
		request.setCannedAccessControlList(getUploadACL(isPrivate));
        client.copyObject(request);
        return ((AmazonS3Client) client).getResourceUrl(bucket, newName);
    }

    public String copy(String objectUrl, String newName, String tableName, String fieldName, boolean isPrivate) {
        String resourceFolderName = ensureFolder(folder, tableName, fieldName);
        String resourceKey = buildPath(resourceFolderName,newName) ;
        objectUrl = objectUrl.replace("https://" + bucket + endpointUrl, "");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("Table", tableName);
        metadata.addUserMetadata("Field", fieldName);
        metadata.addUserMetadata("KeyValue", resourceKey);

        CopyObjectRequest request = new CopyObjectRequest(bucket, objectUrl, bucket, resourceKey);
        request.setNewObjectMetadata(metadata);
        request.setCannedAccessControlList(getUploadACL(false));
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
    public long getLength(String objectName, boolean isPrivate) {
        ObjectMetadata obj = client.getObjectMetadata(bucket, objectName);
        return obj.getInstanceLength();
    }

    public Date getLastModified(String objectName, boolean isPrivate) {
        ObjectMetadata obj = client.getObjectMetadata(bucket, objectName);
        return obj.getLastModified();
    }

    public boolean exists(String objectName, boolean isPrivate) {
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
            rename(file.getKey(), newKey, false);
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

    public InputStream getStream(String objectName, boolean isPrivate) {
        OutputStream out = new ByteArrayOutputStream();
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
}
