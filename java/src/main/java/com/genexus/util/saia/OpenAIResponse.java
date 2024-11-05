package com.genexus.util.saia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("object")
	private String object;

	@JsonProperty("created")
	private long created;

	@JsonProperty("choices")
	private List<Choice> choices;

	@JsonProperty("usage")
	private Usage usage;

	@JsonProperty("data")
	private List<DataItem> data;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getObject() { return object; }
	public void setObject(String object) { this.object = object; }

	public long getCreated() { return created; }
	public void setCreated(long created) { this.created = created; }

	public List<Choice> getChoices() { return choices; }
	public void setChoices(List<Choice> choices) { this.choices = choices; }

	public Usage getUsage() { return usage; }
	public void setUsage(Usage usage) { this.usage = usage; }

	public List<DataItem> getData() { return data; }
	public void setData(List<DataItem> data) { this.data = data; }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Choice {

		@JsonProperty("index")
		private int index;

		@JsonProperty("message")
		private Message message;

		@JsonProperty("finish_reason")
		private String finishReason;

		public int getIndex() { return index; }
		public void setIndex(int index) { this.index = index; }

		public Message getMessage() { return message; }
		public void setMessage(Message message) { this.message = message; }

		public String getFinishReason() { return finishReason; }
		public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Message {

		@JsonProperty("role")
		private String role;

		@JsonProperty("content")
		private String content;

		@JsonProperty("tool_calls")
		private List<ToolCall> toolCalls;

		public String getRole() { return role; }
		public void setRole(String role) { this.role = role; }

		public String getContent() { return content; }
		public void setContent(String content) { this.content = content; }

		public List<ToolCall> getToolCalls() { return toolCalls; }
		public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ToolCall {

		@JsonProperty("tool_name")
		private String toolName;

		@JsonProperty("tool_input")
		private ToolInput toolInput;

		@JsonProperty("tool_output")
		private String toolOutput;

		public String getToolName() { return toolName; }
		public void setToolName(String toolName) { this.toolName = toolName; }

		public ToolInput getToolInput() { return toolInput; }
		public void setToolInput(ToolInput toolInput) { this.toolInput = toolInput; }

		public String getToolOutput() { return toolOutput; }
		public void setToolOutput(String toolOutput) { this.toolOutput = toolOutput; }
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ToolInput {

		@JsonProperty("prompt")
		private String prompt;

		@JsonProperty("size")
		private String size;

		public String getPrompt() { return prompt; }
		public void setPrompt(String prompt) { this.prompt = prompt; }

		public String getSize() { return size; }
		public void setSize(String size) { this.size = size; }
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Usage {

		@JsonProperty("prompt_tokens")
		private int promptTokens;

		@JsonProperty("completion_tokens")
		private int completionTokens;

		@JsonProperty("total_tokens")
		private int totalTokens;

		public int getPromptTokens() { return promptTokens; }
		public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }

		public int getCompletionTokens() { return completionTokens; }
		public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }

		public int getTotalTokens() { return totalTokens; }
		public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DataItem {

		@JsonProperty("id")
		private String id;

		@JsonProperty("object")
		private String object;

		@JsonProperty("embedding")
		private List<Double> embedding;

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }

		public String getObject() { return object; }
		public void setObject(String object) { this.object = object; }

		public List<Double> getEmbedding() { return embedding; }
		public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
	}
}
