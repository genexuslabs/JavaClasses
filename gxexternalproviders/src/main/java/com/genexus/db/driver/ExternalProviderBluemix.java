package com.genexus.db.driver;

import com.genexus.Application;
import com.genexus.StructSdtMessages_Message;
import com.genexus.util.Encryption;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;
import com.genexus.util.StorageUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.ResponseException;
import org.openstack4j.api.exceptions.StatusCode;
import org.openstack4j.api.types.Facing;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.common.DLPayload;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.identity.v3.Endpoint;
import org.openstack4j.model.identity.v3.Service;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.model.storage.object.options.CreateUpdateContainerOptions;
import org.openstack4j.model.storage.object.options.ObjectListOptions;
import org.openstack4j.model.storage.object.options.ObjectLocation;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;
import java.util.logging.Level;

public class ExternalProviderBluemix implements ExternalProvider {

    private static Logger logger = LogManager.getLogger(ExternalProviderAzureStorage.class);

    static final String SERVER_URL = "SERVER_URL";
    static final String USER = "STORAGE_PROVIDER_USER";
    static final String PASSWORD = "STORAGE_PROVIDER_PASSWORD";
    static final String PROJECT_ID = "PROJECT_ID";
    static final String PUBLIC_BUCKET = "PUBLIC_BUCKET_NAME";
    static final String PRIVATE_BUCKET = "PRIVATE_BUCKET_NAME";
    static final String FOLDER = "FOLDER_NAME";
    static final String REGION = "STORAGE_PROVIDER_REGION";

    private OSClientV3 client;
    private String publicBucket;
    private String privateBucket;
    private String folder;
    private String storageUrl;
    private String authToken;
    private String tempUrlKey;

    public ExternalProviderBluemix(String service) {
        this(Application.getGXServices().get(service));
    }

    public ExternalProviderBluemix(GXService providerService) {
        String endpoint = providerService.getProperties().get(SERVER_URL) + "/v3";
        String username = Encryption.decrypt64(providerService.getProperties().get(USER));
        String password = Encryption.decrypt64(providerService.getProperties().get(PASSWORD));
        String project = providerService.getProperties().get(PROJECT_ID);
        Identifier domainId = Identifier.byName("default");
        Identifier projectId = Identifier.byId(project);
        client = OSFactory.builderV3()
            .endpoint(endpoint)
            .credentials(username, password)
            .scopeToProject(projectId, domainId)
            .authenticate();
        publicBucket = Encryption.decrypt64(providerService.getProperties().get(PUBLIC_BUCKET));
        privateBucket = Encryption.decrypt64(providerService.getProperties().get(PRIVATE_BUCKET));
        folder = providerService.getProperties().get(FOLDER);
        createBuckets();
        createFolder(folder);

        storageUrl = getObjectStorageEndpoint(providerService.getProperties().get(REGION));
        updateTemporaryContainerUrlKey();
    }

    private void updateTemporaryContainerUrlKey() {
        try {
            tempUrlKey = UUID.randomUUID().toString();
            URL url = new URL(storageUrl + "/" + privateBucket);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-Container-Meta-Temp-Url-Key", tempUrlKey);
            conn.setRequestProperty("Content-Length", "0");
            conn.setRequestProperty("X-Auth-Token", client.getToken().getId());

            if (conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                logger.error("Error" + conn.getResponseCode() + conn.getResponseMessage());
            }

            conn.disconnect();
        } catch (MalformedURLException ex) {
            logger.error("Error updating temporary container url", ex.getMessage());
        } catch (ProtocolException ex) {
            logger.error("Error updating temporary container url", ex.getMessage());
        } catch (IOException ex) {
            logger.error("Error updating temporary container url", ex.getMessage());
        }

    }

    private void createBuckets() {
        ActionResponse r = client.objectStorage().containers().create(publicBucket, CreateUpdateContainerOptions.create().accessAnybodyRead());
        if (!r.isSuccess()) {
            logger.error("Error creating bucket " + publicBucket + " " + r.getFault());
        }

        r = client.objectStorage().containers().create(privateBucket);
        if (!r.isSuccess()) {
            logger.error("Error creating bucket " + privateBucket + " " + r.getFault());
        }
    }

    private void createFolder(String folderName) {
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        client.objectStorage().objects().put(publicBucket, folderName, Payloads.create(emptyContent), ObjectPutOptions.create().contentType("application/directory"));
    }

