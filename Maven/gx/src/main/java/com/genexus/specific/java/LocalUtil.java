package com.genexus.specific.java;

import com.genexus.Application;
import com.genexus.common.interfaces.IExtensionLocalUtil;

public class LocalUtil implements IExtensionLocalUtil {

	@Override
	public String getLanguage(String language) {
		return Application.getClientPreferences().getProperty("language|" + language, "culture", null);	
	}

	@Override
	public boolean IsBlankEmptyDate() {
		return Application.getClientPreferences().getBLANK_EMPTY_DATE();
	}

}
