package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.model.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.ExecutionContext;

import java.time.Instant;
import java.util.List;
import java.util.*;

/**
 * Azure Functions with Azure Storage Queue Trigger Handler.
 * https://docs.microsoft.com/en-us/azure/azure-functions/functions-bindings-storage-queue-trigger?tabs=java
 */
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

		Instant nowUtc = Instant.now();
		msg.setMessageDate(Date.from(nowUtc));
		msg.setMessageData(message);

		List<EventMessageProperty> msgAtts = msg.getMessageProperties();

		msgAtts.add(new EventMessageProperty("Id", id));
		msgAtts.add(new EventMessageProperty("DequeueCount", Long.toString(dequeCount)));
		msgAtts.add(new EventMessageProperty("ExpirationTime", expirationTime));
		msgAtts.add(new EventMessageProperty("InsertionTime", insertionTime));
		msgAtts.add(new EventMessageProperty("NextVisibleTime", nextVisibleTime));
		msgAtts.add(new EventMessageProperty("PopReceipt", popReceipt));

		msgs.add(msg);

		boolean wasHandled = false;
		String errorMessage;

		SetupServerlessMappings(context.getFunctionName());

		try {
			EventMessageResponse response = dispatchEvent(msgs, message);
			wasHandled = !response.hasFailed();
			errorMessage = response.getErrorMessage();
		} catch (Exception e) {
			errorMessage = "HandleRequest execution error";
			logger.error(errorMessage, e);
			throw e; 		//Throw the exception so the runtime can Retry the operation.
		}
	}

	@Override
	protected AzureFunctionConfiguration createFunctionConfiguration(String functionName, String className) {
		return null;
	}
	@Override
	protected AzureFunctionConfiguration createFunctionConfiguration(String className) {
		return null;
	}

	@Override
	protected AzureFunctionConfiguration createFunctionConfiguration() {
		return null;
	}
}