    private String getObjectStorageEndpoint(String region) {
        for (Service s : client.getToken().getCatalog()) {
            if (s.getName().equals("swift")) {
                for (Endpoint e : s.getEndpoints()) {
                    if (e.getRegion().equals(region) && e.getIface().equals(Facing.PUBLIC)) {
                        return e.getUrl().toString();
                    }
                }
            }
        }
        logger.error("Error getting the storage endpoint");
        return null;
    }

    public void download(String externalFileName, String localFile, boolean isPrivate) {

        try {
            //Workaround issue: https://github.com/ContainX/openstack4j/issues/918
            DLPayload dp = client.objectStorage().objects().download(publicBucket, externalFileName);

            if (dp.getHttpResponse().getStatus() == 200) {
                dp.writeToFile(new File(localFile));
            } else {
                throw new ResponseException(dp.getHttpResponse().getStatusMessage(), dp.getHttpResponse().getStatus());
            }
        } catch (IOException ex) {
            logger.fatal("Error downloading file", ex);
        }
    }

    public String upload(String localFile, String externalFileName, boolean isPrivate) {
        String bucket = (isPrivate)? privateBucket: publicBucket;
        client.objectStorage().objects().put(bucket, externalFileName, Payloads.create(new File(localFile)));
        return storageUrl + StorageUtils.DELIMITER + bucket + StorageUtils.DELIMITER + StorageUtils.encodeName(externalFileName);
    }

    public String upload(String externalFileName, InputStream input, boolean isPrivate) {
        String bucket = (isPrivate)? privateBucket: publicBucket;
        ObjectPutOptions objOptions = ObjectPutOptions.create();
        if (externalFileName.endsWith(".tmp")) {
            objOptions.contentType("image/jpeg");
        }

        client.objectStorage().objects().put(bucket, externalFileName, Payloads.create(input), objOptions);
        return storageUrl + StorageUtils.DELIMITER + bucket + StorageUtils.DELIMITER + StorageUtils.encodeName(externalFileName);
    }

    public String get(String externalFileName, boolean isPrivate, int expirationMinutes) {
        if (isPrivate) {
            return getPrivate(externalFileName, expirationMinutes);
        } else if (client.objectStorage().objects().get(publicBucket, externalFileName) != null) {
            return storageUrl + StorageUtils.DELIMITER + publicBucket + StorageUtils.DELIMITER + StorageUtils.encodeName(externalFileName);
        }
        return "";
    }

    private String getPrivate(String externalFileName, int expirationMinutes) {
        if (client.objectStorage().objects().get(privateBucket, externalFileName) != null) {
            Long expires = expirationTime(expirationMinutes);
            String path = "/v1/" + storageUrl.split("/v1/")[1] + StorageUtils.DELIMITER + privateBucket + StorageUtils.DELIMITER + externalFileName;
            String key = generateKey(expires, path);
            return storageUrl + StorageUtils.DELIMITER + privateBucket + StorageUtils.DELIMITER + StorageUtils.encodeName(externalFileName) + "?temp_url_sig=" + key + "&temp_url_expires=" + expires;
        }
        return "";
    }

    //For example: v1/AUTH_2440e4fa1725452cb2e14506cb5d63ec/bucket/externalFileName
    private String generatePath(String externalFileName) {
        return "/v1/" + storageUrl.split("/v1/")[1] + StorageUtils.DELIMITER + publicBucket + StorageUtils.DELIMITER + StorageUtils.encodeName(externalFileName);
    }

    private Long expirationTime(int minutes) {
        return (System.currentTimeMillis() / 1000) + (minutes * 60);
    }

