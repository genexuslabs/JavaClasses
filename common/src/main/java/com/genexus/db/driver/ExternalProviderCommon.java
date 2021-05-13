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

	public static String getNormalizedProviderUrl(ExternalProvider provider, String url)
	{
		if (provider != null)
		{
			String objectName = provider.getObjectNameFromURL(url);
			if (objectName != null && objectName.indexOf("?") > 0)
			{
				url = url.substring(0, url.indexOf("?"));
			}
		}
		return url;
	}

}
