package com.genexus.cloud.serverless.azure.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessageProperty;
import com.genexus.cloud.serverless.model.EventMessages;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import io.cloudevents.CloudEvent;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AzureEventGridCloudHandler extends AzureEventHandler{
	EventMessages msgs = new EventMessages();
	String rawMessage = "";

	public AzureEventGridCloudHandler() throws Exception {super();}

	public void run(
		@EventGridTrigger(name = "eventgridEvent") String eventJson,
		final ExecutionContext context) throws Exception {
		context.getLogger().info("GeneXus Event Grid CloudEvents trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());
		setupEventGridMessage(eventJson, context);
		ExecuteDynamic(msgs, rawMessage);
	}

	protected void setupEventGridMessage(String eventJson, ExecutionContext context) throws JsonProcessingException {
		switch (executor.getMethodSignatureIdx()) {
			case 0:
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.registerModule(io.cloudevents.jackson.JsonFormat.getCloudEventJacksonModule());
					CloudEvent cloudEvent = objectMapper.readValue(eventJson, CloudEvent.class);
					EventMessage msg = new EventMessage();
					msg.setMessageId(cloudEvent.getId());
					msg.setMessageSourceType(cloudEvent.getType());
					msg.setMessageVersion("");
					msg.setMessageDate(new Date());
					msg.setMessageData(Objects.requireNonNull(cloudEvent.getData()).toString());

					List<EventMessageProperty> msgAtts = msg.getMessageProperties();
					msgAtts.add(new EventMessageProperty("Id", cloudEvent.getId()));
					msgAtts.add(new EventMessageProperty("Subject", cloudEvent.getSubject()));
					msgAtts.add(new EventMessageProperty("DataContentType", cloudEvent.getDataContentType()));
					if (cloudEvent.getDataSchema() != null)
						msgAtts.add(new EventMessageProperty("DataSchema", cloudEvent.getDataSchema().toString()));
					if (cloudEvent.getSource() != null)
						msgAtts.add(new EventMessageProperty("Source", cloudEvent.getSource().toString()));
					if (cloudEvent.getSpecVersion() != null)
						msgAtts.add(new EventMessageProperty("SpecVersion", cloudEvent.getSpecVersion().toString()));
					if (cloudEvent.getTime() != null)
						msgAtts.add(new EventMessageProperty("Time", cloudEvent.getTime().toString()));
					msgs.add(msg);
				}
				catch (Exception e) {
					context.getLogger().severe(String.format("HandleRequest execution error: %s",e.getMessage()));
					throw e;}
				break;
			case 1:
			case 2:
				rawMessage = eventJson;
		}
	}
}
