package com.genexus.specific.android;

import com.genexus.common.interfaces.IExtensionGXDirectory;
import com.artech.base.services.AndroidContext;

public class GXDirectory implements IExtensionGXDirectory {
	@Override
	public String getApplicationDataPath() {
		return AndroidContext.ApplicationContext.getApplicationDataPath();
	}

	@Override
	public String getTemporaryFilesPath() {
		return AndroidContext.ApplicationContext.getTemporaryFilesPath();
	}

	@Override
	public String getExternalFilesPath() {
		return AndroidContext.ApplicationContext.getExternalFilesPath();
	}

}
