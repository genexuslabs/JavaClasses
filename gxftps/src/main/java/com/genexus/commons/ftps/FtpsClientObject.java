package com.genexus.commons.ftps;

import com.genexus.ftps.FtpsOptions;
import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class FtpsClientObject extends SecurityAPIObject{

	public FtpsClientObject()
	{
		super();
	}

	public abstract boolean connect(FtpsOptions options);
	public abstract boolean put(String localPath, String remoteDir);
	public abstract boolean get(String remoteFilePath, String localDir);
	public abstract boolean rm(String remoteFilePath);
	public abstract void disconnect();
	public abstract String getWorkingDirectory();
}
