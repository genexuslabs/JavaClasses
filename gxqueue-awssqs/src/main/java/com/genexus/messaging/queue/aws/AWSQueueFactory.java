package com.genexus.messaging.queue.aws;

import com.genexus.GXBaseCollection;
import com.genexus.GxUserType;
import com.genexus.SdtMessages_Message;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.messaging.queue.IQueue;
import com.genexus.messaging.queue.SimpleMessageQueue;
import com.genexus.services.ServiceConfigurationException;
import com.genexusmessaging.awscore.SdtAWSBasicCredentials;


public class AWSQueueFactory {
	private static ILogger logger = LogManager.getLogger(AWSQueueFactory.class);

	public SimpleMessageQueue connect(GxUserType awsBasicCredentials, String queueURL, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errMessages = errorMessagesArr[0];
		AWSBasicCredentials credentials = new AWSBasicCredentials() {{
			setAccessKeyId(((SdtAWSBasicCredentials) awsBasicCredentials).getgxTv_SdtAWSBasicCredentials_Accesskey());
			setSecretKey(((SdtAWSBasicCredentials) awsBasicCredentials).getgxTv_SdtAWSBasicCredentials_Secretkey());
			setRegion(((SdtAWSBasicCredentials) awsBasicCredentials).getgxTv_SdtAWSBasicCredentials_Region());
		}};

		try {
			IQueue queueImpl = new AWSQueue(credentials, queueURL);
			errMessages.clear();
			success[0] = queueURL.length() > 0;
			return new SimpleMessageQueue(queueImpl);

		} catch (ServiceConfigurationException e) {
			handleConnectionError(errMessages, e);
		}
		return null;
	}

	public SimpleMessageQueue connect(String queueURL, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errMessages = errorMessagesArr[0];

		try {
			IQueue queueImpl = new AWSQueue(queueURL);
			errMessages.clear();
			success[0] = queueURL.length() > 0;
			return new SimpleMessageQueue(queueImpl);

		} catch (ServiceConfigurationException e) {
			handleConnectionError(errMessages, e);
		}
		return null;
	}

	private static void handleConnectionError(GXBaseCollection<SdtMessages_Message> errMessages, ServiceConfigurationException e) {
		logger.error("Failed to connect to AWS SQS Queue", e);
		SdtMessages_Message msg = new SdtMessages_Message();
		msg.setgxTv_SdtMessages_Message_Description(e.getMessage());
		msg.setgxTv_SdtMessages_Message_Type((byte) 1);
		errMessages.add(msg);
	}

}
