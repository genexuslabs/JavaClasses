package com.genexus.cloud.serverless.azure.handler;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.genexus.cloud.serverless.JSONHelper;
import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;

import java.util.Date;
import java.util.List;

public class AzureEventGridHandler  extends AzureEventHandler {

	EventMessages msgs = new EventMessages();
	String rawMessage = "";
	public AzureEventGridHandler() throws Exception {
	}

	public void run(
		@EventGridTrigger(name = "eventgridEvent") EventGridEvent event,
		final ExecutionContext context) throws Exception {
		context.getLogger().info("GeneXus Event Grid trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());
		setupEventGridMessage(event);
		ExecuteDynamic(msgs, rawMessage);
	}

	protected void setupEventGridMessage(EventGridEvent event) {
		switch (executor.getMethodSignatureIdx()) {
			case 0:
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
				if (event.getEventTime()!= null)
					msgAtts.add(new EventMessageProperty("EventTime",event.getEventTime().toString()));
				msgs.add(msg);
				break;
			case 1:
			case 2:
				rawMessage = JSONHelper.toJSONString(event);
		}
	}
}