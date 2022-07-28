package com.genexus.cloud.serverless.aws.handler;

import com.genexus.ApplicationContext;
import com.genexus.cloud.serverless.aws.LambdaHandler;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import com.genexus.util.IniFile;

import javax.ws.rs.core.Application;

public class LambdaHelper {
	private static final String GX_APPLICATION_CLASS = "GXApplication";

	public static Application initialize() throws Exception {
		Connect.init();
		IniFile config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", null);
		String className = config.getProperty("Client", "PACKAGE", null);
		Class<?> cls;
		try {
			cls = Class.forName(className.isEmpty() ? GX_APPLICATION_CLASS: String.format("%s.%s", className, GX_APPLICATION_CLASS));
			Application app = (Application) cls.getDeclaredConstructor().newInstance();
			ApplicationContext appContext = ApplicationContext.getInstance();
			appContext.setServletEngine(true);
			appContext.setServletEngineDefaultPath("");
			com.genexus.Application.init(cls);
			return app;
		} catch (Exception e) {
			throw e;
		}
	}
}
