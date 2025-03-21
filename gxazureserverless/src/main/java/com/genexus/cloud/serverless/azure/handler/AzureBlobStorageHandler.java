package com.genexus.cloud.serverless.azure.handler;

import com.genexus.cloud.serverless.model.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.ExecutionContext;

import java.time.Instant;
import java.util.List;
import java.util.*;

public class AzureBlobStorageHandler extends AzureEventHandler{

	EventMessages msgs = new EventMessages();
	String rawMessage ="";

	public AzureBlobStorageHandler() throws Exception {
		super();
	}
	public void run(
		@BlobTrigger(name = "content", source = "EventGrid", path = "test-triggerinput-java/{name}", dataType = "binary") byte[] content,
		@BindingName("name") String name,
		final ExecutionContext context
	) throws Exception {

		context.getLogger().info("GeneXus Blob Storage trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());
		String functionName = context.getFunctionName().trim().toUpperCase();
		String storageEnvVar = String.format("GX_AZURE_%s_BLOB_STORAGE", functionName);
		String containerEnvVar = String.format("GX_AZURE_%s_BLOB_STORAGE_CONTAINER", functionName);
		String storageAccount = System.getenv(storageEnvVar);
		String containerName = System.getenv(containerEnvVar);
		String blobUri;
		if (storageAccount != null && !storageAccount.isEmpty() && containerName != null && !containerName.isEmpty())
			blobUri = String.format("https://%s.blob.core.windows.net/%s/%s", storageAccount, containerName, "name");
		else {
			blobUri = "name";
			context.getLogger().warning(String.format("Could not return complete URI. Please configure GX_AZURE_%s_BLOB_STORAGE and GX_AZURE_%s_BLOB_STORAGE_CONTAINER app settings.",functionName,functionName));
		}
		switch (executor.getMethodSignatureIdx()) {
			case 0:
				EventMessage msg = new EventMessage();
				msg.setMessageId(context.getInvocationId());
				msg.setMessageSourceType(EventMessageSourceType.BLOB);
				Instant nowUtc = Instant.now();
				msg.setMessageDate(Date.from(nowUtc));
				List<EventMessageProperty> msgAtts = msg.getMessageProperties();
				msg.setMessageData(blobUri);
				msgAtts.add(new EventMessageProperty("Id", context.getInvocationId()));
				msgAtts.add(new EventMessageProperty("name", name));
				msgs.add(msg);
				break;
			case 1:
			case 2:
				rawMessage = blobUri;
		}
		ExecuteDynamic(msgs,rawMessage);
	}
}

