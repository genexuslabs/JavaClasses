package com.genexus.messaging.queue;

import com.genexus.GXBaseCollection;

import com.genexus.messaging.queue.model.DeleteMessageResult;
import com.genexus.messaging.queue.model.MessageQueueOptions;
import com.genexus.messaging.queue.model.SendMessageResult;
import com.genexus.messaging.queue.model.SimpleQueueMessage;
import com.genexus.util.GXProperties;
import com.genexus.util.GXProperty;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessage;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageOptions;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageResult;

import java.util.List;

public class Convert {

	protected static SimpleQueueMessage toSimpleQueueMessage(SdtMessage msg) {
		return new SimpleQueueMessage() {{
			String id = msg.getgxTv_SdtMessage_Messageid();
			setMessageId((id.isEmpty())? java.util.UUID.randomUUID().toString() :id);
			setMessageBody(msg.getgxTv_SdtMessage_Messagebody());
			setMessageHandleId(msg.getgxTv_SdtMessage_Messagehandleid());
			if (msg.getgxTv_SdtMessage_Messageattributes() != null) {
				setMessageAttributes(toGXProperties(msg.getgxTv_SdtMessage_Messageattributes()));
			}
		}};
	}

	protected static MessageQueueOptions toMessageQueueOptions(SdtMessageOptions receiveOptions) {
		MessageQueueOptions mqOptions = new MessageQueueOptions() {{
			setMaxNumberOfMessages(receiveOptions.getgxTv_SdtMessageOptions_Maxnumberofmessages());
			setWaitTimeout(receiveOptions.getgxTv_SdtMessageOptions_Waittimeout());
			setTimetoLive(receiveOptions.getgxTv_SdtMessageOptions_Timetolive());
			setDelaySeconds(receiveOptions.getgxTv_SdtMessageOptions_Delayseconds());
			setVisibilityTimeout(receiveOptions.getgxTv_SdtMessageOptions_Visibilitytimeout());
		}};
		return mqOptions;
	}


	protected static SdtMessageResult toSdtMessageResult(SendMessageResult mResult) {
		SdtMessageResult r = new SdtMessageResult();
		r.setgxTv_SdtMessageResult_Messageid(mResult.getMessageId());
		r.setgxTv_SdtMessageResult_Servermessageid(mResult.getMessageServerId());
		r.setgxTv_SdtMessageResult_Messagestatus(mResult.getMessageSentStatus());
		return r;
	}

	protected static GXBaseCollection<SdtMessageProperty> toSdtMessagePropertyCollection(GXProperties msgProps) {
		GXBaseCollection<SdtMessageProperty> props = new GXBaseCollection<SdtMessageProperty>();
		for (int i = 0; i < msgProps.count(); i++) {
			GXProperty propertyItem = msgProps.item(i);
			SdtMessageProperty msgProperty = new SdtMessageProperty();
			msgProperty.setgxTv_SdtMessageProperty_Propertykey(propertyItem.getKey());
			msgProperty.setgxTv_SdtMessageProperty_Propertyvalue(propertyItem.getValue());
			props.add(msgProperty);
		}
		return props;
	}

	protected static GXProperties toGXProperties(GXBaseCollection<SdtMessageProperty> msgProps) {
		GXProperties props = new GXProperties();
		for (SdtMessageProperty prop : msgProps) {
			props.add(prop.getgxTv_SdtMessageProperty_Propertykey(), prop.getgxTv_SdtMessageProperty_Propertyvalue());
		}
		return props;
	}

	public static SdtMessage toSdtMessage(SimpleQueueMessage simpleQueueMessage) {
		SdtMessage msg = new SdtMessage();
		msg.setgxTv_SdtMessage_Messageattributes(toSdtMessagePropertyCollection(simpleQueueMessage.getMessageAttributes()));
		msg.setgxTv_SdtMessage_Messagehandleid(simpleQueueMessage.getMessageHandleId());
		msg.setgxTv_SdtMessage_Messageid(simpleQueueMessage.getMessageId());
		msg.setgxTv_SdtMessage_Messagebody(simpleQueueMessage.getMessageBody());
		return msg;
	}

	public static GXBaseCollection<SdtMessageResult> toDeleteExternalMessageResultList(List<DeleteMessageResult> deletedMessages) {
		GXBaseCollection<SdtMessageResult> externalList = new GXBaseCollection<>();
		for (DeleteMessageResult deletedMessage : deletedMessages) {
			SdtMessageResult sdtMessageResult = new SdtMessageResult();
			sdtMessageResult.setgxTv_SdtMessageResult_Messageid(deletedMessage.getMessageId());
			sdtMessageResult.setgxTv_SdtMessageResult_Servermessageid(deletedMessage.getMessageServerId());
			sdtMessageResult.setgxTv_SdtMessageResult_Messagestatus(deletedMessage.getMessageDeleteStatus());
			externalList.add(sdtMessageResult);
		}
		return externalList;
	}

}
