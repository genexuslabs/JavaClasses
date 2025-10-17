package com.genexus.util.saia;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIResponse {

	@JsonProperty("id")
	private String id;

	@JsonProperty("object")
	private String object;

	@JsonProperty("created")
	private long created;

	@JsonProperty("choices")
	private ArrayList<Choice> choices;

	@JsonProperty("usage")
	private Usage usage;

	@JsonProperty("tool_calls")
	private ArrayList<Message> tool_calls;

	@JsonProperty("data")
	private ArrayList<DataItem> data;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getObject() { return object; }
	public void setObject(String object) { this.object = object; }

	public long getCreated() { return created; }
	public void setCreated(long created) { this.created = created; }

	public ArrayList<Choice> getChoices() { return choices; }
	public void setChoices(ArrayList<Choice> choices) { this.choices = choices; }

	public Usage getUsage() { return usage; }
	public void setUsage(Usage usage) { this.usage = usage; }

	public ArrayList<Message> getToolCalls() { return tool_calls; }
	public void setToolCalls(ArrayList<Message> tool_calls) { this.tool_calls = tool_calls; }

	public ArrayList<DataItem> getData() { return data; }
	public void setData(ArrayList<DataItem> data) { this.data = data; }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Choice {

		@JsonProperty("index")
		private int index;

		@JsonProperty("message")
		private Message message;

		@JsonProperty("delta")
		private Message delta;

		@JsonProperty("finish_reason")
		private String finishReason;

		public int getIndex() { return index; }
		public void setIndex(int index) { this.index = index; }

		public Message getMessage() { return message; }
		public void setMessage(Message message) { this.message = message; }

		public Message getDelta() { return message; }
		public void setDelta(Message message) { this.message = message; }

		public String getFinishReason() { return finishReason; }
		public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Message {

		@JsonProperty("role")
		private String role;

		@JsonProperty("content")
		@JsonSerialize(using = ContentSerializer.class)
		@JsonDeserialize(using = ContentDeserializer.class)
		private Content content;

		@JsonProperty("tool_calls")
		private ArrayList<ToolCall> toolCalls;

		@JsonProperty("tool_call_id")
		private String toolCallId;

		public String getRole() { return role; }
		public void setRole(String role) { this.role = role; }

		@JsonIgnore
		public String getStringContent() {
			if (content != null)
				return ((StringContent) content).getValue();

			return null;
		}
		@JsonIgnore
		public StructuredContent getStructuredContent() {return (StructuredContent)content;}
		public Content getContent() { return content; }
		@JsonIgnore
		public void setStringContent(String content) { this.content = new StringContent(content); }
		@JsonIgnore
		public void setStructuredContent(StructuredContent content) { this.content = content; }
		public void setContent(Content content) { this.content = content; }

		public ArrayList<ToolCall> getToolCalls() { return toolCalls; }
		public void setToolCalls(ArrayList<ToolCall> toolCalls) { this.toolCalls = toolCalls; }

		public String getToolCallId() { return toolCallId; }
		public void setToolCallId(String toolCallId) { this.toolCallId = toolCallId; }
	}

	public interface Content { }

	public static class StringContent implements Content {
		public StringContent() {}

		private String value;

		public StringContent(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class StructuredContent implements Content {
		public StructuredContent() {}

		private ArrayList<StructuredContentItem> items;

		public StructuredContent(ArrayList<StructuredContentItem> items) {
			this.items = items;
		}

		public ArrayList<StructuredContentItem> getItems() {
			return items;
		}

		public void setItems(ArrayList<StructuredContentItem> items) {
			this.items = items;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class StructuredContentItem {
		public StructuredContentItem() {}

		private String type;
		private String text;
		private ImageUrl image_url;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public ImageUrl getImage_url() {
			return image_url;
		}

		public void setImage_url(ImageUrl imageURL) {
			this.image_url = imageURL;
		}

		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class ImageUrl {
			public ImageUrl() {}

			private String url;
			private String detail;

			public String getUrl() {
				return url;
			}

			public void setUrl(String url) {
				this.url = url;
			}

			public String getDetail() {
				return detail;
			}
			
			public void setDetail(String detail) {
				this.detail = detail;
			}
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ToolCall {

		@JsonProperty("id")
		private String id;

		@JsonProperty("type")
		private String type;

		@JsonProperty("function")
		private Function function;

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }

		public String getType() { return type; }
		public void setType(String type) { this.type = type; }

		public Function getFunction() { return function; }
		public void setFunction(Function function) { this.function = function; }
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Function {

		@JsonProperty("name")
		private String name;

		@JsonProperty("arguments")
		private String arguments;

		public String getName() { return name; }
		public void setName(String name) { this.name = name; }

		public String getArguments() { return arguments; }
		public void setArguments(String arguments) { this.arguments = arguments; }
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
		private ArrayList<Double> embedding;

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }

		public String getObject() { return object; }
		public void setObject(String object) { this.object = object; }

		public ArrayList<Double> getEmbedding() { return embedding; }
		public void setEmbedding(ArrayList<Double> embedding) { this.embedding = embedding; }
	}
}
