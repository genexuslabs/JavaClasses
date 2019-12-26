package com.genexus.db.driver;

import com.genexus.Application;
import com.genexus.util.GXService;
import com.genexus.util.Encryption;
import com.genexus.util.GXServices;
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

public class ExternalProviderAzureStorage implements ExternalProvider {

    private static Logger logger = LogManager.getLogger(ExternalProviderAzureStorage.class);

    static final String ACCOUNT = "ACCOUNT_NAME";
    static final String KEY = "ACCESS_KEY";
    static final String PUBLIC_CONTAINER = "PUBLIC_CONTAINER_NAME";
    static final String PRIVATE_CONTAINER = "PRIVATE_CONTAINER_NAME";

    private String account;
    private String key;
    private CloudBlobContainer publicContainer;
    private CloudBlobContainer privateContainer;
    private CloudBlobClient client;

    public ExternalProviderAzureStorage(String service) {
        this(Application.getGXServices().get(service));
    }

    public ExternalProviderAzureStorage(GXService providerService) {
        try {

            account = Encryption.decrypt64(providerService.getProperties().get(ACCOUNT));
            key = Encryption.decrypt64(providerService.getProperties().get(KEY));

            CloudStorageAccount storageAccount = CloudStorageAccount.parse(
                String.format("DefaultEndpointsProtocol=%1s;AccountName=%2s;AccountKey=%3s", "https", account, key));
            client = storageAccount.createCloudBlobClient();

            privateContainer = client.getContainerReference(Encryption.decrypt64(providerService.getProperties().get(PRIVATE_CONTAINER)).toLowerCase());
            privateContainer.createIfNotExists();
            publicContainer = client.getContainerReference(Encryption.decrypt64(providerService.getProperties().get(PUBLIC_CONTAINER)).toLowerCase());
            publicContainer.createIfNotExists();

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

    public void download(String externalFileName, String localFile, boolean isPrivate) {
        try {
            CloudBlockBlob blob = getCloudBlockBlob(externalFileName, isPrivate);
            blob.downloadToFile(localFile);
        } catch (URISyntaxException ex) {
            logger.error("Invalid URI ", ex.getMessage());
        } catch (StorageException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (java.io.IOException ioex) {
            logger.error("Error downloading file", ioex);
        }
    }

    private CloudBlockBlob getCloudBlockBlob(String fileName, boolean isPrivate) throws URISyntaxException, StorageException {
        CloudBlockBlob blob = null;
        if (isPrivate) {
            blob = privateContainer.getBlockBlobReference(fileName);
        }
        else
        {
            blob = publicContainer.getBlockBlobReference(fileName);
        }
        return blob;
    }

    public String upload(String localFile, String externalFileName, boolean isPrivate) {
        try {
            CloudBlockBlob blob = getCloudBlockBlob(externalFileName, isPrivate);
            blob.uploadFromFile(localFile);
            return blob.getUri().toString();
        } catch (URISyntaxException ex) {
            logger.error("Invalid URI ", ex.getMessage());
            return "";
        } catch (StorageException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (IOException ioex) {
            logger.error("Error uploading file", ioex);
            return "";
        }
    }

    public String upload(String externalFileName, InputStream input, boolean isPrivate) {
        try {
            CloudBlockBlob blob = getCloudBlockBlob(externalFileName, isPrivate);
            if (externalFileName.endsWith(".tmp")) {
                blob.getProperties().setContentType("image/jpeg");
            }
            BlobOutputStream blobOutputStream = blob.openOutputStream();
            int next = input.read();
            while (next != -1) {
                blobOutputStream.write(next);
                next = input.read();
            }
            blobOutputStream.close();

            return blob.getUri().toString();

        } catch (URISyntaxException ex) {
            logger.error("Invalid URI", ex);
            return "";
        } catch (StorageException sex) {
            throw new RuntimeException(sex.getMessage(), sex);
        } catch (java.io.IOException ioex) {
            logger.error("Error uploading file", ioex);
            return "";
        }
    }

    public String get(String externalFileName, boolean isPrivate, int expirationMinutes) {
        try {
            if (isPrivate) {
                return getPrivate(externalFileName, expirationMinutes);
            } else {
                CloudBlockBlob blob = publicContainer.getBlockBlobReference(externalFileName);
                if (exists(externalFileName, isPrivate)) {
                    return blob.getUri().toString();
                }
            }
        } catch (StorageException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.error("Error getting file", ex);
            return "";
        }
        return "";
    }

    private String getPrivate(String externalFileName, int expirationMinutes) {
        try {
            CloudBlockBlob blob = privateContainer.getBlockBlobReference(externalFileName);
            if (blob.exists()) {
                SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
                policy.setPermissionsFromString("r");
                Calendar date = Calendar.getInstance();
                Date expire = new Date(date.getTimeInMillis() + (expirationMinutes * 60000));
                policy.setSharedAccessExpiryTime(expire);
                return blob.getUri().toString() + "?" + blob.generateSharedAccessSignature(policy, null);
            }
        } catch (StorageException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.error("Error getting private file", ex);
            return "";
        }
        return "";
    }

    public void delete(String objectName, boolean isPrivate) {
        try {
            CloudBlockBlob blob = getCloudBlockBlob(objectName, isPrivate);
            blob.deleteIfExists();
        } catch (URISyntaxException ex) {
            logger.error("Invalid URI ", ex.getMessage());
        } catch (StorageException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public String rename(String objectName, String newName, boolean isPrivate) {
        String ret = copy(objectName, newName, isPrivate);
        delete(objectName, isPrivate);
        return ret;
    }

    public String copy(String objectName, String newName, boolean isPrivate) {
        if (objectName.startsWith(getUrl())) {
            if (isPrivate)
            {
                objectName = objectName.replace(getUrl() + privateContainer.getName() + "/", "");
            }
            else
            {
                objectName = objectName.replace(getUrl() + publicContainer.getName() + "/", "");
            }
        }
        //objectName = StorageUtils.decodeName(objectName);
        try {
            CloudBlockBlob sourceBlob = getCloudBlockBlob(objectName, isPrivate);
            CloudBlockBlob targetBlob = getCloudBlockBlob(newName, false);
            targetBlob.startCopy(sourceBlob);
            return targetBlob.getUri().toString();
        } catch (URISyntaxException ex) {
            logger.error("Invalid URI ", ex.getMessage());
        } catch (StorageException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return "";
    }

    public String copy(String objectUrl, String newName, String tableName, String fieldName, boolean isPrivate) {
        //objectUrl = StorageUtils.decodeName(objectUrl.replace(getUrl(), ""));
        objectUrl = objectUrl.replace(getUrl(), "");
        newName = tableName + "/" + fieldName + "/" + newName;
        try {
            CloudBlockBlob sourceBlob = getCloudBlockBlob(objectUrl, isPrivate);
            CloudBlockBlob targetBlob = publicContainer.getBlockBlobReference(newName);
            targetBlob.setMetadata(createObjectMetadata(tableName, fieldName, StorageUtils.encodeName(newName)));
            targetBlob.startCopy(sourceBlob);
            return targetBlob.getUri().toString();
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

    public long getLength(String objectName, boolean isPrivate) {
        try {
            CloudBlockBlob blob = getCloudBlockBlob(objectName, isPrivate);
            blob.downloadAttributes();
            return blob.getProperties().getLength();
        } catch (URISyntaxException ex) {
            logger.error("Invalid URI ", ex.getMessage());
            return 0;
        } catch (StorageException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public Date getLastModified(String objectName, boolean isPrivate) {
        try {
            CloudBlockBlob blob = getCloudBlockBlob(objectName, isPrivate);
            blob.downloadAttributes();
            return blob.getProperties().getLastModified();
        } catch (URISyntaxException ex) {
            logger.error("Invalid URI ", ex.getMessage());
            return new Date();
        } catch (StorageException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public boolean exists(String objectName, boolean isPrivate) {
        try {
            CloudBlockBlob blob = getCloudBlockBlob(objectName, isPrivate);
            return blob.exists();
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
                    if (!itemName.equals(directoryName))
                    {
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
                    delete(itemName, false);
                }
                if (isDirectory(item)) {
                    if (item instanceof CloudBlobDirectory) {
                        itemName = ((CloudBlobDirectory) item).getPrefix();
                    } else if (item instanceof CloudBlockBlob) {
                        itemName = ((CloudBlockBlob) item).getName();
                    }
                    if (!itemName.equals(directoryName))
                    {
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
        if (!existsDirectory(newDirectoryName))
        {
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
                    rename(itemName, itemName.replace(directoryName, newDirectoryName), false);
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

    public InputStream getStream(String objectName, boolean isPrivate){
        try {
            CloudBlockBlob blob = getCloudBlockBlob(objectName, isPrivate);
            blob.downloadAttributes();
            byte[] bytes= new byte[(int)blob.getProperties().getLength()];
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

    public boolean getMessageFromException(Exception ex, StructSdtMessages_Message msg){
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
        return "http://" + account + ".blob.core.windows.net/";
    }
}
