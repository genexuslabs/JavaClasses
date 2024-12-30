package com.genexus.cloud.serverless.azure.handler;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.genexus.cloud.serverless.Helper;
import com.genexus.cloud.serverless.helpers.ServiceBusMessagesSetup;
import com.genexus.cloud.serverless.model.EventMessageResponse;
import com.genexus.cloud.serverless.model.EventMessages;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.Cardinality;
import com.microsoft.azure.functions.annotation.ServiceBusTopicTrigger;

import java.util.ArrayList;
import java.util.List;

public class AzureServiceBusTopicSingleMsgHandler extends AzureEventHandler {

	EventMessages msgs = new EventMessages();
	String rawMessage = "";

	public AzureServiceBusTopicSingleMsgHandler() throws Exception {
		super();
	}

	public void run(
		@ServiceBusTopicTrigger(name = "messages", topicName = "%topic_name%", subscriptionName = "%subscriptionName%", connection = "%queue_connection%", cardinality = Cardinality.ONE)
		ServiceBusReceivedMessage message,
		final ExecutionContext context
	) throws Exception {

		context.getLogger().info("GeneXus Service Bus Topic trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());

		setupServerlessMappings(context.getFunctionName());
		List<ServiceBusReceivedMessage> messages = new ArrayList<>();
		messages.add(message);
		try {
			EventMessageResponse response = dispatchEvent(msgs,rawMessage);
			if (response.hasFailed()) {
				logger.error(String.format("Messages were not handled. Error: %s", response.getErrorMessage()));
				throw new RuntimeException(response.getErrorMessage()); //Throw the exception so the runtime can Retry the operation.
			}
		} catch (Exception e) {
			logger.error("HandleRequest execution error", e);
			throw e; 		//Throw the exception so the runtime can Retry the operation.
		}
	}

	protected void setupServiceBusMessages(List<ServiceBusReceivedMessage> messages) {
		switch (executor.getMethodSignatureIdx()) {
			case 0:
				msgs = ServiceBusMessagesSetup.setupservicebuslistmsgs(messages);
				break;
			case 1:
			case 2:
				rawMessage = Helper.toJSONString(messages);
		}
	}
}
