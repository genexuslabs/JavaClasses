package com.genexus.messaging.queue.azure;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.messaging.queue.SimpleMessageQueue;
import com.genexus.services.ServiceConfigurationException;
import org.apache.commons.lang.NotImplementedException;


public class AzureQueueFactory {
	private static ILogger logger = LogManager.getLogger(AzureQueueFactory.class);


	public SimpleMessageQueue connect(String queueName, String queueUrl, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		throw new NotImplementedException();
	}

	private static void handleConnectionError(GXBaseCollection<SdtMessages_Message> errMessages, ServiceConfigurationException e) {
		logger.error("Failed to connect to AWS SQS Queue", e);
		SdtMessages_Message msg = new SdtMessages_Message();
		msg.setgxTv_SdtMessages_Message_Description(e.getMessage());
		msg.setgxTv_SdtMessages_Message_Type((byte) 1);
		errMessages.add(msg);
	}

}
