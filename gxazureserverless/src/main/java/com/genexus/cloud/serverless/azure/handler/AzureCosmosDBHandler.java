package com.genexus.cloud.serverless.azure.handler;
import com.genexus.cloud.serverless.Helper;
import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.CosmosDBTrigger;
import com.sun.jna.platform.win32.Guid;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AzureCosmosDBHandler extends AzureEventHandler{

	public AzureCosmosDBHandler() throws Exception {
		super();
	}
	public void run(
		@CosmosDBTrigger(name = "itemIn", databaseName = "%CosmosDB_Database_Name%", containerName = "%Container_Name%", leaseContainerName = "%lease_Container_Name%", connection = "%CosmosDB_Connection%") List<Map<String,Object>> items ,
		final ExecutionContext context) throws Exception {

		context.getLogger().info("GeneXus CosmosDB trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());

		Guid.GUID eventId = new Guid.GUID(context.getInvocationId());

		EventMessages msgs = new EventMessages();
		EventMessagesList eventMessagesList = new EventMessagesList();

		switch (executor.methodSignatureIdx) {
			case 0:
				msgs = setupEventMessages(eventId,items);
				break;
			case 4:
				eventMessagesList = setupEventMessagesList(items);
				break;
			default:
				break;
		}
		SetupServerlessMappings(context.getFunctionName());

		try {
			EventMessageResponse response = dispatchEvent(msgs, eventMessagesList, Helper.toJSONString(items));
			if (response.hasFailed()) {
				logger.error(String.format("Messages were not handled. Error: %s", response.getErrorMessage()));
				throw new RuntimeException(response.getErrorMessage()); //Throw the exception so the runtime can Retry the operation.
			}

		} catch (Exception e) {
			logger.error("HandleRequest execution error", e);
			throw e; 		//Throw the exception so the runtime can Retry the operation.
		}

	}
	private EventMessagesList setupEventMessagesList(List<Map<String,Object>> jsonList)
	{
		EventMessagesList messagesList = new EventMessagesList();
		for (Map<String, Object> json : jsonList) {
			messagesList.addItem(Helper.toJSONString(json));
		}
		return messagesList;
	}
	private EventMessages setupEventMessages(Guid.GUID eventId, List<Map<String,Object>> jsonList)
	{
		EventMessages msgs = new EventMessages();

		for (Map<String, Object> json : jsonList) {

			String idValue = "";
			EventMessage msg = new EventMessage();
			msg.setMessageDate(new Date());
			msg.setMessageSourceType(EventMessageSourceType.COSMOSDB);

			List<EventMessageProperty> msgAtts = msg.getMessageProperties();

			for (Map.Entry<String, Object> entry : json.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().toString();

				msgAtts.add(new EventMessageProperty(key, value));

				if (key.equals("id"))
					idValue = value;
			}
			String messageId = eventId.toString() + "_" + idValue;
			msg.setMessageId(messageId);
			msgs.add(msg);
		}
		return msgs;
	}
}
