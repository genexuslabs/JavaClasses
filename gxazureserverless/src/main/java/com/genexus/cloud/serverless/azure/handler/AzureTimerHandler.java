package com.genexus.cloud.serverless.azure.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.ExecutionContext;
import java.time.Instant;
import java.util.List;
import java.util.*;

import com.genexus.cloud.serverless.model.*;

public class AzureTimerHandler extends AzureEventHandler {

	public AzureTimerHandler() throws Exception {
	}

	public void run(
		@TimerTrigger(name = "TimerInfo", schedule  = "%timer_schedule%") String TimerInfo,
		final ExecutionContext context) throws Exception {

		context.getLogger().info("GeneXus Timer trigger handler. Function processed: " + context.getFunctionName() + " Invocation Id: " + context.getInvocationId());
		setupServerlessMappings(context.getFunctionName());
		EventMessages msgs = new EventMessages();
		if (executor.getMethodSignatureIdx() == 0) {
			try {
				TimerObject timerObject = new ObjectMapper().readValue(TimerInfo, new TypeReference<TimerObject>() {
				});
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
			} catch (Exception e) {
				context.getLogger().severe("Message could not be processed.");
				throw e;
			}
		}
		ExecuteDynamic(msgs, TimerInfo);
	}

	public static class TimerObject{

		@JsonProperty("ScheduleStatus")
		TimerScheduleStatus timerScheduleStatus;

		@JsonProperty("IsPastDue")
		boolean IsPastDue;

		@JsonProperty("Schedule")
		TimerSchedule timerSchedule;

		public TimerObject() {
		}

		public TimerObject(TimerScheduleStatus timerScheduleStatus, boolean IsPastDue,TimerSchedule timerSchedule) {
			this.timerScheduleStatus = timerScheduleStatus;
			this.IsPastDue = IsPastDue;
			this.timerSchedule = timerSchedule;
		}

		protected boolean getIsPastDue() {
			return IsPastDue;
		}
	}

	public static class TimerSchedule{

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

	public static class TimerScheduleStatus {

		@JsonProperty("Last")
		String Last;

		@JsonProperty("Next")
		String Next;

		@JsonProperty("LastUpdated")
		String LastUpdated;

		public TimerScheduleStatus() {}

		public void setLast(String last) {
			Last = last;
		}
		public void setNext(String next) {
			Next = next;
		}
		public void setLastUpdated(String lastUpdated) {
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
