package com.genexus.util;

import com.genexus.db.driver.*;
import com.genexus.*;
import java.io.File;

public class GXStorageProvider {

    ExternalProvider provider;

    public GXStorageProvider() {
        provider = Application.getExternalProvider();
    }

    void validProvider() throws Exception {
        if (provider == null) {
            throw new Exception("External provider not found");
        }
    }

    public boolean upload(String filefullpath, String storageobjectfullname, GXFile uploadedFile, GXBaseCollection<SdtMessages_Message> messages) {
        try {
            validProvider();
            if (storageobjectfullname.isEmpty()) {
                storageobjectfullname = new File(filefullpath).getName();
            }
            String url = provider.upload(filefullpath, storageobjectfullname, false);
            if (uploadedFile != null) {
                uploadedFile.setFileInfo(new GXExternalFileInfo(storageobjectfullname, url, provider));
            }
            return true;
        } catch (Exception ex) {
            storageMessages(ex, messages);
            return false;
        }
    }

    public boolean uploadPrivate(String filefullpath, String storageobjectfullname, GXFile uploadedFile, GXBaseCollection<SdtMessages_Message> messages) {
        try {
            validProvider();
            if (storageobjectfullname.isEmpty()) {
                storageobjectfullname = new File(filefullpath).getName();
            }
            String url = provider.upload(filefullpath, storageobjectfullname, true);
            if (uploadedFile != null) {
                uploadedFile.setFileInfo(new GXExternalFileInfo(storageobjectfullname, url, provider));
            }
            return true;
        } catch (Exception ex) {
            storageMessages(ex, messages);
            return false;
        }
    }

    public boolean download(String storageobjectfullname, GXFile localFile, GXBaseCollection<SdtMessages_Message> messages) {
        try {
            validProvider();
            String destFileName;
            destFileName = localFile.getAbsolutePath();
            provider.download(storageobjectfullname, destFileName, false);
            return true;
        } catch (Exception ex) {
            storageMessages(ex, messages);
            return false;
        }
    }
	
	public boolean downloadPrivate(String storageobjectfullname, GXFile localFile, GXBaseCollection<SdtMessages_Message> messages) {
        try {
            validProvider();
            String destFileName;
            destFileName = localFile.getAbsolutePath();
            provider.download(storageobjectfullname, destFileName, true);
            return true;
        } catch (Exception ex) {
            storageMessages(ex, messages);
            return false;
        }
    }

    public boolean get(String storageobjectfullname, GXFile externalFile, GXBaseCollection<SdtMessages_Message> messages) {
        try {
            validProvider();
            String url = provider.get(storageobjectfullname, false, 0);
            if (url.isEmpty()) {
            	GXutil.ErrorToMessages("Get Error", "File doesn't exist", messages);
                return false;
            }
            externalFile.setFileInfo(new GXExternalFileInfo(storageobjectfullname, url, provider));
            return true;
        } catch (Exception ex) {
            storageMessages(ex, messages);
            return false;
        }

    }

    public boolean getPrivate(String storageobjectfullname, GXFile externalFile, int expirationMinutes, GXBaseCollection<SdtMessages_Message> messages) {
        try {
            validProvider();
            String url = provider.get(storageobjectfullname, true, expirationMinutes);
            if (url.isEmpty()) {
            	GXutil.ErrorToMessages("Get Error", "File doesn't exist", messages);
                return false;
            }
            externalFile.setFileInfo(new GXExternalFileInfo(storageobjectfullname, url, provider));
            return true;
        } catch (Exception ex) {
            storageMessages(ex, messages);
            return false;
        }

    }

    public boolean getDirectory(String directoryFullName, GXDirectory externalDirectory, GXBaseCollection<SdtMessages_Message> messages) {
        try {
            validProvider();
            String url = provider.getDirectory(directoryFullName);
            if (url.isEmpty()) {
            	GXutil.ErrorToMessages("Get Error", "Directory doesn't exist", messages);
                return false;
            }
            externalDirectory.setDirectoryInfo(new GXExternalFileInfo(directoryFullName, url, provider, false));
            return true;
        } catch (Exception ex) {
            storageMessages(ex, messages);
            return false;
        }

    }

    private void storageMessages(Exception ex, GXBaseCollection<SdtMessages_Message> messages) {
        if (messages != null && ex != null) {
            StructSdtMessages_Message struct = new StructSdtMessages_Message();
            if (provider!=null && provider.getMessageFromException(ex, struct)) {
                struct.setDescription(ex.getMessage());
                struct.setType((byte) 1); //error
                SdtMessages_Message msg = new SdtMessages_Message(struct);
                messages.add(msg);
            } else {
            	GXutil.ErrorToMessages("Storage Error", ex.getClass()+" " +ex.getMessage(), messages);
            }
        }
    }
}
