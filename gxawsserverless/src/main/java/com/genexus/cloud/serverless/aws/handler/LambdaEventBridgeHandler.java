package com.genexus.cloud.serverless.aws.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.genexus.cloud.serverless.JSONHelper;
import com.genexus.cloud.serverless.model.*;
import com.genexus.cloud.serverless.exception.FunctionRuntimeException;
import com.genexus.json.JSONObjectWrapper;
import org.apache.http.client.utils.DateUtils;

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
		String jsonEventRaw = JSONHelper.toJSONString(stringObjectMap);

		logger.debug("handleRequest started with event: " + jsonEventRaw);

		String errorMessage;
		EventMessageResponse response;

		try {
			EventMessages msgs = new EventMessages();
			EventMessage msgItem = new EventMessage();
			msgItem.setMessageSourceType(EventMessageSourceType.SERVICE_BUS_MESSAGE);
			if (stringObjectMap.containsKey("time")) {
				msgItem.setMessageDate(DateUtils.parseDate(stringObjectMap.get("time").toString()));
			}
			msgItem.setMessageId(stringObjectMap.getOrDefault("id", "").toString());
			if (stringObjectMap.containsKey("detail")) {
				msgItem.setMessageData(new JSONObjectWrapper(jsonEventRaw).getJSONObject("detail").toString());
			}
			for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
				JSONHelper.addEventMessageProperty(msgItem, entry.getKey(), entry.getValue().toString());
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

		if (response.hasFailed()) {
			//Throw exception in order to mark the message as not processed.
			logger.error(String.format("Messages were not handled. Error: %s", response.getErrorMessage()));
			throw new RuntimeException(response.getErrorMessage());
		}
		return JSONHelper.toJSONString(response);
	}
}
