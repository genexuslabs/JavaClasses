package com.genexus.specific.android;

import com.genexus.Application;
import com.genexus.common.interfaces.IExtensionLocalUtil;

public class LocalUtil implements IExtensionLocalUtil {

	@Override
	public String getLanguage(String language) {
		if (!Application.hasClientPreferences())
			return com.artech.base.services.AndroidContext.ApplicationContext.getLanguageName();
		else
			return Application.getClientPreferences().getProperty("language|" + language, "culture", null);	
	}

	@Override
	public boolean IsBlankEmptyDate() {
		if (!Application.hasClientPreferences())
			return false;
		else
			return Application.getClientPreferences().getBLANK_EMPTY_DATE();
	}

}
