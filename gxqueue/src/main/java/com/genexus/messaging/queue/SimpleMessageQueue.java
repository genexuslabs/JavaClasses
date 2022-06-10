package com.genexus.messaging.queue;


import com.genexus.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.messaging.queue.model.DeleteMessageResult;
import com.genexus.messaging.queue.model.MessageId;
import com.genexus.messaging.queue.model.SendMessageResult;
import com.genexus.messaging.queue.model.SimpleQueueMessage;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessage;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageResult;

import java.lang.reflect.Constructor;

public class SimpleMessageQueue {
	private IQueue queue;
	private static ILogger logger = LogManager.getLogger(SimpleMessageQueue.class);

	private static String SDT_MESSAGE_CLASS_NAME = "SdtMessage";
	private static String SDT_MESSAGEPROPERTY_CLASS_NAME = "SdtMessageProperty";
	private static String SDT_MESSAGERESULT_CLASS_NAME = "SdtMessageResult";
	private static String NAMESPACE = "GeneXus.Programs.genexusmessagingqueue.simplequeue";


	public SimpleMessageQueue() {

	}

	public SimpleMessageQueue(SimpleMessageQueue other) {
		queue = other.queue;
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
		}
		return queueLength;
	}

	public GxUserType sendMessage(SdtMessage sdtMessage, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, boolean[] success) {
		GXBaseCollection<SdtMessages_Message> errorMessages = errorMessagesArr[0];
		errorMessages.clear();

		SendMessageResult sendMessageResult = new SendMessageResult();
		errorMessages = new GXBaseCollection<SdtMessages_Message>();
		SimpleQueueMessage queueMessage = Convert.toSimpleQueueMessage(sdtMessage);
		try {
			validQueue();
			sendMessageResult = queue.sendMessage(queueMessage);
			success[0] = true;
			return Convert.toSdtMessageResult(sendMessageResult);

		} catch (Exception ex) {
			queueErrorMessagesSetup(ex, errorMessages);

			logger.error("Could not send queue message", ex);
		}

		return Convert.toSdtMessageResult(sendMessageResult);
	}

	public boolean deleteMessage(String messageHandleId, GXBaseCollection<SdtMessages_Message>[] errorMessagesArr, SdtMessageResult sdtDelete) {
		GXBaseCollection<SdtMessages_Message> errorMessages = errorMessagesArr[0];
		errorMessages.clear();

		try {
			validQueue();
			DeleteMessageResult deletedMessage = queue.deleteMessage(messageHandleId);
			sdtDelete.setgxTv_SdtMessageResult_Messageid(deletedMessage.getMessageId());
			sdtDelete.setgxTv_SdtMessageResult_Servermessageid(deletedMessage.getMessageServerId());
			sdtDelete.setgxTv_SdtMessageResult_Messagestatus(deletedMessage.getMessageDeleteStatus());
			return deletedMessage.getMessageDeleteStatus() == DeleteMessageResult.DELETED;
		} catch (Exception ex) {
			queueErrorMessagesSetup(ex, errorMessages);
			logger.error(String.format("Could not delete Message '%s' from Queue ", messageHandleId), ex);
		}
		return false;
	}

	protected void queueErrorMessagesSetup(Exception ex, GXBaseCollection<SdtMessages_Message> messages) {
		if (messages != null && ex != null) {
			StructSdtMessages_Message struct = new StructSdtMessages_Message();
			/*if (provider!=null && provider.getMessageFromException(ex, struct)) {
				struct.setDescription(ex.getMessage());
				struct.setType((byte) 1); //error
				SdtMessages_Message msg = new SdtMessages_Message(struct);
				messages.add(msg);
			} else {
				GXutil.ErrorToMessages("Storage Error", ex.getClass()+" " +ex.getMessage(), messages);
			}*/
		}
	}

}
