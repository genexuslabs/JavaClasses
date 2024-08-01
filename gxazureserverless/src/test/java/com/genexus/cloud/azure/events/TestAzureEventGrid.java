package com.genexus.cloud.azure.events;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.genexus.cloud.serverless.azure.handler.AzureEventGridHandler;
import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestAzureEventGrid {
	private AzureEventGridHandler eventGridFunction;
	private ExecutionContext context;

	@BeforeEach
	public void setup() throws Exception {
		eventGridFunction = new AzureEventGridHandler();
	}

	@Test
	public void testEventGridFunctionRaw() throws Exception {

		EventGridEvent message = loadmessages();
		context = new MockExecutionContext("TestEventGridRaw","13e2d1f9-6838-4927-a6a8-0160e8601ab5");
		eventGridFunction.run(message,context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");
	}

	private EventGridEvent loadmessages()
	{
		String id = "ac6105de-9715-4753-9c80-4c07c4c8bda3";
		long sequenceNumber = 1123456789;
		String subject = "testsubject1";
		String eventTime = "2025-01-01T00:00:00.000Z";
		String topic = "testtopic1";

		EventGridEvent message1 = Mockito.mock(EventGridEvent.class);
		Mockito.when(message1.getData()).thenReturn(BinaryData.fromBytes("Test message Event Grid 1".getBytes()));
		Mockito.when(message1.getId()).thenReturn(id);
		Mockito.when(message1.getEventTime()).thenReturn(OffsetDateTime.parse(eventTime));
		Mockito.when(message1.getTopic()).thenReturn(topic);
		Mockito.when(message1.getSubject()).thenReturn(subject);

		return message1;
	}
}
