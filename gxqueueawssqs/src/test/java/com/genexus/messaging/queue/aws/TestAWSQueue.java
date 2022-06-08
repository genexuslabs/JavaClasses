package com.genexus.messaging.queue.aws;

import com.genexus.messaging.queue.IQueue;
import com.genexus.services.ServiceConfigurationException;

public class TestAWSQueue extends  TestQueueBase {

	@Override
	public String getProviderName() {
		return "QUEUE_AWSSQS";
	}

	@Override
	public IQueue getQueue() throws ServiceConfigurationException {
		return new AWSQueue();
	}
}
