package com.genexus.cloud.serverless.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import com.genexus.util.IniFile;


public class LambdaBaseHandler {
	protected static ILogger logger = null;
	protected static Class<?> entryPointClass = null;
	private static final String GX_APPLICATION_CLASS = "GXcfg";
	private static String ENTRY_POINT_CLASS_NAME_VAR = "GX_MAIN_CLASS_NAME";
	private static String packageName = null;
	protected static final String MESSAGE_INPUT_CLASS_NAME = "com.genexusserverlessapi.genexusserverlessapi.SdtEventMessages";
	protected static final String MESSAGE_OUTPUT_COLLECTION_CLASS_NAME = "com.genexusserverlessapi.genexusserverlessapi.SdtEventMessageResponse";


	public LambdaBaseHandler() throws Exception {
		initialize();
	}

	private static void initialize() throws Exception {
		logger = LogManager.initialize(".", LambdaBaseHandler.class);
		Connect.init();

		String entryPointClassName = System.getenv(ENTRY_POINT_CLASS_NAME_VAR);
		if (entryPointClassName == null) {
			throw new Exception(String.format("'%s' Environment Variable must be defined", ENTRY_POINT_CLASS_NAME_VAR));
		}

		IniFile config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", null);
		packageName = config.getProperty("Client", "PACKAGE", null);
		Class<?> cfgClass;
		try {
			cfgClass = Class.forName(packageName.isEmpty() ? GX_APPLICATION_CLASS: String.format("%s.%s", packageName, GX_APPLICATION_CLASS));
			com.genexus.Application.init(cfgClass);
			entryPointClassName = packageName.isEmpty() ? entryPointClassName: String.format("%s.%s", packageName, entryPointClassName);
			logger.debug("Initializing entry Point ClassName: " + entryPointClassName);
			entryPointClass = Class.forName(entryPointClassName);
		} catch (Exception e) {
			logger.error(String.format("Failed to initialize Application for className: %s", entryPointClassName), e);
			throw e;
		}
	}
}

