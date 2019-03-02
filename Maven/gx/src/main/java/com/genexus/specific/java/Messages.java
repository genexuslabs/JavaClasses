package com.genexus.specific.java;

import java.io.InputStream;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.IExtensionMessages;

public class Messages implements IExtensionMessages {

	@Override
	public InputStream getInputStream(String resourceName) {
		try
		{
			Class gxcfg = ModelContext.getModelContextPackageClass();
			if (gxcfg.getName().startsWith("com.genexus.gx.deployment."))
				gxcfg = Class.forName(com.genexus.GXutil.getClassName("GXcfg"));

			return gxcfg.getResourceAsStream(resourceName);
		}
		catch(ClassNotFoundException e)
		{
			System.err.println(e.toString());
			return null;
		}
	}

}
