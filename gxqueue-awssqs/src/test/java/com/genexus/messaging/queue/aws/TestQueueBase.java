package com.genexus.messaging.queue.aws;

import com.genexus.messaging.queue.IQueue;
import com.genexus.messaging.queue.SimpleMessageQueue;
import com.genexus.messaging.queue.model.DeleteMessageResult;
import com.genexus.messaging.queue.model.MessageQueueOptions;
import com.genexus.messaging.queue.model.SimpleQueueMessage;
import com.genexus.messaging.queue.model.SendMessageResult;
import com.genexus.services.ServiceConfigurationException;
import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public abstract class TestQueueBase {
	protected IQueue queue;

	public abstract String getProviderName();

	public abstract IQueue getQueue() throws ServiceConfigurationException;
	public abstract SimpleMessageQueue getQueueWrapper() throws ServiceConfigurationException;

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
		int retry = 60;
		while (queue.getQueueLength() != 0 && retry-- > 0) {
			sleep(500);
		}
		int queueLength = queue.getQueueLength();
		Assert.assertEquals(0, queueLength);
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
	public void sendMessageWithNoId() {
		SimpleQueueMessage msg = createMessage();
		msg.setMessageId("");
		SendMessageResult sendResult = queue.sendMessage(msg);
		Assert.assertNotNull(sendResult);
		Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
		Assert.assertNotEquals("", sendResult.getMessageId());
		Assert.assertNotEquals("", sendResult.getMessageServerId());
	}

	/*@Test
	public void sendMessageWithFactoryQueue() {
		try {
			SimpleMessageQueue simpleQueue = getQueueWrapper();
			simpleQueue.sendMessage(createMessage());

		} catch (ServiceConfigurationException e) {
			e.printStackTrace();
		}

		SendMessageResult sendResult = sendQueueMessage();
		Assert.assertNotNull(sendResult);
		Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
		Assert.assertNotEquals("", sendResult.getMessageId());
		Assert.assertNotEquals("", sendResult.getMessageServerId());
	}*/

	@Test
	public void sendMessageError() {
		List<SendMessageResult> sendResult = queue.sendMessages(new ArrayList<SimpleQueueMessage>(), new MessageQueueOptions());
		Assert.assertNotNull(sendResult);
		Assert.assertTrue(sendResult.size() == 0);
	}

	@Test
	public void receiveMessages() {
		SendMessageResult sendResult = sendQueueMessage();
		Assert.assertNotNull(sendResult);
		Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
		Assert.assertNotEquals("", sendResult.getMessageId());

		List<SimpleQueueMessage> msgs = queue.getMessages(new MessageQueueOptions());
		Assert.assertTrue(msgs.size() > 0);
		SimpleQueueMessage msg = msgs.get(0);
		Assert.assertTrue(msg.getMessageBody().length() > 0);
		Assert.assertTrue(msg.getMessageId().length() > 0);
		Assert.assertTrue(msg.getMessageHandleId().length() > 0);

	}

	@Test
	public void receiveMessagesMaxNumberOfMessages() {
		for (int i = 0; i < 20; i++) {
			SendMessageResult sendResult = sendQueueMessage();
			Assert.assertNotNull(sendResult);
			Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
			Assert.assertNotEquals("", sendResult.getMessageId());
		}
		sleep(1000);
		List<SimpleQueueMessage> msgs = queue.getMessages(new MessageQueueOptions(){{
			setMaxNumberOfMessages(2);
		}});
		Assert.assertTrue(msgs.size() <= 2);
		SimpleQueueMessage msg = msgs.get(0);
		Assert.assertTrue(msg.getMessageBody().length() > 0);
		Assert.assertTrue(msg.getMessageId().length() > 0);
		Assert.assertTrue(msg.getMessageHandleId().length() > 0);
		ensurePurged();
	}

	@Test
	public void receiveMultipleMessages() {
		for (int i = 0; i < 20; i++) {
			SendMessageResult sendResult = sendQueueMessage();
			Assert.assertNotNull(sendResult);
			Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
			Assert.assertNotEquals("", sendResult.getMessageId());
		}
		sleep(1000);
		List<SimpleQueueMessage> msgs = queue.getMessages(new MessageQueueOptions(){{
			setWaitTimeout(10);
			setMaxNumberOfMessages(10);
		}});
		Assert.assertTrue(msgs.size() >= 5); // fewer messages might be returned)
		SimpleQueueMessage msg = msgs.get(0);
		Assert.assertTrue(msg.getMessageBody().length() > 0);
		Assert.assertTrue(msg.getMessageId().length() > 0);
		Assert.assertTrue(msg.getMessageHandleId().length() > 0);
	}

	@Test
	public void receiveMessagesWithAtributes() {
		SendMessageResult sendResult = sendQueueMessage();
		Assert.assertNotNull(sendResult);
		Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
		Assert.assertNotEquals("", sendResult.getMessageId());

		List<SimpleQueueMessage> msgs = queue.getMessages(new MessageQueueOptions());
		Assert.assertTrue(msgs.size() > 0);
		SimpleQueueMessage msg = msgs.get(0);
		Assert.assertTrue(msg.getMessageBody().length() > 0);
		Assert.assertTrue(msg.getMessageId().length() > 0);
		Assert.assertTrue(msg.getMessageHandleId().length() > 0);
		Assert.assertTrue(msg.getMessageAttributes().count() > 1);
		Assert.assertTrue(msg.getMessageAttributes().count() < 5);

		MessageQueueOptions opts = new MessageQueueOptions() {{
			setReceiveMessageAttributes(true);
			setMaxNumberOfMessages(5);
			setDelaySeconds(2);
			setTimetoLive(2);
			setWaitTimeout(3);
		}};

		sendQueueMessage();

		msgs = queue.getMessages(opts);
		Assert.assertTrue(msgs.size() > 0);
		msg = msgs.get(0);
		Assert.assertTrue(msg.getMessageBody().length() > 0);
		Assert.assertTrue(msg.getMessageId().length() > 0);
		Assert.assertTrue(msg.getMessageHandleId().length() > 0);
		Assert.assertTrue(msg.getMessageAttributes().count() > 6);
	}

	@Test
	public void deleteMessage() {
		SendMessageResult sendResult = sendQueueMessage();
		Assert.assertNotNull(sendResult);
		Assert.assertEquals(SendMessageResult.SENT, sendResult.getMessageSentStatus());
		Assert.assertNotEquals("", sendResult.getMessageId());
		Assert.assertNotEquals("", sendResult.getMessageServerId());

		sleep(5000);

		List<SimpleQueueMessage> msgs = queue.getMessages(new MessageQueueOptions());
		Assert.assertTrue(msgs.size() > 0);
		DeleteMessageResult deleteMessageResult = queue.deleteMessage(msgs.get(0).getMessageHandleId());
		Assert.assertNotNull(deleteMessageResult);
		Assert.assertTrue(deleteMessageResult.getMessageId().length() > 0);
		Assert.assertEquals(DeleteMessageResult.DELETED, deleteMessageResult.getMessageDeleteStatus());

	}

	private SendMessageResult sendQueueMessage() {
		SimpleQueueMessage msg = createMessage();
		return queue.sendMessage(msg);
	}

	private SimpleQueueMessage createMessage() {
		SimpleQueueMessage msg = new SimpleQueueMessage() {{
			setMessageId("gx_" + java.util.UUID.randomUUID().toString());
			setMessageBody("messageBody test");
			getMessageAttributes().set("att1", "test1");
			getMessageAttributes().set("att2", "test2");
			getMessageAttributes().set("att3", "test3");
			getMessageAttributes().set("att4", "test4");
			getMessageAttributes().set("att5", "test5");
		}};
		return msg;
	}
}
