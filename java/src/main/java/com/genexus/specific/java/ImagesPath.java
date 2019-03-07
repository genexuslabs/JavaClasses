package com.genexus.specific.java;

import java.io.InputStream;

import com.genexus.ModelContext;
import com.genexus.common.interfaces.IExtensionImagesPath;

public class ImagesPath implements IExtensionImagesPath {

	@Override
	public InputStream getInputStream(String kBId) {
		Class gxcfg = ModelContext.getModelContextPackageClass();
        return gxcfg.getResourceAsStream(kBId + com.genexus.ImagesPath.RESOURCENAME);

	}

}
