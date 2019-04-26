package com.genexus.specific.java;

import java.lang.reflect.Method;

import com.genexus.ModelContext;
import com.genexus.PrivateUtilities;
import com.genexus.common.interfaces.IExtensionGXXMLSerializable;

public class GXXMLSerializable implements IExtensionGXXMLSerializable {
	String externalHandlerManager = "com.genexus.util.GXSoapHandler";
	

	@Override
	public void addExternalSoapHandler(int remoteHandle, Object context, String serviceName, Object objProvider) {
			
			javax.xml.ws.BindingProvider bProvider = (javax.xml.ws.BindingProvider) objProvider;

			if (PrivateUtilities.isClassPresent(externalHandlerManager))
			{			
				try
				{
					Class c = Class.forName(externalHandlerManager);
					Method m = c.getMethod("setHandlers", new Class[]{Integer.class, ModelContext.class, String.class, javax.xml.ws.BindingProvider.class});
					m.invoke(null, new Object[]{remoteHandle, context, serviceName, bProvider});
				}
				catch(Exception e)
				{
					System.err.println(e.toString());
				}				
			}		

	}

}
