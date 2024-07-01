package com.genexus.commons.sftp;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.sftp.SftpOptions;

public abstract class SftpClientObject extends SecurityAPIObject{

	public SftpClientObject() {
		super();
	}

	public abstract boolean connect(SftpOptions options);
	public abstract boolean put(String localPath, String remoteDir);
	public abstract boolean get(String remoteFilePath, String localDir);
	public abstract boolean rm(String remoteFilePath);
	public abstract void disconnect();
	public abstract String getWorkingDirectory();

}
