package com.genexus.messaging.queue;

import com.genexus.messaging.queue.model.DeleteMessageResult;
import com.genexus.messaging.queue.model.MessageQueueOptions;
import com.genexus.messaging.queue.model.SendMessageResult;
import com.genexus.messaging.queue.model.SimpleQueueMessage;

import java.util.List;

public interface IQueue {
	Integer getQueueLength();
	SendMessageResult sendMessage(SimpleQueueMessage simpleQueueMessage);
	SendMessageResult sendMessage(SimpleQueueMessage simpleQueueMessage, MessageQueueOptions messageQueueOptions);
	List<SendMessageResult> sendMessages(List<SimpleQueueMessage> simpleQueueMessages, MessageQueueOptions messageQueueOptions);
	List<SimpleQueueMessage> getMessages(MessageQueueOptions messageQueueOptions);
	DeleteMessageResult deleteMessage(String messageHandleId);

	List<DeleteMessageResult> deleteMessages(List<String> messageHandleId);
	boolean purge();
}
