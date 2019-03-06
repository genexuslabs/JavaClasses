package com.genexus.specific.android;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.GXSmartCacheProvider.DataUpdateStatus;
import com.genexus.GXSmartCacheProvider.SmartCacheStatus;
import com.genexus.ICacheService;
import com.genexus.common.interfaces.IExtensionGXSmartCacheProvider;
import com.genexus.common.interfaces.IGXSmartCacheProvider;

public class GXSmartCacheProvider implements IExtensionGXSmartCacheProvider {

	@Override
	public IGXSmartCacheProvider createCache() {
		return new AndroidSmartCacheProvider();
	}
	
	class AndroidSmartCacheProvider extends com.genexus.BaseProvider
	{
		
	}

}
