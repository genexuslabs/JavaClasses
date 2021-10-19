package com.genexus.session;

import com.genexus.Application;
import com.genexus.ICacheService2;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpSession;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;

public class HttpSessionFactory {
	static final ILogger logger = LogManager.getLogger(HttpSessionFactory.class);
	private static boolean initialized;
	private static volatile ICacheService2 instance;
	private static Object syncRoot = new Object();


	private static ICacheService2 getInstance() {
		if (!initialized && instance == null) {
			synchronized (syncRoot) {
				if (instance == null) {
					initialized = true;
					GXService providerService = Application.getGXServices().get(GXServices.SESSION_SERVICE);
					if (providerService != null) {
						String warnMsg = "Couldn't create SESSION_PROVIDER";
					}
				}
			}
		}
		return instance;
	}


	public static IHttpSession getSession(IHttpServletRequest request, boolean createIfNotExists) {
		ICacheService2 session = getInstance();

		if (session == null) {
			return request.getSession(createIfNotExists);
		}
		else {
			return new DistributedHttpSession(session, "sessionId"/*request.getCookies()[0]*/);
		}
	}
}
