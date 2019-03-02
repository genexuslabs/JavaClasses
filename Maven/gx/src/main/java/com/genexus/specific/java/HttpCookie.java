package com.genexus.specific.java;

import com.genexus.common.interfaces.IExtensionHttpCookie;

public class HttpCookie implements IExtensionHttpCookie {

	@Override
	public boolean getHttpOnly() {
		return com.genexus.Preferences.getDefaultPreferences().getcookie_httponly_default();
	}

}
