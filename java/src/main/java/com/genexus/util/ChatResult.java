package com.genexus.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.GXProcedure;
import com.genexus.internet.HttpClient;
import com.genexus.util.saia.OpenAIResponse;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatResult {
	private HttpClient client = null;
	private String agent = null;
	private GXProperties properties = null;
	private ArrayList<OpenAIResponse.Message> messages = null;
	private CallResult result = null;
	private GXProcedure agentProcedure = null;

	public ChatResult() {
	}

	public ChatResult(GXProcedure agentProcedure, String agent, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result, HttpClient client) {
		this.agentProcedure = agentProcedure;
		this.agent = agent;
		this.properties = properties;
		this.messages = messages;
		this.result = result;
		this.client = client;
	}

	public boolean hasMoreData() {
		return !client.getEof();
	}

	public String getMoreData() {
		String data = client.readChunk();
		if (data.isEmpty())
			return "";
		int index = data.indexOf("data:") + "data:".length();
		String chunkJson = data.substring(index).trim();
		try {
			JSONObject jsonResponse = new JSONObject(chunkJson);
			OpenAIResponse chunkResponse = new ObjectMapper().readValue(jsonResponse.toString(), OpenAIResponse.class);
			OpenAIResponse.Choice choise = chunkResponse.getChoices().get(0);
			String chunkString = choise.getDelta().getStringContent();
			if (chunkString == null)
				return "";
			return chunkString;
		}
		catch (Exception e) {
			return "";
		}
	}
}
