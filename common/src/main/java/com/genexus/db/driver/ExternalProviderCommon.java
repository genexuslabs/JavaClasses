package com.genexus.db.driver;

import com.genexus.CommonUtil;

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

	public static String getProviderObjectNameOrRelative(ExternalProvider provider, String objectNameOrUrl)
	{
		if (!CommonUtil.isAbsoluteURL(objectNameOrUrl)){
			return objectNameOrUrl;
		}
		return  getProviderObjectName(provider, objectNameOrUrl);
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
