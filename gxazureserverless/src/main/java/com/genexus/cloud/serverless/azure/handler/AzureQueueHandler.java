package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.model.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.ExecutionContext;

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

		EventMessages msgs = new EventMessages();
		EventMessage msg = new EventMessage();
		msg.setMessageId(id);
		msg.setMessageSourceType(EventMessageSourceType.QUEUE_MESSAGE);

		msg.setMessageDate(new Date());
		msg.setMessageData(message);

		List<EventMessageProperty> msgAtts = msg.getMessageProperties();

		msgAtts.add(new EventMessageProperty("Id", id));
		msgAtts.add(new EventMessageProperty("DequeueCount", Long.toString(dequeCount)));
		msgAtts.add(new EventMessageProperty("ExpirationTime", expirationTime));
		msgAtts.add(new EventMessageProperty("InsertionTime", insertionTime));
		msgAtts.add(new EventMessageProperty("NextVisibleTime", nextVisibleTime));
		msgAtts.add(new EventMessageProperty("PopReceipt", popReceipt));

		msgs.add(msg);

		SetupServerlessMappings(context.getFunctionName());

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