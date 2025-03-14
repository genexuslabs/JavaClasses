package com.genexus.util.saia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.genexus.util.GXProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIRequest {

	@JsonProperty("model")
	private String model;

	@JsonProperty("messages")
	private ArrayList<OpenAIResponse.Message> messages;

	@JsonProperty("prompt")
	private String prompt;

	@JsonProperty("input")
	private ArrayList<String> input;

	@JsonProperty("max_tokens")
	private Integer maxTokens;

	@JsonProperty("temperature")
	private Double temperature;

	@JsonProperty("stream")
	private Boolean stream;

	@JsonProperty("stop")
	private ArrayList<String> stop;

	@JsonProperty("presence_penalty")
	private Double presencePenalty;

	@JsonProperty("frequency_penalty")
	private Double frequencyPenalty;

	@JsonProperty("user")
	private String user;

	@JsonProperty("variables")
	private ArrayList<GXProperty> variables;

	@JsonProperty("dimensions")
	private int dimension;

	public String getModel() { return model; }
	public void setModel(String model) { this.model = model; }

	public ArrayList<OpenAIResponse.Message> getMessages() { return messages; }
	public void setMessages(ArrayList<OpenAIResponse.Message> messages) { this.messages = messages; }

	public String getPrompt() { return prompt; }
	public void setPrompt(String prompt) { this.prompt = prompt; }

	public ArrayList<String> getInput() { return input; }
	public void setInput(ArrayList<String> input) { this.input = input; }

	public Integer getMaxTokens() { return maxTokens; }
	public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

	public Double getTemperature() { return temperature; }
	public void setTemperature(Double temperature) { this.temperature = temperature; }

	public Boolean getStream() { return stream; }
	public void setStream(Boolean stream) { this.stream = stream; }

	public ArrayList<String> getStop() { return stop; }
	public void setStop(ArrayList<String> stop) { this.stop = stop; }

	public Double getPresencePenalty() { return presencePenalty; }
	public void setPresencePenalty(Double presencePenalty) { this.presencePenalty = presencePenalty; }

	public Double getFrequencyPenalty() { return frequencyPenalty; }
	public void setFrequencyPenalty(Double frequencyPenalty) { this.frequencyPenalty = frequencyPenalty; }

	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }

	public ArrayList<GXProperty> getVariables() { return variables; }
	public void setVariables(ArrayList<GXProperty> variables) { this.variables = variables; }

	public int getDimension() { return dimension; }
	public void setDimension(int dimension) { this.dimension = dimension; }
}