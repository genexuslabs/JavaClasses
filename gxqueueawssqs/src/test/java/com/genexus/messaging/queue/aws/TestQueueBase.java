package com.genexus.messaging.queue.aws;

import com.genexus.messaging.queue.IQueue;
import com.genexus.messaging.queue.model.MessageQueueOptions;
import com.genexus.messaging.queue.model.SimpleQueueMessage;
import com.genexus.messaging.queue.model.SendMessageResult;
import com.genexus.services.ServiceConfigurationException;
import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public abstract class TestQueueBase {

	protected IQueue queue;

	//AKIAT2GWL4JGR56VMWGX
	//PYlgo9ysgr9G2srypFoKkRycjuPqEFB4uJWjgcNW
	//https://sqs.us-east-1.amazonaws.com/262442574413/gx-sqs-githubactions-test
	public abstract String getProviderName();

	public abstract IQueue getQueue() throws ServiceConfigurationException;

	@Before
	public void beforeEachTestMethod() {
		Connect.init();
		boolean testEnabled = false;
		try {
			testEnabled = System.getenv(getProviderName() + "_TEST_ENABLED") != null;
		} catch (Exception e) {

		}

		assumeTrue(testEnabled);

		try {
			queue = getQueue();
		} catch (ServiceConfigurationException e) {
			e.printStackTrace();
		}
		assertTrue(queue != null);
	}

	@Test
	public void purgue() {
		boolean purged = ensurePurged();
		Assert.assertTrue("Queue was not purged", purged);
	}

	private boolean ensurePurged() {
		boolean purged = queue.purge();
		int retry = 30;
		int sleepMs = 1000 * 5;
		while (!purged && retry > 0) {
			sleep(sleepMs);
			purged = queue.purge() || queue.getQueueLength() == 0;
			retry--;
		}
		return purged;
	}

	private void sleep(int sleepMs) {
		try {
			Thread.sleep(sleepMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getQueueLength() {
		boolean purged = ensurePurged();
		Assert.assertTrue("Queue was not purged", purged);
		int qLength = queue.getQueueLength();
		Assert.assertEquals(0, qLength);
	}

	@Test
	public void getQueueLengthNotEmpty() {
		boolean purged = ensurePurged();
		Assert.assertTrue("Queue was not purged", purged);

		int qLength = queue.getQueueLength();
		Assert.assertEquals(0, qLength);
		sendQueueMessage();
		sleep(5000);
		qLength = queue.getQueueLength();
		Assert.assertEquals(1, qLength);
	}

	@Test
	public void sendMessage() {
		SendMessageResult sendResult = sendQueueMessage();
		Assert.assertNotNull(sendResult);
		Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
		Assert.assertNotEquals("", sendResult.getMessageId());
		Assert.assertNotEquals("", sendResult.getMessageServerId());
	}

	@Test
	public void receiveMessages() {
		SendMessageResult sendResult = sendQueueMessage();
		Assert.assertNotNull(sendResult);
		Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
		Assert.assertNotEquals("", sendResult.getMessageId());

		List<SimpleQueueMessage> msgs = queue.getMessages(new MessageQueueOptions());
		Assert.assertNotEquals(0, msgs.size());

	}

	private SendMessageResult sendQueueMessage() {
		SimpleQueueMessage msg = new SimpleQueueMessage() {{
			setMessageId(java.util.UUID.randomUUID().toString());
			setMessageBody("messageBody test");
			getMessageAttributes().set("att1", "test1");
			getMessageAttributes().set("att2", "test2");
		}};
		return queue.sendMessage(msg);
	}
}
