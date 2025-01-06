package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.helpers.ServiceBusProcessedMessage;
import com.genexus.cloud.serverless.helpers.ServiceBusSingleMessageProcessor;
import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.Cardinality;
import com.microsoft.azure.functions.annotation.ServiceBusTopicTrigger;

public class AzureServiceBusTopicSingleMsgHandler extends AzureEventHandler {

	public AzureServiceBusTopicSingleMsgHandler() throws Exception {
		super();
	}

	public void run(
		@ServiceBusTopicTrigger(name = "messages", topicName = "%topic_name%", subscriptionName = "%subscriptionName%", connection = "%queue_connection%", cardinality = Cardinality.ONE)
		String message,
		@BindingName("MessageId") String messageId,
		@BindingName("EnqueuedTimeUtc") String enqueuedTimeUtc, // (ISO-8601)
		final ExecutionContext context
	) throws Exception {

		context.getLogger().info("GeneXus Service Bus Topic trigger single message handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());
		ServiceBusSingleMessageProcessor queueSingleMessageProcessor = new ServiceBusSingleMessageProcessor();
		ServiceBusProcessedMessage queueMessage = queueSingleMessageProcessor.processQueueMessage(executor,messageId,enqueuedTimeUtc,context,message);
		try {
			EventMessageResponse response = dispatchEvent(queueMessage.getEventMessages(),queueMessage.getRawMessage());
			if (response.hasFailed()) {
				logger.error(String.format("Messages were not handled. Error: %s", response.getErrorMessage()));
				throw new RuntimeException(response.getErrorMessage()); //Throw the exception so the runtime can Retry the operation.
			}
		} catch (Exception e) {
			logger.error("HandleRequest execution error", e);
			throw e;        //Throw the exception so the runtime can Retry the operation.
		}
	}
}

