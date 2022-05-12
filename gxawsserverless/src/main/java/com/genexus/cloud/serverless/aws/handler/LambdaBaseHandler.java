package com.genexus.cloud.serverless.aws.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.GxUserType;
import com.genexus.ModelContext;
import com.genexus.cloud.serverless.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import com.genexus.util.IniFile;

import java.util.ArrayList;
import java.util.List;


public class LambdaBaseHandler {
	protected static ILogger logger = null;
	protected static Class<?> entryPointClass = null;
	private static String entryPointClassName;
	private static final String GX_APPLICATION_CLASS = "GXcfg";
	public static String ENTRY_POINT_CLASS_NAME_VAR = "GX_MAIN_CLASS_NAME";
	private static String packageName = null;
	protected static final String MESSAGE_COLLECTION_INPUT_CLASS_NAME = "com.genexus.genexusserverlessapi.SdtEventMessages";
	protected static final String MESSAGE_OUTPUT_COLLECTION_CLASS_NAME = "com.genexus.genexusserverlessapi.SdtEventMessageResponse";
	private static List<GXProcedureDynamicExecuteStrategy> strategies = new ArrayList<>();


	public LambdaBaseHandler() throws Exception {
		initialize();
	}

	public LambdaBaseHandler(String className) throws Exception {
		entryPointClassName = className;
		initialize();
	}

	private void initialize() throws Exception {
		logger = LogManager.initialize(".", LambdaBaseHandler.class);
		Connect.init();

		if (entryPointClassName == null) {
			entryPointClassName = System.getenv(ENTRY_POINT_CLASS_NAME_VAR);
		}
		if (entryPointClassName == null) {
			throw new Exception(String.format("'%s' Environment Variable must be defined", ENTRY_POINT_CLASS_NAME_VAR));
		}

		IniFile config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", null);
		packageName = config.getProperty("Client", "PACKAGE", null);
		Class<?> cfgClass;
		try {
			cfgClass = Class.forName(packageName.isEmpty() ? GX_APPLICATION_CLASS : String.format("%s.%s", packageName, GX_APPLICATION_CLASS));
			com.genexus.Application.init(cfgClass);
			entryPointClassName = entryPointClassName;
			logger.debug("Initializing entry Point ClassName: " + entryPointClassName);
			entryPointClass = Class.forName(entryPointClassName);
		} catch (Exception e) {
			logger.error(String.format("Failed to initialize Application for className: %s", entryPointClassName), e);
			throw e;
		}

		if (entryPointClass == null) {
			throw new Exception(String.format("GeneXus Procedure '%s' was not found. Check deployment package ", entryPointClassName));
		}
		loadStrategies();

	}


	protected EventMessageResponse dispatchEventMessages(EventMessages eventMessages, String lambdaRawMessageBody) throws Exception {
		try {
			Object[] outResponse = null;
			boolean handled = false;

			String jsonStringMessages = Helper.toJSONString(eventMessages);
			ModelContext modelContext = new ModelContext(entryPointClass);

			logger.debug(String.format("dispatchEventMessages (%s) - serialized messages: %s", entryPointClassName,  jsonStringMessages));
			for (GXProcedureDynamicExecuteStrategy stg: strategies) {
				if (outResponse == null && stg.isValid()) {
					handled = true;
					switch (stg.getId()){
						case 1:
							outResponse = stg.execute(modelContext, new String[]{ jsonStringMessages });
							break;
						case 2:
							outResponse = stg.execute(modelContext, new String[]{ lambdaRawMessageBody });
							break;
					}
				}
			}

			if (!handled) {
				throw new Exception(String.format("GeneXus Procedure '%s' does not comply with the required method signature required by Event Handlers. ", entryPointClassName));
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
		GXProcedureDynamicExecuteStrategy strategy = new GXProcedureDynamicExecuteStrategy(1 ,entryPointClassName);
		strategy.addInputParameter(MESSAGE_COLLECTION_INPUT_CLASS_NAME);
		strategy.addOutputParameter(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME);
		strategies.add(strategy);

		strategy = new GXProcedureDynamicExecuteStrategy(2, entryPointClassName);
		strategy.addInputParameter(String.class.getName());
		strategy.addOutputParameter(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME);
		strategies.add(strategy);
	}
}

