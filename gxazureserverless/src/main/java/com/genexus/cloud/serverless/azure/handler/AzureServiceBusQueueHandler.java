package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.helpers.ServiceBusBatchMessageProcessor;
import com.genexus.cloud.serverless.helpers.ServiceBusProcessedMessage;
import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.*;

public class AzureServiceBusQueueHandler extends AzureEventHandler {

	public AzureServiceBusQueueHandler() throws Exception {
		super();
	}

	public void run(
		@ServiceBusQueueTrigger(name = "messages", queueName = "%queue_name%", connection = "%queue_connection%", cardinality = Cardinality.MANY)
		String[] messages,
		final ExecutionContext context
	) throws Exception {

		context.getLogger().info("GeneXus Service Bus Queue trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());
		ServiceBusBatchMessageProcessor queueBatchMessageProcessor = new ServiceBusBatchMessageProcessor();
		ServiceBusProcessedMessage queueMessage = queueBatchMessageProcessor.processQueueMessage(context, executor, messages);
		try {
			EventMessageResponse response = dispatchEvent(queueMessage.getEventMessages(),queueMessage.getRawMessage());
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
