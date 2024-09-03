package com.genexus.cloud.serverless.azure.handler;
import com.genexus.cloud.serverless.Helper;
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
		@BlobTrigger(name = "content", path = "%blob_path%", dataType = "binary") byte[] content,
		@BindingName("name") String name,
		final ExecutionContext context
	) throws Exception {

		context.getLogger().info("GeneXus Blob Storage trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());

		switch (executor.getMethodSignatureIdx()) {
			case 0:

				EventMessage msg = new EventMessage();
				msg.setMessageId(context.getInvocationId());
				msg.setMessageSourceType(EventMessageSourceType.BLOB);

				Instant nowUtc = Instant.now();
				msg.setMessageDate(Date.from(nowUtc));
				msg.setMessageData(Base64.getEncoder().encodeToString(content));

				List<EventMessageProperty> msgAtts = msg.getMessageProperties();

				msgAtts.add(new EventMessageProperty("Id", context.getInvocationId()));
				msgAtts.add(new EventMessageProperty("name", name));

				msgs.add(msg);
				break;
			case 1:
			case 2:
				rawMessage = Base64.getEncoder().encodeToString(content);
		}

		try {
			EventMessageResponse response = dispatchEvent(msgs, rawMessage);
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
