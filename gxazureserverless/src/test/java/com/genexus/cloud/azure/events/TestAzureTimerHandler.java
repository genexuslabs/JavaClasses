package com.genexus.cloud.azure.events;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.azure.handler.AzureQueueHandler;
import com.genexus.cloud.serverless.azure.handler.AzureTimerHandler;
import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestAzureTimerHandler {

	private AzureTimerHandler timerFunction;
	private ExecutionContext context;

	@BeforeEach
	public void setup() throws Exception {
		timerFunction = new AzureTimerHandler();
	}
	@Test
	public void testTimerTriggerFunctionRaw() throws Exception {

		AzureTimerHandler.TimerScheduleStatus timerScheduleStatus = new AzureTimerHandler.TimerScheduleStatus();
		timerScheduleStatus.setLast("2025-01-01T00:00:00.000Z");
		timerScheduleStatus.setNext("2025-01-01T00:00:00.000Z");
		timerScheduleStatus.setLastUpdated("2025-01-01T00:00:00.000Z");

		AzureTimerHandler.TimerSchedule timerSchedule = new AzureTimerHandler.TimerSchedule(true);
		AzureTimerHandler.TimerObject timerObject = new AzureTimerHandler.TimerObject(timerScheduleStatus,false,timerSchedule);

		context = new MockExecutionContext("TestTimerRaw","845579bc-e081-46da-8397-daa17e32e269");
		timerFunction.run(new ObjectMapper().writeValueAsString(timerObject),context);

		assertNotNull(context.getLogger());
		context.getLogger().info("Logger is not null");

	}
}
