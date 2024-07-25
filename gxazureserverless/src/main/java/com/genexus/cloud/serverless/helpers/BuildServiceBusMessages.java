package com.genexus.cloud.serverless.helpers;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.genexus.cloud.serverless.model.EventMessage;
import com.genexus.cloud.serverless.model.EventMessageProperty;
import com.genexus.cloud.serverless.model.EventMessageSourceType;
import com.genexus.cloud.serverless.model.EventMessages;

import java.util.Date;
import java.util.List;

public class BuildServiceBusMessages {

	public static EventMessages setupServiceBusMessages(List<ServiceBusReceivedMessage> messages) {
		EventMessages msgs = new EventMessages();

		for (ServiceBusReceivedMessage message : messages) {

			EventMessage msg = new EventMessage();
			msg.setMessageId(message.getMessageId());
			msg.setMessageSourceType(EventMessageSourceType.SERVICE_BUS_MESSAGE);

			msg.setMessageDate(new Date());
			msg.setMessageData(message.getBody().toString());

			List<EventMessageProperty> msgAtts = msg.getMessageProperties();

			msgAtts.add(new EventMessageProperty("Id", message.getMessageId()));
			msgAtts.add(new EventMessageProperty("Content-Type", message.getContentType()));
			msgAtts.add(new EventMessageProperty("SessionId", message.getSessionId()));
			msgAtts.add(new EventMessageProperty("Subject", message.getSubject()));
			msgAtts.add(new EventMessageProperty("SequenceNumber", String.valueOf(message.getSequenceNumber())));
			msgAtts.add(new EventMessageProperty("EnqueuedSequenceNumber", String.valueOf(message.getEnqueuedSequenceNumber())));
			msgAtts.add(new EventMessageProperty("CorrelationId", message.getCorrelationId()));
			msgAtts.add(new EventMessageProperty("DeliveryCount", String.valueOf(message.getDeliveryCount())));

			msgAtts.add(new EventMessageProperty("TimeToLive", String.valueOf(message.getTimeToLive())));
			msgAtts.add(new EventMessageProperty("EnqueuedTime", String.valueOf(message.getEnqueuedTime())));
			msgAtts.add(new EventMessageProperty("ExpiresAt", String.valueOf(message.getExpiresAt())));
			msgAtts.add(new EventMessageProperty("LockedUntil", String.valueOf(message.getLockedUntil())));
			msgAtts.add(new EventMessageProperty("LockToken", message.getLockToken()));

			msgAtts.add(new EventMessageProperty("DeadLetterErrorDescription", message.getDeadLetterErrorDescription()));
			msgAtts.add(new EventMessageProperty("DeadLetterReason", message.getDeadLetterReason()));
			msgAtts.add(new EventMessageProperty("DeadLetterSource", message.getDeadLetterSource()));

			msgAtts.add(new EventMessageProperty("PartitionKey", message.getPartitionKey()));
			msgAtts.add(new EventMessageProperty("To", message.getTo()));
			msgAtts.add(new EventMessageProperty("ReplyTo", message.getReplyTo()));
			msgAtts.add(new EventMessageProperty("ReplyToSessionId", message.getReplyToSessionId()));

			msgAtts.add(new EventMessageProperty("State", String.valueOf(message.getState())));

			msgs.add(msg);

		}	return msgs;
	}
}
