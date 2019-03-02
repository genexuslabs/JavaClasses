package com.genexus.specific.java;

import com.genexus.common.interfaces.IExtensionUserLog;

public class UserLog implements IExtensionUserLog {

	@Override
	public String GetLogName() {
		return com.genexus.Preferences.getDefaultPreferences().getPropertyDefault("USER_LOG_NAMESPACE", "GeneXusUserLog");
	}

}
