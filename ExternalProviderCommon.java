package com.genexus.db.driver;

public class ExternalProviderCommon {

	public static String getProviderObjectName(ExternalProvider provider, String url)
	{
		String objectName = null;
		if (provider != null)
		{
			objectName = provider.getObjectNameFromURL(url);
			if (objectName != null && objectName.indexOf("?") > 0)
			{
				objectName = objectName.substring(0, objectName.indexOf("?"));
			}
		}
		return objectName;
	}

}
