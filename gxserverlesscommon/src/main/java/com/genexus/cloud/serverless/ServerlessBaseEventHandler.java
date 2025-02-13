package com.genexus.cloud.serverless;

import com.genexus.Application;
import com.genexus.ApplicationContext;
import com.genexus.ConfigFileFinder;
import com.genexus.ModelContext;
import com.genexus.cloud.serverless.model.EventMessageResponse;
import com.genexus.cloud.serverless.model.EventMessages;
import com.genexus.cloud.serverless.model.EventMessagesList;
import com.genexus.specific.java.Connect;
import com.genexus.util.IniFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ServerlessBaseEventHandler <T extends ServerlessFunctionConfiguration> {

	protected static Logger logger = LogManager.getLogger(ServerlessBaseEventHandler.class);
	protected T functionConfiguration;
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
		Connect.init();
		IniFile config = ConfigFileFinder.getConfigFile(null, "client.cfg", null);
		String packageName = config.getProperty("Client", "PACKAGE", null);
		String cfgClassName = packageName.isEmpty() ? GX_APPLICATION_CLASS : String.format("%s.%s", packageName, GX_APPLICATION_CLASS);
		try {
			Class cfgClass;
			cfgClass = Class.forName(cfgClassName);
			logger.debug("Finished loading cfgClassName " + cfgClassName);
			Application.init(cfgClass);
			ApplicationContext.getInstance().setPoolConnections(true);
		} catch (Exception e) {
			logger.error(String.format("Failed to initialize GX AppConfig Class: %s", cfgClassName), e);
			throw e;
		}
		InitializeServerlessConfig();
	}

	protected void ExecuteDynamic(EventMessages msgs, String rawMessage) throws Exception {
		EventMessagesList eventMessagesList = new EventMessagesList();
		ExecuteDynamic(msgs,eventMessagesList,rawMessage);
	}
	protected void ExecuteDynamic(EventMessages msgs, EventMessagesList eventMessagesList, String rawMessage) throws Exception {
		try {
			EventMessageResponse response = dispatchEvent(msgs, eventMessagesList, rawMessage);
			if (response.hasFailed()) {
				logger.error(String.format("Messages were not handled. Error: %s", response.getErrorMessage()));
				throw new RuntimeException(response.getErrorMessage()); //Throw the exception so the runtime can Retry the operation.
			}
		} catch (Exception e) {
			logger.error(String.format("HandleRequest execution error: %s",e));
			throw e; 		//Throw the exception so the runtime can Retry the operation.
		}
	}
	protected EventMessageResponse dispatchEvent(EventMessages eventMessages, EventMessagesList eventMessagesList, String rawMessageBody) throws Exception {
		String jsonStringMessages = JSONHelper.toJSONString(eventMessages);

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

