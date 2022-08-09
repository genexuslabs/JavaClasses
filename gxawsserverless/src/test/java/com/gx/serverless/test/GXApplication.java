package com.gx.serverless.test;

import java.util.*;
import javax.ws.rs.core.Application;

import com.genexus.webpanels.WebUtils;

public final class GXApplication extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> rrcs = new HashSet<Class<?>>();
		WebUtils.getGXApplicationClasses(getClass(), rrcs);
		WebUtils.AddExternalServices(getClass(), rrcs);
		return rrcs;
	}

}

