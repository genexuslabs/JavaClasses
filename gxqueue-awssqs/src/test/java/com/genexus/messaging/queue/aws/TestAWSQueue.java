package com.genexus.messaging.queue.aws;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.messaging.queue.IQueue;
import com.genexus.messaging.queue.SimpleMessageQueue;
import com.genexus.services.ServiceConfigurationException;
import com.genexus.services.ServiceSettingsReader;
import com.genexus.util.GXService;
import com.genexusmessaging.awscore.SdtAWSBasicCredentials;

public class TestAWSQueue extends TestQueueBase {

	public static String ACCESS_KEY = "ACCESS_KEY";
	public static String SECRET_ACCESS_KEY = "SECRET_KEY";
	public static String REGION = "REGION";
	public static String QUEUE_URL = "QUEUE_URL";


	@Override
	public String getProviderName() {
		return AWSQueue.Name;
	}

	@Override
	public IQueue getQueue() throws ServiceConfigurationException {

		ServiceSettingsReader serviceSettings = new ServiceSettingsReader("QUEUE", AWSQueue.Name, new GXService());
		String queueURL = serviceSettings.getEncryptedPropertyValue(QUEUE_URL, "");

		AWSBasicCredentials credentials = new AWSBasicCredentials() {{
			setAccessKeyId(serviceSettings.getEncryptedPropertyValue(ACCESS_KEY, ""));
			setSecretKey(serviceSettings.getEncryptedPropertyValue(SECRET_ACCESS_KEY, ""));
			setRegion(serviceSettings.getEncryptedPropertyValue(REGION, ""));
		}};

		return new AWSQueue(credentials, queueURL);
	}

	@Override
	public SimpleMessageQueue getQueueWrapper() throws ServiceConfigurationException {

		ServiceSettingsReader serviceSettings = new ServiceSettingsReader("QUEUE", AWSQueue.Name, new GXService());
		String queueURL = serviceSettings.getEncryptedPropertyValue(QUEUE_URL, "");

		SdtAWSBasicCredentials credentials = new SdtAWSBasicCredentials();
		credentials.setgxTv_SdtAWSBasicCredentials_Accesskey(serviceSettings.getEncryptedPropertyValue(ACCESS_KEY, ""));
		credentials.setgxTv_SdtAWSBasicCredentials_Secretkey(serviceSettings.getEncryptedPropertyValue(SECRET_ACCESS_KEY, ""));
		credentials.setgxTv_SdtAWSBasicCredentials_Region(serviceSettings.getEncryptedPropertyValue(REGION, ""));

		GXBaseCollection<SdtMessages_Message>[] errArray = new GXBaseCollection[1];
		boolean[] success = new boolean[1];

		return new AWSQueueFactory().connect(credentials, queueURL, errArray, success);

	}
}
