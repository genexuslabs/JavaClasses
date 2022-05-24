package com.genexus.cloud.serverless;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessageProperty;

public class Helper {

	public static String toJSONString(Object dtoObject) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(dtoObject);
		}
		catch (Exception e) { }
		return "";
	}

	public static void addEventMessageProperty(EventMessage msg,  String key, String value) {
		msg.getMessageProperties().add(new EventMessageProperty(key, value));
	}
}
