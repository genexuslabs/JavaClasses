package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.helpers.ServiceBusProcessedMessage;
import com.genexus.cloud.serverless.helpers.ServiceBusSingleMessageProcessor;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.Cardinality;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;

public class AzureServiceBusQueueSingleMsgHandler extends AzureEventHandler {

	public AzureServiceBusQueueSingleMsgHandler() throws Exception {
		super();
	}

	public void run(
		@ServiceBusQueueTrigger(name = "messages", queueName = "%queue_name%", connection = "%queue_connection%", cardinality = Cardinality.ONE)
		String message,
		@BindingName("MessageId") String messageId,
		@BindingName("EnqueuedTimeUtc") String enqueuedTimeUtc, // (ISO-8601)
		final ExecutionContext context
	) throws Exception {

		context.getLogger().info("GeneXus Service Bus Queue trigger single message process handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());
		ServiceBusSingleMessageProcessor queueSingleMessageProcessor = new ServiceBusSingleMessageProcessor();
		ServiceBusProcessedMessage queueMessage = queueSingleMessageProcessor.processQueueMessage(executor,messageId,enqueuedTimeUtc,context,message);
		ExecuteDynamic(queueMessage.getEventMessages(), queueMessage.getRawMessage());
	}
}
