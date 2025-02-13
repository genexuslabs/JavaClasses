package com.genexus.cloud.serverless.helpers;
import com.genexus.cloud.serverless.model.EventMessages;

public class ServiceBusProcessedMessage {
	private final EventMessages _eventMessages;
	private final String _rawMessage;

	public ServiceBusProcessedMessage(EventMessages eventMessages, String rawMessage) {
		this._eventMessages = eventMessages;
		this._rawMessage = rawMessage;
	}

	public EventMessages getEventMessages() {
		return _eventMessages;
	}

	public String getRawMessage() {
		return _rawMessage;
	}
}

