package com.genexus.cloud.serverless;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessageProperty;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class Helper {

	private static final ILogger logger = LogManager.getLogger(Helper.class);

	public static String toJSONString(Object dtoObject) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(dtoObject);
		}
		catch (Exception e) {
			logger.error("Failed to serialize object to jsonString", e);
		}
		return "";
	}

	public static void addEventMessageProperty(EventMessage msg,  String key, String value) {
		msg.getMessageProperties().add(new EventMessageProperty(key, value));
	}
}
