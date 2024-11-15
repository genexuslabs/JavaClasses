package com.genexus.util.saia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.genexus.util.GXProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIRequest {

	@JsonProperty("model")
	private String model;

	@JsonProperty("messages")
	private List<OpenAIResponse.Message> messages;

	@JsonProperty("prompt")
	private String prompt;

	@JsonProperty("input")
	private List<String> input;

	@JsonProperty("max_tokens")
	private Integer maxTokens;

	@JsonProperty("temperature")
	private Double temperature;

	@JsonProperty("stream")
	private Boolean stream;

	@JsonProperty("stop")
	private List<String> stop;

	@JsonProperty("presence_penalty")
	private Double presencePenalty;

	@JsonProperty("frequency_penalty")
	private Double frequencyPenalty;

	@JsonProperty("user")
	private String user;

	@JsonProperty("variables")
	private List<GXProperty> variables;

	@JsonProperty("dimensions")
	private int dimension;

	public String getModel() { return model; }
	public void setModel(String model) { this.model = model; }

	public List<OpenAIResponse.Message> getMessages() { return messages; }
	public void setMessages(List<OpenAIResponse.Message> messages) { this.messages = messages; }

	public String getPrompt() { return prompt; }
	public void setPrompt(String prompt) { this.prompt = prompt; }

	public List<String> getInput() { return input; }
	public void setInput(List<String> input) { this.input = input; }

	public Integer getMaxTokens() { return maxTokens; }
	public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

	public Double getTemperature() { return temperature; }
	public void setTemperature(Double temperature) { this.temperature = temperature; }

	public Boolean getStream() { return stream; }
	public void setStream(Boolean stream) { this.stream = stream; }

	public List<String> getStop() { return stop; }
	public void setStop(List<String> stop) { this.stop = stop; }

	public Double getPresencePenalty() { return presencePenalty; }
	public void setPresencePenalty(Double presencePenalty) { this.presencePenalty = presencePenalty; }

	public Double getFrequencyPenalty() { return frequencyPenalty; }
	public void setFrequencyPenalty(Double frequencyPenalty) { this.frequencyPenalty = frequencyPenalty; }

	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }

	public List<GXProperty> getVariables() { return variables; }
	public void setVariables(List<GXProperty> variables) { this.variables = variables; }

	public int getDimension() { return dimension; }
	public void setDimension(int dimension) { this.dimension = dimension; }
}