package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.model.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.ExecutionContext;

import java.time.Instant;
import java.util.List;
import java.util.*;

public class AzureQueueHandler extends AzureEventHandler {
	public AzureQueueHandler() throws Exception {
		super();
	}
	public void run(
		@QueueTrigger(name = "message", queueName = "%queue_name%") String message,
		@BindingName("Id") final String id,
		@BindingName("DequeueCount") final long dequeCount,
		@BindingName("ExpirationTime") final String expirationTime,
		@BindingName("InsertionTime") final String insertionTime,
		@BindingName("NextVisibleTime") final String nextVisibleTime,
		@BindingName("PopReceipt") final String popReceipt,
		final ExecutionContext context) throws Exception {

		context.getLogger().info("GeneXus Queue trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());
		EventMessages msgs = new EventMessages();
		if (executor.getMethodSignatureIdx() == 0) {
				EventMessage msg = new EventMessage();
				msg.setMessageId(id);
				msg.setMessageSourceType(EventMessageSourceType.QUEUE_MESSAGE);
				try {
					String sanitizedTime = insertionTime.replace("\"", "");
					if (!sanitizedTime.endsWith("Z")) {
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
				List<EventMessageProperty> msgAtts = msg.getMessageProperties();
				msgAtts.add(new EventMessageProperty("Id", id));
				msgAtts.add(new EventMessageProperty("DequeueCount", Long.toString(dequeCount)));
				msgAtts.add(new EventMessageProperty("ExpirationTime", expirationTime));
				msgAtts.add(new EventMessageProperty("InsertionTime", insertionTime));
				msgAtts.add(new EventMessageProperty("NextVisibleTime", nextVisibleTime));
				msgAtts.add(new EventMessageProperty("PopReceipt", popReceipt));
				msgs.add(msg);
		}
		try {
			EventMessageResponse response = dispatchEvent(msgs, message);
			if (response.hasFailed()) {
				logger.error(String.format("Messages were not handled. Error: %s", response.getErrorMessage()));
				throw new RuntimeException(response.getErrorMessage()); //Throw the exception so the runtime can Retry the operation.
			}
		} catch (Exception e) {
			logger.error("HandleRequest execution error", e);
			throw e; 		//Throw the exception so the runtime can Retry the operation.
		}
	}
}