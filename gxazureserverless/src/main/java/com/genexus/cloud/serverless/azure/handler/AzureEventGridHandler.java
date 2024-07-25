package com.genexus.cloud.serverless.azure.handler;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.genexus.cloud.serverless.Helper;
import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;

import java.util.Date;
import java.util.List;

public class AzureEventGridHandler  extends AzureEventHandler {
	public AzureEventGridHandler() throws Exception {
	}

	public void run(
		@EventGridTrigger(name = "eventgridEvent") EventGridEvent event,
		final ExecutionContext context) throws Exception {

		context.getLogger().info("GeneXus Event Grid trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());

		EventMessages msgs = new EventMessages();
		EventMessage msg = new EventMessage();
		msg.setMessageId(event.getId());
		msg.setMessageSourceType(event.getEventType());
		msg.setMessageVersion(event.getDataVersion());

		msg.setMessageDate(new Date());
		msg.setMessageData(event.getData().toString());

		List<EventMessageProperty> msgAtts = msg.getMessageProperties();

		msgAtts.add(new EventMessageProperty("Id", event.getId()));

		msgAtts.add(new EventMessageProperty("Subject",event.getSubject()));
		msgAtts.add(new EventMessageProperty("Topic",event.getTopic()));
		msgAtts.add(new EventMessageProperty("EventTime",event.getEventTime().toString()));

		msgs.add(msg);

		SetupServerlessMappings(context.getFunctionName());

		try {
			EventMessageResponse response = dispatchEvent(msgs, Helper.toJSONString(event));
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