    private String generateKey(Long expires, String path) {
        try {
            String body = "GET\n" + expires.toString() + "\n" + path;
            SecretKeySpec signingKey = new SecretKeySpec(tempUrlKey.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            return toHexString(mac.doFinal(body.getBytes()));

        } catch (Exception ex) {
            logger.error("Error generating key", ex.getMessage());
        }
        return "";
    }

    public void delete(String objectName, boolean isPrivate) {
        client.objectStorage().objects().delete(publicBucket, objectName);
    }

    public String rename(String objectName, String newName, boolean isPrivate) {
        copy(objectName, newName, isPrivate);
        delete(objectName, isPrivate);
        return storageUrl + StorageUtils.DELIMITER + publicBucket + StorageUtils.DELIMITER + StorageUtils.encodeName(newName);
    }

    public String copy(String objectName, String newName, boolean isPrivate) {
        internalCopy(publicBucket, objectName, publicBucket, newName);
        return storageUrl + StorageUtils.DELIMITER + publicBucket + StorageUtils.DELIMITER + StorageUtils.encodeName(newName);
    }

    public String copy(String objectUrl, String newName, String tableName, String fieldName, boolean isPrivate) {
        objectUrl = objectUrl.replace(storageUrl + StorageUtils.DELIMITER + publicBucket + StorageUtils.DELIMITER, "");
        String resourceFolderName = folder + "/" + tableName + "/" + fieldName;
        String resourceKey = resourceFolderName + "/" + newName;

        internalCopy(publicBucket, objectUrl, publicBucket, resourceKey);
        return storageUrl + StorageUtils.DELIMITER + publicBucket + StorageUtils.DELIMITER + StorageUtils.encodeName(resourceKey);
    }

    public long getLength(String objectName, boolean isPrivate) {
        try {
            return client.objectStorage().objects().get(publicBucket, objectName).getSizeInBytes();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public Date getLastModified(String objectName, boolean isPrivate) {
        try {
            return client.objectStorage().objects().get(publicBucket, objectName).getLastModified();
        } catch (NullPointerException e) {
            return new Date();
        }
    }

    public boolean exists(String objectName, boolean isPrivate) {
        if (client.objectStorage().objects().get(publicBucket, objectName) == null) {
            return false;
        }
        return true;
    }

    public String getDirectory(String directoryName) {
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        if (existsDirectory(directoryName)) {
            return publicBucket + StorageUtils.DELIMITER + directoryName;
        } else {
            return "";
        }
    }

    public boolean existsDirectory(String directoryName) {
        return getDirectories(null).contains(StorageUtils.normalizeDirectoryName(directoryName));
    }

    public void createDirectory(String directoryName) {
        createFolder(StorageUtils.normalizeDirectoryName(directoryName));
    }

    public void deleteDirectory(String directoryName) {
        List<String> objs = getFiles(directoryName);
        objs.addAll(getSubDirectories(directoryName));
        objs.add(StorageUtils.normalizeDirectoryName(directoryName));
        for (String obj : objs) {
            delete(obj, false);
        }
    }

    public void renameDirectory(String directoryName, String newDirectoryName) {
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        newDirectoryName = StorageUtils.normalizeDirectoryName(newDirectoryName);
        for (SwiftObject obj : client.objectStorage().objects().list(publicBucket, ObjectListOptions.create().path(directoryName))) {
            copy(obj.getName(), obj.getName().replace(directoryName, newDirectoryName), false);
            delete(obj.getName(), false);
        }
    }

    public List<String> getFiles(String directoryName, String filter) {
        List<String> list = new ArrayList<String>();
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        for (SwiftObject obj : client.objectStorage().objects().list(publicBucket)) {
            if (!obj.isDirectory() && obj.getName().startsWith(directoryName) && (filter.isEmpty() || obj.getName().contains(filter))) {
                list.add(obj.getName());
            }
        }
        return list;
    }

    public List<String> getFiles(String directoryName) {
        return getFiles(directoryName, "");
    }

    public List<String> getSubDirectories(String directoryName) {
        return getDirectories(directoryName);
    }

    public boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg) {
        try {
            ResponseException aex = (ResponseException) ex;
            msg.setId(StatusCode.fromCode(aex.getStatus()).name());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> getDirectories(String directoryName) {
        List<String> list = new ArrayList<String>();
        directoryName = StorageUtils.normalizeDirectoryName(directoryName);
        for (SwiftObject obj : client.objectStorage().objects().list(publicBucket)) {
            if (directoryName == null) {
                String dir = "";
                String[] parts = obj.getName().split(StorageUtils.DELIMITER);
                int end = parts.length - 1;
                if (obj.getName().endsWith(StorageUtils.DELIMITER)) {
                    end++;
                }
                for (int i = 0; i < end; i++) {
                    dir += parts[i] + StorageUtils.DELIMITER;
                    if (!list.contains(dir)) {
                        list.add(dir);
                    }
                }
            } else if (obj.getName().startsWith(directoryName)) {
                String name = obj.getName().replace(directoryName, "");
                int i = name.indexOf(StorageUtils.DELIMITER);
                if (i != -1) {
                    name = name.substring(0, i);
                    String dir = StorageUtils.normalizeDirectoryName(directoryName + name);
                    if (!list.contains(dir)) {
                        list.add(dir);
                    }
                }
            }
        }
        list.remove(directoryName);
        return list;
    }

    public InputStream getStream(String objectName, boolean isPrivate) {
        DLPayload dp = client.objectStorage().objects().download(publicBucket, objectName);
        return dp.getInputStream();
    }

    private void internalCopy(String originContainer, String origin, String destinationContainer, String destination) {
        client.objectStorage().objects().copy(ObjectLocation.create(originContainer, origin), ObjectLocation.create(destinationContainer, destination));
    }

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static String calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }
}
