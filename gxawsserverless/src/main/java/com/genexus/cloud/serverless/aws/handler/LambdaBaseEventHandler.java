package com.genexus.cloud.serverless.aws.handler;

import com.genexus.ModelContext;
import com.genexus.cloud.serverless.*;
import com.genexus.cloud.serverless.model.EventMessageResponse;
import com.genexus.cloud.serverless.model.EventMessages;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import com.genexus.util.IniFile;


public class LambdaBaseEventHandler {
	protected static ILogger logger = null;
	protected static Class entryPointClass = null;
	private static LambdaFunctionConfiguration functionConfiguration;
	private static final String GX_APPLICATION_CLASS = "GXcfg";
	private static String packageName = null;
	private static GXProcedureExecutor executor;

	public LambdaBaseEventHandler() throws Exception {
		initialize();

	}

	public LambdaBaseEventHandler(String className) throws Exception {
		functionConfiguration = new LambdaFunctionConfiguration(className);
		initialize();

	}


	private void initialize() throws Exception {
		logger = LogManager.initialize(".", LambdaBaseEventHandler.class);
		Connect.init();

		IniFile config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", null);
		packageName = config.getProperty("Client", "PACKAGE", null);
		Class cfgClass;

		String cfgClassName = packageName.isEmpty() ? GX_APPLICATION_CLASS : String.format("%s.%s", packageName, GX_APPLICATION_CLASS);
		try {
			cfgClass = Class.forName(cfgClassName);
			com.genexus.Application.init(cfgClass);
		} catch (ClassNotFoundException e) {
			logger.error(String.format("Failed to initialize GX AppConfig Class: %s", cfgClassName), e);
			throw e;
		}

		logger.debug("Initializing Function configuration");
		try {
			if (functionConfiguration == null) {
				functionConfiguration = LambdaFunctionConfigurationHelper.getFunctionConfiguration();
			}
			entryPointClass = Class.forName(functionConfiguration.getEntryPointClassName());
		} catch (Exception e) {
			logger.error(String.format("Failed to initialize Application for className: %s", functionConfiguration.getEntryPointClassName()), e);
			throw e;
		}

		if (entryPointClass == null) {
			throw new ClassNotFoundException(String.format("GeneXus Procedure '%s' was not found. Check deployment package ", functionConfiguration.getEntryPointClassName()));
		}

		executor = new GXProcedureExecutor(functionConfiguration.getEntryPointClassName());
	}

	protected EventMessageResponse dispatchEvent(EventMessages eventMessages, String lambdaRawMessageBody) throws Exception {
		String jsonStringMessages = Helper.toJSONString(eventMessages);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("dispatchEventMessages (%s) - serialized messages: %s", functionConfiguration.getEntryPointClassName(), jsonStringMessages));
		}

		ModelContext modelContext = new ModelContext(entryPointClass);
		EventMessageResponse response = null;

		try {
			response = executor.execute(modelContext, eventMessages, lambdaRawMessageBody);
		} catch (Exception e) {
			logger.error(String.format("dispatchEventmessages - program '%s' execution error", entryPointClass.getName()), e);
			throw e;
		}

		if (!response.isHandled()) {
			logger.info("dispatchEventmessages - messages not handled with success: " + response.getErrorMessage());
		} else {
			logger.debug("dispatchEventmessages - message handled with success");
		}
		return response;

	}
}

