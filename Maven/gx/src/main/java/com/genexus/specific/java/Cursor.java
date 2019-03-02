package com.genexus.specific.java;

import com.genexus.CacheFactory;
import com.genexus.Preferences;
import com.genexus.common.interfaces.IExtensionCursor;

public class Cursor implements IExtensionCursor {

	@Override
	public int getCacheableLevel(int cacheableLvl) {
		if (CacheFactory.getForceHighestTimetoLive())
		{
			return Preferences.CHANGE_ALMOST_NEVER;
		}
		else
		{
			return cacheableLvl;
		}
	}

}
