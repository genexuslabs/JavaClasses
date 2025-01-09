package com.gx.serverless.test;

import com.genexus.webpanels.WebUtils;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public final class GXApplication extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> rrcs = new HashSet<Class<?>>();
		WebUtils.getGXApplicationClasses(getClass(), rrcs);
		WebUtils.AddExternalServices(getClass(), rrcs);
		return rrcs;
	}

}

