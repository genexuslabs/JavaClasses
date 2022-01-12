package com.genexus.specific.android;

import java.io.InputStream;

import com.artech.base.services.AndroidContext;
import com.genexus.common.interfaces.IExtensionMessages;


public class Messages implements IExtensionMessages {

	@Override
	public InputStream getInputStream(String resourceName) {
		String androidResourceName = resourceName.replace(".", "_").toLowerCase(); // $NON-NLS-1$ $NON-NLS-2$
		return AndroidContext.ApplicationContext.getResourceStream(androidResourceName, "raw"); // $NON-NLS-1$
	}

}
