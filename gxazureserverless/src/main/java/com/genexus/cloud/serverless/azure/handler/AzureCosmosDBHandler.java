package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.JSONHelper;
import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.CosmosDBTrigger;

import java.util.*;

public class AzureCosmosDBHandler extends AzureEventHandler{

	public AzureCosmosDBHandler() throws Exception {
		super();
	}
	public void run(
		@CosmosDBTrigger(name = "itemIn", databaseName = "%CosmosDB_Database_Name%", containerName = "%Container_Name%", leaseContainerName = "%lease_Container_Name%", connection = "connection") List<Object> items ,
		final ExecutionContext context) throws Exception {

		context.getLogger().info("GeneXus CosmosDB trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		UUID eventId = UUID.randomUUID();
		EventMessages msgs = new EventMessages();
		EventMessagesList eventMessagesList = new EventMessagesList();
		String rawMessage= "";
		setupServerlessMappings(context.getFunctionName());
		switch (executor.getMethodSignatureIdx()) {
			case 0:
				msgs = setupEventMessages(eventId,TryGetDocuments(items, context));
				break;
			case 4:
				eventMessagesList = setupEventMessagesList(TryGetDocuments(items, context));
				break;
			default:
				rawMessage = JSONHelper.toJSONString(items);
				break;
		}
		ExecuteDynamic(msgs,eventMessagesList,rawMessage);
	}

	private List<Map<String,Object>> TryGetDocuments(List<Object> items, ExecutionContext context){
		List<Map<String,Object>> documents = new ArrayList<>();
		if (items != null && !items.isEmpty()) {
			for (Object item : items) {
				if (item instanceof Map) {
					try {
						Map<String, Object> document = (Map<String, Object>) item;
						documents.add(document);
					}
					catch (Exception ex)
					{
						context.getLogger().severe(String.format("Messages were not handled. Error trying to read Cosmos DB data. %s", ex.toString()));
						throw new RuntimeException(String.format("Error trying to read Cosmos DB data. %s", ex.toString())); //Throw the exception so the runtime can Retry the operation.
					}
				}
			}
			return documents;
		}
		return null;
	}

	private EventMessagesList setupEventMessagesList(List<Map<String,Object>> jsonList)
	{
		EventMessagesList messagesList = new EventMessagesList();
		for (Map<String, Object> json : jsonList) {
			messagesList.addItem(JSONHelper.toJSONString(json));
		}
		return messagesList;
	}

	private EventMessages setupEventMessages(UUID eventId, List<Map<String,Object>> jsonList)
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
