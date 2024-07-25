package com.genexus.cloud.serverless.azure.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessageProperty;
import com.genexus.cloud.serverless.model.EventMessageResponse;
import com.genexus.cloud.serverless.model.EventMessages;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import io.cloudevents.CloudEvent;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AzureEventGridCloudHandler extends AzureEventHandler{

	public AzureEventGridCloudHandler() throws Exception {super();}

	public void run(
		@EventGridTrigger(name = "eventgridEvent") String eventJson,
		final ExecutionContext context) throws Exception {

		context.getLogger().info("GeneXus Event Grid CloudEvents trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		SetupServerlessMappings(context.getFunctionName());

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(io.cloudevents.jackson.JsonFormat.getCloudEventJacksonModule());
			CloudEvent cloudEvent = objectMapper.readValue(eventJson, CloudEvent.class);

			EventMessages msgs = new EventMessages();
			EventMessage msg = new EventMessage();
			msg.setMessageId(cloudEvent.getId());
			msg.setMessageSourceType(cloudEvent.getType());
			msg.setMessageVersion("");

			msg.setMessageDate(new Date());
			msg.setMessageData(Objects.requireNonNull(cloudEvent.getData()).toString());

			List<EventMessageProperty> msgAtts = msg.getMessageProperties();

			msgAtts.add(new EventMessageProperty("Id", cloudEvent.getId()));
			msgAtts.add(new EventMessageProperty("Subject",cloudEvent.getSubject()));
			msgAtts.add(new EventMessageProperty("DataContentType",cloudEvent.getDataContentType()));
			msgAtts.add(new EventMessageProperty("DataSchema", Objects.requireNonNull(cloudEvent.getDataSchema()).toString()));
			msgAtts.add(new EventMessageProperty("Source", cloudEvent.getSource().toString()));
			msgAtts.add(new EventMessageProperty("SpecVersion", cloudEvent.getSpecVersion().toString()));
			msgAtts.add(new EventMessageProperty("Time", Objects.requireNonNull(cloudEvent.getTime()).toString()));

			msgs.add(msg);

			EventMessageResponse response = dispatchEvent(msgs, eventJson);
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
