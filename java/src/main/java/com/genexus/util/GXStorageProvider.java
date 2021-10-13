package com.genexus.util;

import java.io.File;

import com.genexus.Application;
import com.genexus.GXBaseCollection;
import com.genexus.GXutil;
import com.genexus.SdtMessages_Message;
import com.genexus.StructSdtMessages_Message;
import com.genexus.db.driver.ExternalProvider;
import com.genexus.db.driver.ResourceAccessControlList;

public class GXStorageProvider {

	protected ExternalProvider provider;

	public GXStorageProvider() {
		provider = Application.getExternalProviderAPI();
	}

	public GXStorageProvider(GXStorageProvider other) {
		provider = other.provider;
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
			ResourceAccessControlList acl = ResourceAccessControlList.PublicRead;
			String url = provider.upload(filefullpath, storageobjectfullname, acl);
			if (uploadedFile != null) {
				uploadedFile.setFileInfo(new GXExternalFileInfo(storageobjectfullname, url, provider, acl));
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
			ResourceAccessControlList acl = ResourceAccessControlList.Private;
			if (storageobjectfullname.isEmpty()) {
				storageobjectfullname = new File(filefullpath).getName();
			}
			String url = provider.upload(filefullpath, storageobjectfullname, acl);
			if (uploadedFile != null) {
				uploadedFile.setFileInfo(new GXExternalFileInfo(storageobjectfullname, url, provider, acl));
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
			provider.download(storageobjectfullname, destFileName, ResourceAccessControlList.PublicRead);
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
			provider.download(storageobjectfullname, destFileName, ResourceAccessControlList.Private);
			return true;
		} catch (Exception ex) {
			storageMessages(ex, messages);
			return false;
		}
	}

	public boolean get(String storageobjectfullname, GXFile externalFile, GXBaseCollection<SdtMessages_Message> messages) {
		try {
			validProvider();
			ResourceAccessControlList acl = ResourceAccessControlList.PublicRead;
			String url = provider.get(storageobjectfullname, acl, 0);
			if (url.isEmpty()) {
				GXutil.ErrorToMessages("Get Error", "File doesn't exist", messages);
				return false;
			}
			externalFile.setFileInfo(new GXExternalFileInfo(storageobjectfullname, url, provider, acl));
			return true;
		} catch (Exception ex) {
			storageMessages(ex, messages);
			return false;
		}

	}

	public boolean getPrivate(String storageobjectfullname, GXFile externalFile, int expirationMinutes, GXBaseCollection<SdtMessages_Message> messages) {
		try {
			validProvider();
			ResourceAccessControlList acl = ResourceAccessControlList.Private;
			String url = provider.get(storageobjectfullname, acl, expirationMinutes);
			if (url.isEmpty()) {
				GXutil.ErrorToMessages("Get Error", "File doesn't exist", messages);
				return false;
			}
			externalFile.setFileInfo(new GXExternalFileInfo(storageobjectfullname, url, provider, acl));
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
			GXExternalFileInfo directoryInfo = new GXExternalFileInfo(directoryFullName, url, provider, ResourceAccessControlList.Private);
			directoryInfo.setIsFile(false);
			externalDirectory.setDirectoryInfo(directoryInfo);
			return true;
		} catch (Exception ex) {
			storageMessages(ex, messages);
			return false;
		}

	}

	protected void storageMessages(Exception ex, GXBaseCollection<SdtMessages_Message> messages) {
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
