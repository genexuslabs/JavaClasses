package com.genexus.specific.android;

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
