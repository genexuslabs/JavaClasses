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

	public static String getProviderObjectAbsoluteUriSafe(ExternalProvider provider, String rawObjectUri)
	{
		String providerObjectName = getProviderObjectName(provider, rawObjectUri);
		if (providerObjectName != null && rawObjectUri.indexOf("?") > 0)
		{
			rawObjectUri = rawObjectUri.substring(0, rawObjectUri.indexOf("?"));
		}
		return rawObjectUri;
	}
}
