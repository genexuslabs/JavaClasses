package com.genexus.cloud.serverless.aws.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.genexus.cloud.serverless.Helper;
import com.genexus.cloud.serverless.exception.FunctionRuntimeException;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessageResponse;
import com.genexus.cloud.serverless.model.EventMessageSourceType;
import com.genexus.cloud.serverless.model.EventMessages;
import com.genexus.webpanels.WebUtils;

import java.util.Map;

public class LambdaEventBridgeHandler extends LambdaBaseEventHandler implements RequestHandler<Map<String, Object>, String> {

	public LambdaEventBridgeHandler() throws Exception {
		super();
	}

	public LambdaEventBridgeHandler(String entryPointClassName) throws Exception {
		super(entryPointClassName);
	}

	@Override
	public String handleRequest(Map<String, Object> stringObjectMap, Context context) {
		String jsonEventRaw = Helper.toJSONString(stringObjectMap);
		if (logger.isDebugEnabled()) {
			logger.debug("handleRequest started with event: " + jsonEventRaw);
		}

		String errorMessage;
		EventMessageResponse response;

		try {
			EventMessages msgs = new EventMessages();
			EventMessage msgItem = new EventMessage();
			msgItem.setMessageSourceType(EventMessageSourceType.ServiceBusMessage);
			if (stringObjectMap.containsKey("time")) {
				msgItem.setMessageDate(WebUtils.parseDTimeParm(stringObjectMap.get("time").toString()));
			}
			msgItem.setMessageId(stringObjectMap.getOrDefault("id", "").toString());
			msgItem.setMessageData(stringObjectMap.getOrDefault("detail", "").toString());
			for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
				Helper.addEventMessageProperty(msgItem, entry.getKey(), entry.getValue().toString());
			}
			msgs.add(msgItem);
			response = dispatchEvent(msgs, jsonEventRaw);
		} catch (Exception e) {
			errorMessage = "HandleRequest execution error";
			logger.error(errorMessage, e);
			throw new FunctionRuntimeException(errorMessage, e);
		}

		if (response == null) {
			return "";
		}

		if (!response.isHandled()) {
			//Throw exception in order to mark the message as not processed.
			logger.error(String.format("Messages were not handled. Error: %s", response.getErrorMessage()));
			throw new RuntimeException(response.getErrorMessage());
		}
		return Helper.toJSONString(response);
	}
}
