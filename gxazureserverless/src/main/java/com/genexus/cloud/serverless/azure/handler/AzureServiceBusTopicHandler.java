package com.genexus.cloud.serverless.azure.handler;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.genexus.cloud.serverless.Helper;
import com.genexus.cloud.serverless.helpers.BuildServiceBusMessages;
import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.*;

import java.util.List;

public class AzureServiceBusTopicHandler extends AzureEventHandler {
	public AzureServiceBusTopicHandler() throws Exception {
		super();
	}

	public void run(
		@ServiceBusTopicTrigger(name = "messages", topicName = "%topic_name%", subscriptionName = "%subscriptionName%", connection = "%queue_connection%", cardinality = Cardinality.MANY)
		List<ServiceBusReceivedMessage> messages,
		final ExecutionContext context
	) throws Exception {

		context.getLogger().info("GeneXus Service Bus Topic trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());

		SetupServerlessMappings(context.getFunctionName());

		try {
			EventMessageResponse response = dispatchEvent(BuildServiceBusMessages.setupServiceBusMessages(messages), Helper.toJSONString(messages));
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
