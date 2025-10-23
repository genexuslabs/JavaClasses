package com.genexus.util.saia;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaiaEvents {
	private static final ILogger logger = LogManager.getLogger(SaiaEvents.class);

	@JsonProperty("event")
	private String event;

	@JsonProperty("data")
	private Data data;

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Data {

		@JsonProperty("toolId")
		private String toolId;

		@JsonProperty("toolName")
		private String toolName;

		@JsonProperty("timestamp")
		private String timestamp;

		@JsonProperty("toolDescription")
		private String toolDescription;

		@JsonProperty("toolStatus")
		private String toolStatus;

		@JsonProperty("toolResponse")
		private String toolResponse;

		@JsonProperty("executionId")
		private String executionId;

		public String getToolId() {
			return toolId;
		}

		public void setToolId(String toolId) {
			this.toolId = toolId;
		}

		public String getToolName() {
			return toolName;
		}

		public void setToolName(String toolName) {
			this.toolName = toolName;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public String getToolDescription() {
			return toolDescription;
		}

		public void setToolDescription(String toolDescription) {
			this.toolDescription = toolDescription;
		}

		public String getToolResponse() {
			return toolResponse;
		}

		public void setToolResponse(String toolResponse) {
			this.toolResponse = toolResponse;
		}

		public String getToolStatus() {
			return toolStatus;
		}

		public void setToolStatus(String toolStatus) {
			this.toolStatus = toolStatus;
		}

		public String getExecutionId() {
			return executionId;
		}

		public void setExecutionId(String executionId) {
			this.executionId = executionId;
		}
	}

	public String serializeSaiaEvent(OpenAIResponse.ToolCall toolCall, String event, String tollStatus, String result) {
		SaiaEvents.Data saiaToolStartedData = new SaiaEvents.Data();
		saiaToolStartedData.setToolName(toolCall.getFunction().getName());
		saiaToolStartedData.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
		saiaToolStartedData.setToolStatus(tollStatus);
		saiaToolStartedData.setToolResponse(result);
		saiaToolStartedData.setExecutionId(toolCall.getId());
		setEvent(event);
		setData(saiaToolStartedData);

		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		}
		catch (JsonProcessingException e) {
			logger.error("Serializing Saia Event", e);
			return null;
		}
	}
}
