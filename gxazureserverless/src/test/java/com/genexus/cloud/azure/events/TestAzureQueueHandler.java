package com.genexus.cloud.azure.events;

import com.genexus.cloud.serverless.azure.handler.AzureQueueHandler;
import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestAzureQueueHandler {
	private AzureQueueHandler queueFunction;
	private ExecutionContext context;

	@BeforeEach
	public void setup() throws Exception {
		queueFunction = new AzureQueueHandler();
	}

	@Test
	public void testQueueTriggerFunctionRaw() throws Exception {
		String testMessage = "Test message Queue";
		String id = "ac6105de-9715-4753-9c80-4c07c4c8bda3";
		long dequeCount =2;
		String expirationTime = "2025-01-01T00:00:00.000Z";
		final String insertionTime = "2024-11-01T00:00:00.000Z";
		String nextVisibleTime = "2024-11-01T04:00:00.000Z";
		String popReceipt = "bc2a33d5-d481-4fc2-a1e7-a9f8e50e0281";

		context = new MockExecutionContext("TestQueueRaw","13e2d1f9-6838-4927-a6a8-0160e8601ab0");
		queueFunction.run(testMessage,id,dequeCount,expirationTime,insertionTime, nextVisibleTime,popReceipt,context);
		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");

	}
	@Test
	public void testQueueTriggerFunctionMessage() throws Exception {
		String testMessage = "{\"UserId\":\"d2376a4c-86c3-461f-93cc-1c2e0174222b\", \"UserName\":\"John\"}";
		String id = "ac6105de-9715-4753-9c80-4c07c4c8bda3";
		long dequeCount =2;
		String expirationTime = "2025-01-01T00:00:00.000Z";
		final String insertionTime = "2024-11-01T00:00:00.000Z";
		String nextVisibleTime = "2024-11-01T04:00:00.000Z";
		String popReceipt = "bc2a33d5-d481-4fc2-a1e7-a9f8e50e0281";

		context = new MockExecutionContext("TestQueueEventMessage","758093bf-68c1-47a5-8f93-cc2882e961e7");
		queueFunction.run(testMessage,id,dequeCount,expirationTime,insertionTime, nextVisibleTime,popReceipt,context);
		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");
	}
}
