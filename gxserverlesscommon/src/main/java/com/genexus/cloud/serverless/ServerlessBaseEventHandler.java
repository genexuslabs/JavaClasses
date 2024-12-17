package com.genexus.cloud.serverless;

import com.genexus.ApplicationContext;
import com.genexus.ModelContext;
import com.genexus.cloud.serverless.model.EventMessageResponse;
import com.genexus.cloud.serverless.model.EventMessages;
import com.genexus.cloud.serverless.model.EventMessagesList;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

import com.genexus.specific.java.Connect;

import com.genexus.util.IniFile;

public abstract class ServerlessBaseEventHandler <T extends ServerlessFunctionConfiguration> {

	protected T functionConfiguration;
	protected static ILogger logger = null;
	protected Class entryPointClass = null;
	protected static GXProcedureExecutor executor;
	private static final String GX_APPLICATION_CLASS = "GXcfg";

	protected abstract T createFunctionConfiguration();
	protected abstract T createFunctionConfiguration(String className);
	protected abstract T createFunctionConfiguration(String functionName, String className);

	protected abstract void InitializeServerlessConfig() throws Exception;
	public ServerlessBaseEventHandler() throws Exception {
		this.functionConfiguration = createFunctionConfiguration();
		initialize();
	}
	public ServerlessBaseEventHandler(String className) throws Exception {
		this.functionConfiguration = createFunctionConfiguration(className);
		initialize();
	}
	public ServerlessBaseEventHandler(String functionName, String className) throws Exception {
		this.functionConfiguration = createFunctionConfiguration(functionName,className);
		initialize();
	}
	private void initialize() throws Exception {
		//logger = LogManager.getLogger(ServerlessBaseEventHandler.class);
		logger = com.genexus.specific.java.LogManager.initialize(".", ServerlessBaseEventHandler.class);
		Connect.init();

		IniFile config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", null);
		String packageName = config.getProperty("Client", "PACKAGE", null);
		Class cfgClass;

		String cfgClassName = packageName.isEmpty() ? GX_APPLICATION_CLASS : String.format("%s.%s", packageName, GX_APPLICATION_CLASS);

		try {
			cfgClass = Class.forName(cfgClassName);
			logger.debug("Finished loading cfgClassName " + cfgClassName);
			com.genexus.Application.init(cfgClass);
			ApplicationContext.getInstance().setPoolConnections(true);
		} catch (Exception e) {
			logger.error(String.format("Failed to initialize GX AppConfig Class: %s", cfgClassName), e);
			throw e;
		}

		InitializeServerlessConfig();
	}
	protected EventMessageResponse dispatchEvent(EventMessages eventMessages, String rawMessageBody) throws Exception {
		EventMessagesList eventMessagesList = new EventMessagesList();
		return dispatchEvent(eventMessages,eventMessagesList,rawMessageBody);
	}
	protected EventMessageResponse dispatchEvent(EventMessages eventMessages, EventMessagesList eventMessagesList, String rawMessageBody) throws Exception {
		String jsonStringMessages = Helper.toJSONString(eventMessages);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("dispatchEventMessages (%s) - serialized messages: %s", functionConfiguration.getGXClassName(), jsonStringMessages));
		}

		ModelContext modelContext = new ModelContext(entryPointClass);
		EventMessageResponse response;

		try {
			response = executor.execute(modelContext, eventMessages, eventMessagesList, rawMessageBody);
		} catch (Exception e) {
			logger.error(String.format("dispatchEventmessages - program '%s' execution error", entryPointClass.getName()), e);
			throw e;
		}

		if (response.hasFailed()) {
			logger.info("dispatchEventmessages - messages not handled with success: " + response.getErrorMessage());
		} else {
			logger.debug("dispatchEventmessages - message handled with success");
		}
		return response;

	}
}

