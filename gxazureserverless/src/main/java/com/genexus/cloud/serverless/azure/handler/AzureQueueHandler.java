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
				msg.setMessageDate(Date.from(Instant.now()));
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
		ExecuteDynamic(msgs, message);
	}
}