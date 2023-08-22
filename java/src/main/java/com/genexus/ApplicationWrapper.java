package com.genexus;

import com.genexus.performance.IApplication;

public class ApplicationWrapper implements IApplication {

	@Override
	public boolean isJMXEnabled() { return Application.isJMXEnabled(); }

	public static boolean staticIsJMXEnabled(){ return Application.isJMXEnabled(); }
}
