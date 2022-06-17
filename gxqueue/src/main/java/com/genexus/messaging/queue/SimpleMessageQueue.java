package com.genexus.messaging.queue;

import com.genexus.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.messaging.queue.model.DeleteMessageResult;
import com.genexus.messaging.queue.model.MessageQueueOptions;
import com.genexus.messaging.queue.model.SendMessageResult;
import com.genexus.messaging.queue.model.SimpleQueueMessage;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessage;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageOptions;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageResult;

import java.util.ArrayList;
import java.util.List;

public class SimpleMessageQueue {
	private IQueue queue;
	private static ILogger logger = LogManager.getLogger(SimpleMessageQueue.class);

	public SimpleMessageQueue() {

	}

	public SimpleMessageQueue(IQueue queueProvider) {
		queue = queueProvider;
	}

	void validQueue() throws Exception {
		if (queue == null) {
			logger.error("Queue was not instantiated.");
			throw new Exception("Queue was not instantiated.");
		}
	}

	public void clear(GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errorMessages = errorMessagesArr[0];
		errorMessages.clear();
		try {
			validQueue();
			success[0] = queue.purge();
		} catch (Exception ex) {
			queueErrorMessagesSetup(ex, errorMessages);
			logger.error("Could not clear queue", ex);
		}
		success[0] = false;
	}

	public short getQueueLength(GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errorMessages = errorMessagesArr[0];
		errorMessages.clear();
		short queueLength = 0;
		try {
			validQueue();
			queueLength = queue.getQueueLength().shortValue();
		} catch (Exception ex) {
			queueErrorMessagesSetup(ex, errorMessages);
			logger.error("Could not getQueueLength", ex);
		}
		return queueLength;
	}

	public SdtMessageResult sendMessage(SdtMessage sdtMessage, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessage> messages = new GXBaseCollection<>();
		messages.add(sdtMessage);
		GXBaseCollection<SdtMessageResult> result = sendMessages(messages, new SdtMessageOptions(), errorMessagesArr, success);
		return ((result.size() == 1) ? result.item(1) : Convert.toSdtMessageResult(new SendMessageResult()));
	}

	public GXBaseCollection<SdtMessageResult> sendMessages(GXBaseCollection<SdtMessage> sdtMessages, SdtMessageOptions msgOptions, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errorMessages = errorMessagesArr[0];
		List<SimpleQueueMessage> msgList = new ArrayList<>();
		GXBaseCollection<SdtMessageResult> listReturn = new GXBaseCollection<>();
		errorMessages.clear();

		for (SdtMessage m : sdtMessages) {
			msgList.add(Convert.toSimpleQueueMessage(m));
		}

		try {
			validQueue();
			List<SendMessageResult> sendMessageResult = queue.sendMessages(msgList, Convert.toMessageQueueOptions(msgOptions));
			success[0] = true;
			for (SendMessageResult msgResult : sendMessageResult) {
				listReturn.add(Convert.toSdtMessageResult(msgResult));
			}
		} catch (Exception ex) {
			queueErrorMessagesSetup(ex, errorMessages);
			logger.error("Could not send queue message", ex);
		}

		return listReturn;
	}

	public GXBaseCollection<SdtMessage> getMessages(GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		return getMessages(null, errorMessagesArr, success);
	}

	public GXBaseCollection<SdtMessage> getMessages(SdtMessageOptions receiveOptions, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errorMessages = errorMessagesArr[0];

		MessageQueueOptions mqOptions = (receiveOptions != null) ? Convert.toMessageQueueOptions(receiveOptions) : new MessageQueueOptions();
		List<SimpleQueueMessage> receivedMessages = queue.getMessages(mqOptions);
		GXBaseCollection<SdtMessage> receivedMessagesResult = new GXBaseCollection<>();
		try {
			validQueue();
			for (SimpleQueueMessage m : receivedMessages) {
				receivedMessagesResult.add(Convert.toSdtMessage(m));
			}
			success[0] = true;
		} catch (Exception ex) {
			queueErrorMessagesSetup(ex, errorMessages);
			logger.error(String.format("Could not get Messages from Queue"), ex);
		}
		return receivedMessagesResult;
	}

	public SdtMessageResult deleteMessage(String messageHandleId, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errorMessages = errorMessagesArr[0];
		errorMessages.clear();
		SdtMessageResult sdtDelete = new SdtMessageResult();
		try {
			validQueue();
			DeleteMessageResult deletedMessage = queue.deleteMessage(messageHandleId);
			sdtDelete.setgxTv_SdtMessageResult_Messageid(deletedMessage.getMessageId());
			sdtDelete.setgxTv_SdtMessageResult_Servermessageid(deletedMessage.getMessageServerId());
			sdtDelete.setgxTv_SdtMessageResult_Messagestatus(deletedMessage.getMessageDeleteStatus());
			success[0] = true;
		} catch (Exception ex) {
			queueErrorMessagesSetup(ex, errorMessages);
			logger.error(String.format("Could not delete Message '%s' from Queue ", messageHandleId), ex);
		}
		return sdtDelete;
	}

	public GXBaseCollection<SdtMessageResult> deleteMessages(GXSimpleCollection<String> msgHandlesToDelete, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errorMessages = errorMessagesArr[0];
		errorMessages.clear();

		try {
			validQueue();
			List<String> handles = new ArrayList<>();
			for (String hnd : msgHandlesToDelete) {
				handles.add(hnd);
			}
			List<DeleteMessageResult> deletedMessage = queue.deleteMessages(handles);
			success[0] = true;
			return Convert.toDeleteExternalMessageResultList(deletedMessage);
		} catch (Exception ex) {
			queueErrorMessagesSetup(ex, errorMessages);
			logger.error(String.format("Could not delete Messages from Queue "), ex);
		}
		return new GXBaseCollection<SdtMessageResult>();
	}

	protected void queueErrorMessagesSetup(Exception ex, GXBaseCollection<SdtMessages_Message> messages) {
		if (messages != null && ex != null) {
			StructSdtMessages_Message struct = new StructSdtMessages_Message();
			struct.setType((byte) 1);
			struct.setDescription(ex.getMessage());
		}
	}

}
