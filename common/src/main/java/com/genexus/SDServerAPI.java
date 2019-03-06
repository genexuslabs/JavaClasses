package com.genexus;

import com.genexus.GXSmartCacheProvider;

public class SDServerAPI
{
	
	static public void invalidateCache()
	{
		GXSmartCacheProvider.invalidateAll();
	}

	static public void invalidateCacheItem(String item)
	{
		GXSmartCacheProvider.invalidate(item);
	}
}