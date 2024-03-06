package com.genexus.db.driver;

import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.genexus.Application;
import com.genexus.util.GXService;
import com.genexus.util.StorageUtils;
import com.genexus.StructSdtMessages_Message;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ExternalProviderS3V2 extends ExternalProviderBase implements ExternalProvider {
	private static Logger logger = LogManager.getLogger(ExternalProviderS3V2.class);

	static final String NAME = "AWSS3";
	static final String ACCESS_KEY = "ACCESS_KEY";
	static final String SECRET_ACCESS_KEY = "SECRET_KEY";
	static final String STORAGE_CUSTOM_ENDPOINT = "CUSTOM_ENDPOINT";
	static final String STORAGE_ENDPOINT = "ENDPOINT";
	static final String BUCKET = "BUCKET_NAME";
	static final String REGION = "REGION";
	static final String USE_IAM = "USE_IAM";

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
	private S3Client client;
	private S3Presigner presigner;
	private String clientRegion = "";
	private String bucket;
	private String folder;
	private String endpointUrl = ".s3.amazonaws.com/";
	private int defaultExpirationMinutes = DEFAULT_EXPIRATION_MINUTES;
	private Boolean pathStyleUrls = false;

	public String getName() {
		return NAME;
	}

	public ExternalProviderS3V2(String service) throws Exception {
		this(Application.getGXServices().get(service));
	}

	public ExternalProviderS3V2() throws Exception {
		super();
		initialize();
	}

	public ExternalProviderS3V2(GXService providerService) throws Exception {
		super(providerService);
		initialize();
	}

	private void initialize() throws Exception {
		String accessKey = getEncryptedPropertyValue(ACCESS_KEY, ACCESS_KEY_ID_DEPRECATED, "");
		String secretKey = getEncryptedPropertyValue(SECRET_ACCESS_KEY, SECRET_ACCESS_KEY_DEPRECATED, "");
		String bucket = getEncryptedPropertyValue(BUCKET, BUCKET_DEPRECATED);
		String folder = getPropertyValue(FOLDER, FOLDER_DEPRECATED, "");
		clientRegion = getPropertyValue(REGION, REGION_DEPRECATED, DEFAULT_REGION);
		String endpointValue = getPropertyValue(STORAGE_ENDPOINT, STORAGE_ENDPOINT_DEPRECATED, "");
		if (endpointValue.equals("custom")) {
			endpointValue = getPropertyValue(STORAGE_CUSTOM_ENDPOINT, STORAGE_CUSTOM_ENDPOINT_DEPRECATED);
		}

		try {
			defaultExpirationMinutes = Integer.parseInt(getPropertyValue(DEFAULT_EXPIRATION, DEFAULT_EXPIRATION_DEPRECATED, Integer.toString(defaultExpirationMinutes)));
		} catch (Exception e) {
			logger.error("Failed to parse default expiration time", e);
		}

		if (this.client == null) {
			if (clientRegion.length() == 0) {
				clientRegion = DEFAULT_REGION;
			}

			this.bucket = bucket;
			this.folder = folder;

			this.client = buildS3Client(accessKey, secretKey, endpointValue, clientRegion);
			this.presigner = buildS3Presinger(accessKey, secretKey, clientRegion);
			bucketExists();
		}
	}

	private S3Client buildS3Client(String accessKey, String secretKey, String endpoint, String region) {
		S3Client s3Client;

		boolean bUseIAM = !getPropertyValue(USE_IAM, "", "").isEmpty() || (accessKey.equals("") && secretKey.equals(""));

		S3ClientBuilder builder = bUseIAM ?
			S3Client.builder() :
			S3Client.builder().credentialsProvider(
				StaticCredentialsProvider.create(
					AwsBasicCredentials.create(accessKey, secretKey)
				)
			);

		if (bUseIAM) {
			logger.debug("Using IAM Credentials");
		}

		if (endpoint.length() > 0 && !endpoint.contains(".amazonaws.com")) {
			pathStyleUrls = true;

			s3Client = builder
				.endpointOverride(URI.create(endpoint))
				.region(Region.of(region))
				.serviceConfiguration(S3Configuration.builder()
					.pathStyleAccessEnabled(true)
					.chunkedEncodingEnabled(false)
					.build())
				.build();
			endpointUrl = endpoint;
		} else {
			pathStyleUrls = false;
			if (endpoint.equals(ACCELERATED)) {
				s3Client = builder
					.region(Region.of(region))
					.serviceConfiguration(S3Configuration.builder()
						.accelerateModeEnabled(true)
						.build())
					.build();
			} else if (endpoint.equals(DUALSTACK)) {
				s3Client = builder
					.region(Region.of(region))
					.serviceConfiguration(S3Configuration.builder()
						.dualstackEnabled(true)
						.accelerateModeEnabled(true)
						.build())
					.build();
			} else {
				s3Client = builder
					.region(Region.of(region))
					.build();
			}
		}
		return s3Client;
	}

	private S3Presigner buildS3Presinger(String accessKey, String secretKey, String region) {
		return S3Presigner.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
			.build();
	}

	private void bucketExists() {
		// There is no "bucket.exists" method, so we attempt to get metadata about the bucket
		// and if we get a 404 error then it means the bucket does not exist
		try {
			HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
				.bucket(bucket)
				.build();
			client.headBucket(headBucketRequest);
		} catch (S3Exception e) {
			if (e.statusCode() == 404)
				logger.debug(String.format("Bucket %s doesn't exist, please create the bucket", bucket));
			else
				logger.debug("Something went wrong while checking for bucket existence", e);
		}
	}

	private String ensureFolder(String... pathPart) {
		String folderName = buildPath(pathPart);
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(StorageUtils.normalizeDirectoryName(folderName))
			.contentLength(0L)
			.build();
		client.putObject(putObjectRequest, RequestBody.fromBytes(new byte[0]));
		return folderName;
	}

	public void download(String externalFileName, String localFile, ResourceAccessControlList acl) {
		try {
			ResponseBytes<GetObjectResponse> objectBytes = client.getObject(GetObjectRequest.builder()
					.bucket(bucket)
					.key(externalFileName)
					.build(),
				ResponseTransformer.toBytes());
			try (InputStream objectData = objectBytes.asInputStream()) {
				try (OutputStream outputStream = new FileOutputStream(localFile)) {
					int read;
					byte[] bytes = new byte[1024];
					while ((read = objectData.read(bytes)) != -1) {
						outputStream.write(bytes, 0, read);
					}
				} catch (IOException e) {
					logger.error("Error while processing the input stream", e);
				} finally {
					// Drain the remainder of the stream to avoid leaving the HTTP connection in an inconsistent state
					byte[] buffer = new byte[1024];
					while (objectData.read(buffer) != -1) {
						// Consume the rest of the stream without processing it
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
		client.putObject(PutObjectRequest.builder()
				.bucket(bucket)
				.key(externalFileName)
				.build(),
			RequestBody.fromFile(Paths.get(localFile)));
		// As of December 2023, some S3-compatible storages do not
		// implement every AWS S3 feature such as setting an object ACL
		if (endpointUrl.contains(".amazonaws.com"))
			client.putObjectAcl(PutObjectAclRequest.builder()
				.bucket(bucket)
				.key(externalFileName)
				.acl(internalToAWSACL(acl))
				.build());
		return getResourceUrl(externalFileName, acl, defaultExpirationMinutes);
	}

	private ObjectCannedACL internalToAWSACL(ResourceAccessControlList acl) {
		if (acl == ResourceAccessControlList.Default) {
			acl = this.defaultAcl;
		}

		ObjectCannedACL accessControl = ObjectCannedACL.PRIVATE;
		if (acl == ResourceAccessControlList.Private) {
			accessControl = ObjectCannedACL.PRIVATE;
		} else if (acl == ResourceAccessControlList.PublicRead) {
			accessControl = ObjectCannedACL.PUBLIC_READ;
		} else if (acl == ResourceAccessControlList.PublicReadWrite) {
			accessControl = ObjectCannedACL.PUBLIC_READ_WRITE;
		}
		return accessControl;
	}

	public String upload(String externalFileName, InputStream input, ResourceAccessControlList acl) {
		try {
			ByteBuffer byteBuffer = ByteBuffer.wrap(IoUtils.toByteArray(input));
			PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
				.bucket(bucket)
				.key(externalFileName)
				.contentType(externalFileName.endsWith(".tmp") ? "image/jpeg" : null);
			if (endpointUrl.contains(".amazonaws.com"))
				putObjectRequestBuilder = putObjectRequestBuilder.acl(internalToAWSACL(acl));
			PutObjectRequest putObjectRequest = putObjectRequestBuilder.build();

			PutObjectResponse response = client.putObject(putObjectRequest, RequestBody.fromByteBuffer(byteBuffer));
			if (!response.sdkHttpResponse().isSuccessful()) {
				logger.error("Error while uploading file: " + response.sdkHttpResponse().statusText().orElse("Unknown error"));
			}

			return getResourceUrl(externalFileName, acl, defaultExpirationMinutes);
		} catch (IOException ex) {
			logger.error("Error while uploading file to the external provider.", ex);
			return "";
		}
	}

	public String get(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) {
		// Send a request to AWS S3 to retrieve the metadata for the specified object to see if
		// the object exists and is accessible under the provided credentials and permissions.
		HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
			.bucket(bucket)
			.key(externalFileName)
			.build();
		client.headObject(headObjectRequest);

		return getResourceUrl(externalFileName, acl, expirationMinutes);
	}

	private String getResourceUrl(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) {
		if (internalToAWSACL(acl) == ObjectCannedACL.PRIVATE) {
			expirationMinutes = expirationMinutes > 0 ? expirationMinutes : defaultExpirationMinutes;
			Instant expiration = Instant.now().plus(Duration.ofMinutes(expirationMinutes));

			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(bucket)
				.key(externalFileName)
				.build();

			PresignedGetObjectRequest presignedGetObjectRequest =
				presigner.presignGetObject(r -> r.signatureDuration(Duration.between(Instant.now(), expiration))
					.getObjectRequest(getObjectRequest));

			return presignedGetObjectRequest.url().toString();
		} else {
			try {
				int lastIndex = Math.max(externalFileName.lastIndexOf('/'), externalFileName.lastIndexOf('\\'));
				String path = externalFileName.substring(0, lastIndex + 1);
				String fileName = externalFileName.substring(lastIndex + 1);
				String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

				String url = String.format(
					"https://%s.s3.%s.amazonaws.com/%s%s",
					bucket,
					clientRegion,
					path,
					encodedFileName
				);

				return url;
			} catch (UnsupportedEncodingException uee) {
				logger.error("Failed to encode resource URL for " + externalFileName, uee);
				return "";
			}
		}
	}

	public void delete(String objectName, ResourceAccessControlList acl) {
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(bucket)
			.key(objectName)
			.build();
		client.deleteObject(deleteObjectRequest);
	}

	public String rename(String objectName, String newName, ResourceAccessControlList acl) {
		String url = copy(objectName, newName, acl);
		delete(objectName, acl);
		return url;
	}

	public String copy(String objectName, String newName, ResourceAccessControlList acl) {
		CopyObjectRequest.Builder requestBuilder = CopyObjectRequest.builder()
			.sourceBucket(bucket)
			.sourceKey(objectName)
			.destinationBucket(bucket)
			.destinationKey(newName);
		if (endpointUrl.contains(".amazonaws.com"))
			requestBuilder = requestBuilder.acl(internalToAWSACL(acl));
		CopyObjectRequest request = requestBuilder.build();
		client.copyObject(request);
		return getResourceUrl(newName, acl, defaultExpirationMinutes);
	}

	public String copy(String objectUrl, String newName, String tableName, String fieldName, ResourceAccessControlList acl) {
		String resourceFolderName = buildPath(folder, tableName, fieldName);
		String resourceKey = resourceFolderName + StorageUtils.DELIMITER + newName;

		try {
			objectUrl = new URI(objectUrl).getPath();
		} catch (Exception e) {
			logger.error("Failed to Parse Storage Object URI for Copy operation", e);
		}

		Map<String, String> metadata = new HashMap<>();
		metadata.put("Table", tableName);
		metadata.put("Field", fieldName);
		metadata.put("KeyValue", StorageUtils.encodeNonAsciiCharacters(resourceKey));

		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucket)
			.key(objectUrl)
			.build();
		ResponseBytes<GetObjectResponse> objectBytes = client.getObjectAsBytes(getObjectRequest);

		PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
			.bucket(bucket)
			.key(resourceKey)
			.metadata(metadata)
			.contentType(getContentType(newName));
		if (endpointUrl.contains(".amazonaws.com"))
			putObjectRequestBuilder = putObjectRequestBuilder.acl(internalToAWSACL(acl));
		PutObjectRequest putObjectRequest = putObjectRequestBuilder.build();
		client.putObject(putObjectRequest, RequestBody.fromBytes(objectBytes.asByteArray()));

		return getResourceUrl(resourceKey, acl, defaultExpirationMinutes);
	}

	private String getContentType(String fileName) {
		Path path = Paths.get(fileName);
		String defaultContentType = "application/octet-stream";

		try {
			String probedContentType = Files.probeContentType(path);
			if (probedContentType == null || probedContentType.equals(defaultContentType))
				return findContentTypeByExtension(fileName);
			return probedContentType;
		} catch (IOException ioe) {
			return findContentTypeByExtension(fileName);
		}
	}

	private String findContentTypeByExtension(String fileName) {
		String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		String contentType = contentTypes.get(fileExtension);
		return contentType != null ? contentTypes.get(fileExtension) : "application/octet-stream";
	}

	private static Map<String, String> contentTypes  = new HashMap<String, String>() {{
			put("txt" 	, "text/plain");
			put("rtx" 	, "text/richtext");
			put("htm" 	, "text/html");
			put("html" , "text/html");
			put("xml" 	, "text/xml");
			put("aif"	, "audio/x-aiff");
			put("au"	, "audio/basic");
			put("wav"	, "audio/wav");
			put("bmp"	, "image/bmp");
			put("gif"	, "image/gif");
			put("jpe"	, "image/jpeg");
			put("jpeg"	, "image/jpeg");
			put("jpg"	, "image/jpeg");
			put("jfif"	, "image/pjpeg");
			put("tif"	, "image/tiff");
			put("tiff"	, "image/tiff");
			put("png"	, "image/x-png");
			put("3gp"	, "video/3gpp");
			put("3g2"	, "video/3gpp2");
			put("mpg"	, "video/mpeg");
			put("mpeg"	, "video/mpeg");
			put("mov"	, "video/quicktime");
			put("qt"	, "video/quicktime");
			put("avi"	, "video/x-msvideo");
			put("exe"	, "application/octet-stream");
			put("dll"	, "application/x-msdownload");
			put("ps"	, "application/postscript");
			put("pdf"	, "application/pdf");
			put("svg"	, "image/svg+xml");
			put("tgz"	, "application/x-compressed");
			put("zip"	, "application/x-zip-compressed");
			put("gz"	, "application/x-gzip");
			put("json"	, "application/json");
	}};

	private String buildPath(String... pathPart) {
		ArrayList<String> pathParts = new ArrayList<>();
		for (String part : pathPart) {
			if (part.length() > 0) {
				pathParts.add(part);
			}
		}
		return String.join(StorageUtils.DELIMITER, pathParts);
	}

	public long getLength(String objectName, ResourceAccessControlList acl) {
		HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
			.bucket(bucket)
			.key(objectName)
			.build();

		HeadObjectResponse objectHead = client.headObject(headObjectRequest);
		return objectHead.contentLength();
	}

	public Date getLastModified(String objectName, ResourceAccessControlList acl) {
		HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
			.bucket(bucket)
			.key(objectName)
			.build();

		HeadObjectResponse objectHead = client.headObject(headObjectRequest);
		return Date.from(objectHead.lastModified());
	}

	public boolean exists(String objectName, ResourceAccessControlList acl) {
		try {
			HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
				.bucket(bucket)
				.key(objectName)
				.build();

			client.headObject(headObjectRequest);
		} catch (S3Exception ex) {
			if (ex.statusCode() == 404) {
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
		if (directoryName == null || directoryName.isEmpty() || directoryName.equals(".") || directoryName.equals("/"))
			directoryName = "";
		else
			directoryName = StorageUtils.normalizeDirectoryName(directoryName);


		ListObjectsV2Request listObjectsRequest;
		if (!directoryName.isEmpty())
			listObjectsRequest = ListObjectsV2Request.builder()
				.bucket(bucket)
				.prefix(directoryName)
				.maxKeys(1)
				.delimiter(StorageUtils.DELIMITER)
				.build();
		else
			listObjectsRequest = ListObjectsV2Request.builder()
				.bucket(bucket)
				.prefix(directoryName)
				.maxKeys(1)
				.build();

		return client.listObjectsV2(listObjectsRequest).keyCount() > 0;
	}

	public void createDirectory(String directoryName) {
		ensureFolder(directoryName);
	}

	public void deleteDirectory(String directoryName) {
		ListObjectsV2Request listReq = ListObjectsV2Request.builder()
			.bucket(bucket)
			.prefix(directoryName)
			.build();

		ListObjectsV2Response listRes;
		do {
			listRes = client.listObjectsV2(listReq);
			for (S3Object s3Object : listRes.contents()) {
				DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
					.bucket(bucket)
					.key(s3Object.key())
					.build();
				client.deleteObject(deleteReq);
			}

			listReq = listReq.toBuilder().continuationToken(listRes.nextContinuationToken()).build();
		} while (listRes.isTruncated());
	}

	public void renameDirectory(String directoryName, String newDirectoryName) {
		final String finalDirectoryName = StorageUtils.normalizeDirectoryName(directoryName);
		final String finalNewDirectoryName = StorageUtils.normalizeDirectoryName(newDirectoryName);

		ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
			.bucket(bucket)
			.prefix(directoryName)
			.build();

		client.listObjects(listObjectsRequest).contents().forEach(file -> {
			String newKey = file.key().replace(finalDirectoryName, finalNewDirectoryName);

			CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
				.sourceBucket(bucket)
				.sourceKey(file.key())
				.destinationBucket(bucket)
				.destinationKey(newKey)
				.build();
			client.copyObject(copyObjectRequest);

			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
				.bucket(bucket)
				.key(file.key())
				.build();
			client.deleteObject(deleteObjectRequest);
		});

		deleteDirectory(finalDirectoryName);
	}

	public List<String> getFiles(String directoryName, String filter) {
		filter = (filter == null || filter.isEmpty()) ? null : filter.replace("*", "");
		List<String> files = new ArrayList<>();

		directoryName = StorageUtils.normalizeDirectoryName(directoryName);

		ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
			.bucket(bucket)
			.prefix(directoryName)
			.delimiter(StorageUtils.DELIMITER)
			.build();

		ListObjectsResponse response = client.listObjects(listObjectsRequest);

		for (S3Object file : response.contents()) {
			String key = file.key();
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
		ListObjectsV2Request listReq = ListObjectsV2Request.builder()
			.bucket(bucket)
			.prefix(directoryName)
			.delimiter("/")
			.build();

		List<String> subdirectories = new ArrayList<>();

		ListObjectsV2Response listRes = client.listObjectsV2(listReq);
		listRes.commonPrefixes().forEach(prefix -> subdirectories.add(prefix.prefix()));

		return subdirectories;
	}

	public InputStream getStream(String objectName, ResourceAccessControlList acl) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucket)
			.key(objectName)
			.build();

		ResponseInputStream<GetObjectResponse> object = client.getObject(getObjectRequest);
		return object;
	}

	public boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg) {
		try {
			S3Exception s3Exception = (S3Exception) ex;
			msg.setId(s3Exception.awsErrorDetails().errorCode());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getObjectNameFromURL(String url) {
		String objectName = null;
		if (url.startsWith(this.getStorageUri()))
			objectName = url.replace(this.getStorageUri(), "");
		else if (url.startsWith(this.getStorageUriWithoutRegion()))
			objectName = url.replace(this.getStorageUriWithoutRegion(), "");
		return objectName;
	}

	private String getStorageUri() {
		return (!pathStyleUrls) ?
			"https://" + bucket + ".s3." + clientRegion + ".amazonaws.com/" :
			".s3." + clientRegion + ".amazonaws.com//" + bucket + "/";
	}

	private String getStorageUriWithoutRegion() {
		return (!pathStyleUrls) ?
			"https://" + bucket + ".s3.amazonaws.com/" :
			".s3.amazonaws.com//" + bucket + "/";
	}
}

//http://192.168.254.78:9000/java-classes-unittests/text.txt