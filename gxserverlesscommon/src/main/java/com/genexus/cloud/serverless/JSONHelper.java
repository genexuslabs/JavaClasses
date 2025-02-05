package com.genexus.cloud.serverless;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessageProperty;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

import java.text.SimpleDateFormat;

public class JSONHelper {

	private static final ILogger logger = LogManager.getLogger(JSONHelper.class);

	public static String toJSONString(Object dtoObject) {
		try {

			JsonMapper jsonMapper = new JsonMapper();
			jsonMapper.registerModule(new JavaTimeModule());
			jsonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
			jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			String json = jsonMapper.writeValueAsString(dtoObject);
			return json;
		}
		catch (Exception e) {
			logger.error("Failed to serialize object to jsonString", e);
		}
		return "";
	}

	public static void addEventMessageProperty(EventMessage msg, String key, String value) {
		msg.getMessageProperties().add(new EventMessageProperty(key, value));
	}
}
