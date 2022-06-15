package com.genexus.messaging.queue;

import com.genexus.GXBaseCollection;

import com.genexus.messaging.queue.model.SendMessageResult;
import com.genexus.messaging.queue.model.SimpleQueueMessage;
import com.genexus.util.GXProperties;
import com.genexus.util.GXProperty;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessage;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty;
import com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageResult;

public class Convert {

	protected static SimpleQueueMessage toSimpleQueueMessage(SdtMessage msg) {
		return new SimpleQueueMessage() {{
			setMessageId(msg.getgxTv_SdtMessage_Messageid());
			setMessageBody(msg.getgxTv_SdtMessage_Messagebody());
			setMessageHandleId(msg.getgxTv_SdtMessage_Messagehandleid());
			if (msg.getgxTv_SdtMessage_Messageattributes() != null) {
				setMessageAttributes(toGXProperties(msg.getgxTv_SdtMessage_Messageattributes()));
			}
		}};
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
}
