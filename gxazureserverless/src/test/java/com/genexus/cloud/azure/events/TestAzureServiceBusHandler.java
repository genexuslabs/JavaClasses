package com.genexus.cloud.azure.events;
import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.genexus.cloud.serverless.azure.handler.AzureServiceBusQueueHandler;
import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestAzureServiceBusHandler {
	private AzureServiceBusQueueHandler sbqueueFunction;
	private ExecutionContext context;

	@BeforeEach
	public void setup() throws Exception {
		sbqueueFunction = new AzureServiceBusQueueHandler();
	}

	@Test
	public void testSBTriggerFunctionRaw() throws Exception {

		List<ServiceBusReceivedMessage> messages = loadmessages();

		context = new MockExecutionContext("TestServiceBusRaw","13e2d1f9-6838-4927-a6a8-0160e8601ab0");
		sbqueueFunction.run(messages,context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");
	}
	@Test
	public void testSBTriggerFunctionMessage() throws Exception {

		List<ServiceBusReceivedMessage> messages = loadmessages();
		context = new MockExecutionContext("TestServiceBusMessage","13e2d1f9-6838-4927-a6a8-0160e8601ab1");
		sbqueueFunction.run(messages,context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");

	}
	private List<ServiceBusReceivedMessage> loadmessages()
	{
		String id = "ac6105de-9715-4753-9c80-4c07c4c8bda3";
		long sequenceNumber = 1123456789;
		String subject = "testsubject1";

		List<ServiceBusReceivedMessage> messages = new ArrayList<>();
		ServiceBusReceivedMessage message1 = Mockito.mock(ServiceBusReceivedMessage.class);
		Mockito.when(message1.getBody()).thenReturn(BinaryData.fromBytes("Test message Service Bus Queue 1".getBytes()));
		Mockito.when(message1.getMessageId()).thenReturn(id);
		Mockito.when(message1.getContentType()).thenReturn("application/octet-stream");
		Mockito.when(message1.getSequenceNumber()).thenReturn(sequenceNumber);
		Mockito.when(message1.getSubject()).thenReturn(subject);
		messages.add(message1);

		id = "ac6105de-9715-4753-9c80-4c07c4c8bda4";
		sequenceNumber = 1123456780;
		subject = "testsubject2";
		ServiceBusReceivedMessage message2 = Mockito.mock(ServiceBusReceivedMessage.class);
		Mockito.when(message2.getBody()).thenReturn(BinaryData.fromBytes("Test message Service Bus Queue 2".getBytes()));
		Mockito.when(message2.getMessageId()).thenReturn(id);
		Mockito.when(message2.getContentType()).thenReturn("application/octet-stream");
		Mockito.when(message2.getSequenceNumber()).thenReturn(sequenceNumber);
		Mockito.when(message2.getSubject()).thenReturn(subject);

		messages.add(message2);
		return messages;
	}
}
