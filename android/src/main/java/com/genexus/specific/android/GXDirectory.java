package com.genexus.specific.android;

import com.artech.base.services.AndroidContext;
import com.genexus.common.interfaces.IExtensionGXDirectory;

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
