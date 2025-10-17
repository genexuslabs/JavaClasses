package com.genexus.cloud.serverless.helpers;

import com.genexus.cloud.serverless.GXProcedureExecutor;
import com.genexus.cloud.serverless.JSONHelper;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessages;
import com.microsoft.azure.functions.ExecutionContext;

import java.time.Instant;
import java.util.Date;

public class ServiceBusBatchMessageProcessor {
	public ServiceBusProcessedMessage processQueueMessage(ExecutionContext context, GXProcedureExecutor executor, String[] messages) {
		String rawMessage = "";
		EventMessages eventMessages = new EventMessages();
		switch (executor.getMethodSignatureIdx()) {
			case 0: {
				for(String message : messages)
				{
					EventMessage msg = new EventMessage();
					msg.setMessageId(context.getInvocationId()); // This is a fake Id.
					msg.setMessageDate(Date.from(Instant.now())); //This is the time of processing not the enqueued time. Until now, this metadata is not available.
					msg.setMessageData(message);
					eventMessages.add(msg);
				}
			}
			break;
			case 1:
			case 2:
				rawMessage = JSONHelper.toJSONString(messages);
				break;
		}
		return new ServiceBusProcessedMessage(eventMessages,rawMessage);
	}
}
