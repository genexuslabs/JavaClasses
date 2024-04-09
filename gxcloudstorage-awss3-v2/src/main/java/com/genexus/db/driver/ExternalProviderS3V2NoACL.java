package com.genexus.db.driver;

import com.genexus.Application;
import com.genexus.util.GXService;
import com.genexus.util.StorageUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ExternalProviderS3V2NoACL extends ExternalProviderS3V2 {
	public ExternalProviderS3V2NoACL(String service) throws Exception {
		super(Application.getGXServices().get(service));
	}

	public ExternalProviderS3V2NoACL() throws Exception {
		super();
	}

	public ExternalProviderS3V2NoACL(GXService providerService) throws Exception {
		super(providerService);
	}

	public String upload(String localFile, String externalFileName, ResourceAccessControlList acl) {
		client.putObject(PutObjectRequest.builder()
				.bucket(bucket)
				.key(externalFileName)
				.build(),
			RequestBody.fromFile(Paths.get(localFile)));
		return getResourceUrl(externalFileName, acl, defaultExpirationMinutes);
	}

	public String upload(String externalFileName, InputStream input, ResourceAccessControlList acl) {
		try {
			ByteBuffer byteBuffer = ByteBuffer.wrap(IoUtils.toByteArray(input));
			PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
				.bucket(bucket)
				.key(externalFileName)
				.contentType(externalFileName.endsWith(".tmp") ? "image/jpeg" : null);
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

	protected String getResourceUrl(String externalFileName, ResourceAccessControlList acl, int expirationMinutes) {
		try {
			GetUrlRequest request = GetUrlRequest.builder()
				.bucket(bucket)
				.key(externalFileName)
				.build();

			URL url = client.utilities().getUrl(request);
			return url.toString();

		} catch (S3Exception e) {
			logger.error("Failed to get the URL for the given resource because " + e.awsErrorDetails().errorMessage(), e);
			return "";
		}
	}

	public String copy(String objectName, String newName, ResourceAccessControlList acl) {
		CopyObjectRequest.Builder requestBuilder = CopyObjectRequest.builder()
			.sourceBucket(bucket)
			.sourceKey(objectName)
			.destinationBucket(bucket)
			.destinationKey(newName);
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
		PutObjectRequest putObjectRequest = putObjectRequestBuilder.build();
		client.putObject(putObjectRequest, RequestBody.fromBytes(objectBytes.asByteArray()));

		return getResourceUrl(resourceKey, acl, defaultExpirationMinutes);
	}


}
