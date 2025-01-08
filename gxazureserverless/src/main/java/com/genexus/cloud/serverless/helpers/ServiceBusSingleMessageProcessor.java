package com.genexus.cloud.serverless.helpers;

import com.genexus.cloud.serverless.GXProcedureExecutor;
import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.ExecutionContext;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public class ServiceBusSingleMessageProcessor {

	public ServiceBusProcessedMessage processQueueMessage(GXProcedureExecutor executor, String messageId, String enqueuedTimeUtc, ExecutionContext context, String message){
		EventMessages eventmessages = new EventMessages();
		String rawMessage = "";
		switch (executor.getMethodSignatureIdx()) {
			case 0: {
				EventMessage msg = new EventMessage();
				msg.setMessageId(messageId);
				msg.setMessageSourceType(EventMessageSourceType.SERVICE_BUS_MESSAGE);
				try {
					String sanitizedTime = enqueuedTimeUtc.replace("\"", "");
					if (!sanitizedTime.endsWith("Z") && !sanitizedTime.matches(".*[+-]\\d{2}:\\d{2}$")) {
						sanitizedTime += "Z";
					}
					Instant instant = Instant.from(Instant.parse(sanitizedTime));
					msg.setMessageDate(Date.from(instant));
				}
				catch (Exception exception)
				{
					context.getLogger().severe(exception.toString());
				}
				msg.setMessageData(message);
				List<EventMessageProperty> msgProperties = msg.getMessageProperties();
				msgProperties.add(new EventMessageProperty("Id", messageId));
				msgProperties.add(new EventMessageProperty("EnqueuedTime", enqueuedTimeUtc));
				eventmessages.add(msg);
			}
			break;
			case 1:
			case 2:
				rawMessage = message;
				break;
		}
		return new ServiceBusProcessedMessage(eventmessages,rawMessage);
	}
}
