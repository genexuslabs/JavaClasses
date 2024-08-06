package com.genexus.cloud.azure.events;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.azure.handler.AzureEventGridCloudHandler;;
import com.microsoft.azure.functions.ExecutionContext;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import io.cloudevents.jackson.JsonFormat;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestAzureEventGridCloud {
	private AzureEventGridCloudHandler eventGridCloudFunction;
	private ExecutionContext context;

	@Test
	public void testEventGridCloudFunction() throws Exception {

		eventGridCloudFunction = new AzureEventGridCloudHandler();
		CloudEvent message = loadmessages();
		context = new MockExecutionContext("TestEventGridCloud","13e2d1f9-6838-4927-a6a8-0160e8601ab2");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JsonFormat.getCloudEventJacksonModule());
		String json = mapper.writeValueAsString(message);
		eventGridCloudFunction.run(json,context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");
	}

	private CloudEvent loadmessages()
	{
		String id = "ac6105de-9715-4753-9c80-4c07c4c8bda3";

		CloudEvent message1 = Mockito.mock(CloudEvent.class);
		CloudEventData mockData = Mockito.mock(CloudEventData.class);

		Mockito.when(mockData.toBytes()).thenReturn("{\"key\":\"1234\"}".getBytes());
		Mockito.when(message1.getData()).thenReturn(mockData);

		Mockito.when(message1.getType()).thenReturn("MyEventType");
		Mockito.when(message1.getSource()).thenReturn(URI.create("/my/source"));
		Mockito.when(message1.getId()).thenReturn(id);
		Mockito.when(message1.getSpecVersion()).thenReturn(SpecVersion.parse("1.0"));

		return message1;
	}

}
