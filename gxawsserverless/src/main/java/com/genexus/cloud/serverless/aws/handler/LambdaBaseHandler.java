package com.genexus.cloud.serverless.aws.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.GxUserType;
import com.genexus.ModelContext;
import com.genexus.cloud.serverless.*;
import com.genexus.cloud.serverless.model.EventMessageResponse;
import com.genexus.cloud.serverless.model.EventMessages;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import com.genexus.util.IniFile;

import java.util.ArrayList;
import java.util.List;


public class LambdaBaseHandler {
	protected static ILogger logger = null;
	protected static Class entryPointClass = null;
	private static LambdaFunctionConfiguration functionConfiguration;
	private static final String GX_APPLICATION_CLASS = "GXcfg";
	private static String packageName = null;
	protected static final String MESSAGE_COLLECTION_INPUT_CLASS_NAME = "com.genexus.genexusserverlessapi.SdtEventMessages";
	protected static final String MESSAGE_OUTPUT_COLLECTION_CLASS_NAME = "com.genexus.genexusserverlessapi.SdtEventMessageResponse";
	private static List<GXProcedureDynamicExecuteStrategy> strategies = new ArrayList<>();

	public LambdaBaseHandler() throws Exception {
		initialize();
	}

	public LambdaBaseHandler(String className) throws Exception {
		functionConfiguration = new LambdaFunctionConfiguration(className);
		initialize();
	}

	private void initialize() throws Exception {
		logger = LogManager.initialize(".", LambdaBaseHandler.class);
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
		loadStrategies();
	}

	protected EventMessageResponse dispatchEventMessages(EventMessages eventMessages, String lambdaRawMessageBody) throws Exception {
		try {
			Object[] outResponse = null;
			boolean handled = false;

			String jsonStringMessages = Helper.toJSONString(eventMessages);
			ModelContext modelContext = new ModelContext(entryPointClass);

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("dispatchEventMessages (%s) - serialized messages: %s", functionConfiguration.getEntryPointClassName(), jsonStringMessages));
			}
			for (GXProcedureDynamicExecuteStrategy stg : strategies) {
				if (outResponse == null && stg.isValid()) {
					handled = true;
					switch (stg.getId()) {
						case 1:
							outResponse = stg.execute(modelContext, new String[]{jsonStringMessages});
							break;
						case 2:
							outResponse = stg.execute(modelContext, new String[]{lambdaRawMessageBody});
							break;
					}
				}
			}

			if (!handled) {
				throw new Exception(String.format("GeneXus Procedure '%s' does not comply with the required method signature required by Event Handlers. ", functionConfiguration.getEntryPointClassName()));
			}

			GxUserType handlerOutput = (GxUserType) outResponse[0];
			String jsonResponse = handlerOutput.toJSonString(false);
			EventMessageResponse response = new ObjectMapper().readValue(jsonResponse, EventMessageResponse.class);

			if (!response.isHandled()) {
				logger.info("dispatchEventmessages - messages not handled with success: " + response.getErrorMessage());
			} else {
				logger.debug("dispatchEventmessages - message handled with success");
			}
			return response;

		} catch (Exception e) {
			logger.error("HandleRequest failed: " + entryPointClass.getName(), e);
			throw e;
		}
	}


	private void loadStrategies() throws ClassNotFoundException {
		GXProcedureDynamicExecuteStrategy strategy = new GXProcedureDynamicExecuteStrategy(1, functionConfiguration.getEntryPointClassName());
		strategy.addInputParameter(MESSAGE_COLLECTION_INPUT_CLASS_NAME);
		strategy.addOutputParameter(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME);
		strategies.add(strategy);

		strategy = new GXProcedureDynamicExecuteStrategy(2, functionConfiguration.getEntryPointClassName());
		strategy.addInputParameter(String.class.getName());
		strategy.addOutputParameter(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME);
		strategies.add(strategy);
	}
}

