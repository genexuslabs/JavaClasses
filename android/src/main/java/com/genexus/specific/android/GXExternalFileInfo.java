package com.genexus.specific.android;

import com.genexus.common.interfaces.IExtensionGXExternalFileInfo;
import com.genexus.util.GXFileCollection;

public class GXExternalFileInfo implements IExtensionGXExternalFileInfo {

	@Override
	public GXFileCollection listFiles(String filter, Object provider, String name) {
		return new GXFileCollection ();
	}

}
