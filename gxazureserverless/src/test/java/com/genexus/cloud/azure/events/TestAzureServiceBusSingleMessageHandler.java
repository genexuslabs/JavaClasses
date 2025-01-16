package com.genexus.cloud.azure.events;

import com.genexus.cloud.serverless.azure.handler.AzureServiceBusQueueSingleMsgHandler;

import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestAzureServiceBusSingleMessageHandler {
	private AzureServiceBusQueueSingleMsgHandler queueFunction;
	private ExecutionContext context;

	@BeforeEach
	public void setup() throws Exception {
		queueFunction = new AzureServiceBusQueueSingleMsgHandler();
	}

	@Test
	public void testSBTriggerFunctionRaw() throws Exception {

		String messageId = "ac6105de-9715-4753-9c80-4c07c4c8bda3";
		String enqueuedTimeUtc = "2025-01-01T12:34:56Z";
		String message = "Test message Service Bus Queue 1";

		context = new MockExecutionContext("TestServiceBusRaw","13e2d1f9-6838-4927-a6a8-0160e8601ab0");
		queueFunction.run(message,messageId,enqueuedTimeUtc,context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");
	}
	@Test
	public void testSBTriggerFunctionRaw2() throws Exception {

		String messageId = "ac6105de-9715-4753-9c80-4c07c4c8bda4";
		String enqueuedTimeUtc = "2025-01-01T12:34:56Z";
		String message = "{\"UserId\":\"d2376a4c-86c3-461f-93cc-1c2e0174222b\", \"UserName\":\"John\"}";

		context = new MockExecutionContext("TestServiceBusRaw","13e2d1f9-6838-4927-a6a8-0160e8601ab1");
		queueFunction.run(message,messageId,enqueuedTimeUtc,context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");
	}
	@Test
	public void testSBTriggerFunctionMessage() throws Exception {

		String messageId = "ac6105de-9715-4753-9c80-4c07c4c8bda5";
		String enqueuedTimeUtc = "2025-01-01T12:34:56Z";
		String message = "{\"UserId\":\"d2376a4c-86c3-461f-93cc-1c2e0174222a\", \"UserName\":\"Mary\"}";

		context = new MockExecutionContext("TestServiceBusMessage","13e2d1f9-6838-4927-a6a8-0160e8601ab1");
		queueFunction.run(message,messageId,enqueuedTimeUtc,context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");
	}
}
