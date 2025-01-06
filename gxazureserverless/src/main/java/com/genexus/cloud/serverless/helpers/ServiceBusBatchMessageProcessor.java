package com.genexus.cloud.serverless.helpers;

import com.genexus.cloud.serverless.GXProcedureExecutor;
import com.genexus.cloud.serverless.JSONHelper;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessages;
import com.sun.jna.platform.win32.Guid;

public class ServiceBusBatchMessageProcessor {
	public ServiceBusProcessedMessage processQueueMessage(GXProcedureExecutor executor, String[] messages) {
		String rawMessage = "";
		EventMessages eventMessages = new EventMessages();
		switch (executor.getMethodSignatureIdx()) {
			case 0: {
				for(String message : messages)
				{
					EventMessage msg = new EventMessage();
					msg.setMessageId(Guid.GUID.newGuid().toString()); // This is a fake Id.
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
