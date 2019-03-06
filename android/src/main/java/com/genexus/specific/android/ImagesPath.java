package com.genexus.specific.android;

import com.artech.base.services.AndroidContext;
import java.io.InputStream;

import com.genexus.ModelContext;
import com.genexus.common.interfaces.IExtensionImagesPath;

public class ImagesPath implements IExtensionImagesPath {

	@Override
	public InputStream getInputStream(String kBId) {
		InputStream is = null;
		String fileName = com.genexus.ImagesPath.RESOURCENAME.replace(".", "_").toLowerCase();
		int id = AndroidContext.ApplicationContext.getResource(fileName, "raw"); //$NON-NLS-1$
		if (id != 0)
			is = AndroidContext.ApplicationContext.openRawResource(id);
		else	
			is = AndroidContext.ApplicationContext.getResourceStream(fileName, "raw"); //$NON-NLS-1$
		return is;
	}

}
