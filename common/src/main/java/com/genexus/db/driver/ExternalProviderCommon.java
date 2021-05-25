package com.genexus.db.driver;

public class ExternalProviderCommon {

	public static String getProviderObjectName(ExternalProvider provider, String objectNameOrUrl)
	{
		String providerObjectName = null;
		if (provider != null)
		{
			providerObjectName = provider.getObjectNameFromURL(objectNameOrUrl);
			if (providerObjectName != null && providerObjectName.indexOf("?") > 0)
			{
				providerObjectName = providerObjectName.substring(0, providerObjectName.indexOf("?"));
			}
		}
		return providerObjectName;
	}

	public static String getProviderObjectNameSafe(ExternalProvider provider, String objectNameOrUrl)
	{
		String providerObjectName = getProviderObjectName(provider, objectNameOrUrl);
		return (providerObjectName != null) ? providerObjectName: objectNameOrUrl;
	}
}
