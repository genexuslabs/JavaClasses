package com.genexus.cloud.serverless.azure.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.genexus.cloud.serverless.model.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.ExecutionContext;

import java.time.Instant;
import java.util.List;
import java.util.*;


public class AzureTimerHandler extends AzureEventHandler {

	public AzureTimerHandler() throws Exception {
	}

	public void run(
		@TimerTrigger(name = "TimerInfo", schedule  = "%timer_schedule%") String TimerInfo,
		final ExecutionContext context) throws Exception {

		context.getLogger().info("GeneXus Timer trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());

		try {
			TimerObject timerObject = new ObjectMapper().readValue(TimerInfo, new TypeReference<TimerObject>(){});
			EventMessages msgs = new EventMessages();
			EventMessage msg = new EventMessage();
			msg.setMessageId(context.getInvocationId());
			msg.setMessageSourceType(EventMessageSourceType.TIMER);

			Instant nowUtc = Instant.now();
			msg.setMessageDate(Date.from(nowUtc));

			List<EventMessageProperty> msgAtts = msg.getMessageProperties();

			msgAtts.add(new EventMessageProperty("Id", context.getInvocationId()));
			boolean adjustForSDT = timerObject.timerSchedule.getAdjustForDST();
			msgAtts.add(new EventMessageProperty("AdjustForDST", Boolean.toString(adjustForSDT)));

			msgAtts.add(new EventMessageProperty("Next", timerObject.timerScheduleStatus.getNext()));
			msgAtts.add(new EventMessageProperty("Last", timerObject.timerScheduleStatus.getLast()));
			msgAtts.add(new EventMessageProperty("LastUpdated", timerObject.timerScheduleStatus.getLastUpdated()));

			boolean isPastDue = timerObject.getIsPastDue();
			msgAtts.add(new EventMessageProperty("IsPastDue", Boolean.toString(isPastDue)));

			msgs.add(msg);


			SetupServerlessMappings(context.getFunctionName());
			EventMessageResponse response = dispatchEvent(msgs, TimerInfo);

			if (response.hasFailed()) {
				logger.error(String.format("Messages were not handled. Error: %s", response.getErrorMessage()));
				throw new RuntimeException(response.getErrorMessage()); //Throw the exception so the runtime can Retry the operation.
			}

		}
		catch (Exception e) {
			logger.error("Message was not handled.");
			throw e;
		}
	}
	private static class TimerObject{

		@JsonProperty("ScheduleStatus")
		TimerScheduleStatus timerScheduleStatus;

		@JsonProperty("IsPastDue")
		boolean IsPastDue;

		@JsonProperty("Schedule")
		TimerSchedule timerSchedule;

		protected TimerObject() {
		}

		protected TimerObject(TimerScheduleStatus timerScheduleStatus, boolean IsPastDue,TimerSchedule timerSchedule) {
			this.timerScheduleStatus = timerScheduleStatus;
			this.IsPastDue = IsPastDue;
			this.timerSchedule = timerSchedule;
		}

		protected boolean getIsPastDue() {
			return IsPastDue;
		}
	}

	private static class TimerSchedule{

		@JsonProperty("AdjustForDST")
		boolean AdjustForDST;

		public TimerSchedule() {}

		public TimerSchedule(boolean AdjustForDST) {
			this.AdjustForDST = AdjustForDST;
		}
		protected boolean getAdjustForDST() {
			return AdjustForDST;
		}
	}

	private static class TimerScheduleStatus {

		@JsonProperty("Last")
		String Last;

		@JsonProperty("Next")
		String Next;

		@JsonProperty("LastUpdated")
		String LastUpdated;

		public TimerScheduleStatus() {}

		protected void setLast(String last) {
			Last = last;
		}
		protected void setNext(String next) {
			Next = next;
		}
		protected void setLastUpdated(String lastUpdated) {
			LastUpdated = lastUpdated;
		}

		protected String getLastUpdated() {
			return LastUpdated;
		}
		protected String getNext() {
			return Next;
		}
		protected String getLast() {
			return Last;
		}
	}
}
