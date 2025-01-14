package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.helpers.ServiceBusBatchMessageProcessor;
import com.genexus.cloud.serverless.helpers.ServiceBusProcessedMessage;
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
		ExecuteDynamic(queueMessage.getEventMessages(),queueMessage.getRawMessage());
	}
}
