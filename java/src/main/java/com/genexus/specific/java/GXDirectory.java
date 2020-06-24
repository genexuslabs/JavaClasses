package com.genexus.specific.java;

import com.genexus.common.interfaces.IExtensionGXDirectory;

public class GXDirectory implements IExtensionGXDirectory {

	@Override
	public String getApplicationDataPath() {
		return System.getProperty("user.home");
	}

	@Override
	public String getTemporaryFilesPath() {
		return System.getProperty("java.io.tmpdir");
	}

	@Override
	public String getExternalFilesPath() {
		return getApplicationDataPath();
	}

	@Override
	public String getCacheFilesPath() {
		return System.getProperty("java.io.tmpdir");
	}

}
