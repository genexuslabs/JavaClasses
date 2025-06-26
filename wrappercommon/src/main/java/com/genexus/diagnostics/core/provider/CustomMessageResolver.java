package com.genexus.diagnostics.core.provider;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolver;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;


public class CustomMessageResolver implements EventResolver {
	private static final String RESOLVER_NAME = "customMessage";

	CustomMessageResolver(TemplateResolverConfig config) {
	}

	static String getName() {
		return RESOLVER_NAME;
	}

	@Override
	public void resolve(LogEvent logEvent, JsonWriter jsonWriter) {
		Message message = logEvent.getMessage();
		if (message instanceof MapMessage) {
			MapMessage<?, ?> mapMessage = (MapMessage<?, ?>) message;
			Object msgValue = mapMessage.get("message");
			if (msgValue != null) {
				jsonWriter.writeString(msgValue.toString());
				return;
			}
		}
		// fallback
		jsonWriter.writeString(message.getFormattedMessage());
	}
}

