package com.genexus.util.saia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.GXProcedure;
import com.genexus.SdtMessages_Message;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpClient;
import com.genexus.util.ChatResult;
import com.genexus.util.GXProperties;
import org.json.JSONObject;
import com.genexus.util.CallResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class SaiaService {
	private static final ILogger logger = LogManager.getLogger(SaiaService.class);
	private static final String apiKey = (String) SpecificImplementation.Application.getProperty("AI_PROVIDER_API_KEY", "");;
	private static final String aiProvider = (String) SpecificImplementation.Application.getProperty("AI_PROVIDER", "");
	private static final Logger log = LoggerFactory.getLogger(SaiaService.class);

	public static OpenAIResponse call(GXProcedure proc, OpenAIRequest request, HttpClient client, String agent, boolean stream, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result, ChatResult chatResult) {
		return call(proc, request, false, client, agent, stream, properties, messages, result, chatResult);
	}

	public static OpenAIResponse call(OpenAIRequest request, boolean isEmbedding, CallResult result) {
		return call(null, request, isEmbedding, new HttpClient(), null, false, null, null, result, null);
	}

	public static OpenAIResponse call(GXProcedure proc, OpenAIRequest request, boolean isEmbedding, HttpClient client, String agent, boolean stream, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result, ChatResult chatResult) {
		try {
			String jsonRequest = new ObjectMapper().writeValueAsString(request);
			logger.debug("Agent payload: " + jsonRequest);
			String providerURL = aiProvider + "/chat";;

			client.setSecure(1);
			client.addHeader("Content-Type", "application/json");
			client.addHeader("Authorization", "Bearer " + apiKey);
			if (isEmbedding) {
				client.addHeader("X-Saia-Source", "Embedding");
				providerURL = providerURL + "/embedding";
			}

			client.addString(jsonRequest);
			client.setTimeout(600);
			client.execute("POST", providerURL);
			if (client.getStatusCode() == 200) {
				String saiaResponse;
				if (client.getHeader("Content-Type").contains("text/event-stream")){
					getChunkedSaiaResponse(proc, client, agent, stream, properties, messages, result, chatResult);
					return null;
				}
				else {
					saiaResponse = client.getString();
				}

				logger.debug("Agent response: " + saiaResponse);
				JSONObject jsonResponse = new JSONObject(saiaResponse);
				return new ObjectMapper().readValue(jsonResponse.toString(), OpenAIResponse.class);
			}
			else {
				String errorDescription = String.format("Error calling Enterprise AI API, StatusCode: %d, ReasonLine: %s",
					client.getStatusCode(),
					client.getReasonLine());
				addResultMessage("SAIA_ERROR_CALL", (byte)1, errorDescription, result);
				logger.error(errorDescription);
				logger.debug("Agent error response: " + client.getString());
			}
		}
		catch (Exception e) {
			addResultMessage("SAIA_EXCEPTION", (byte)1, e.getMessage(), result);
			logger.error("Calling Enterprise AI API Error", e);
		}
		return null;
	}

	private static void getChunkedSaiaResponse(GXProcedure proc, HttpClient client, String agent, boolean stream, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result, ChatResult chatResult) {
		String saiaChunkResponse = client.readChunk();;
		String chunkJson;
		while (!client.getEof()) {
			logger.debug("Agent response chunk: " + saiaChunkResponse);
			if (saiaChunkResponse.isEmpty() || saiaChunkResponse.equals("data: [DONE]")) {
				saiaChunkResponse = client.readChunk();
				continue;
			}
			int index = saiaChunkResponse.indexOf("data:") + "data:".length();
			chunkJson = saiaChunkResponse.substring(index).trim();
			try {
				JSONObject jsonResponse = new JSONObject(chunkJson);
				OpenAIResponse chunkResponse = new ObjectMapper().readValue(jsonResponse.toString(), OpenAIResponse.class);
				if (!chunkResponse.getChoices().isEmpty()) {
					OpenAIResponse.Choice choice = chunkResponse.getChoices().get(0);
					if (choice.getFinishReason() != null && choice.getFinishReason().equals("tool_calls")) {
						messages.add(choice.getMessage());
						proc.processNotChunkedResponse(agent, stream, properties, messages, result, chatResult, choice.getMessage().getToolCalls());
						;
					} else if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
						chatResult.addChunk(((OpenAIResponse.StringContent) choice.getDelta().getContent()).getValue());
					}
				}
				saiaChunkResponse = client.readChunk();
			}
			catch (Exception e) {
				logger.warn("Error deserializing the response chunk", e);
				saiaChunkResponse = client.readChunk();
			}
		}
	}

	private static void addResultMessage(String id, byte type, String description, CallResult result){
		if (type == 1)
			result.setFail();
		SdtMessages_Message msg = new SdtMessages_Message();
		msg.setgxTv_SdtMessages_Message_Id(id);
		msg.setgxTv_SdtMessages_Message_Type(type);
		msg.setgxTv_SdtMessages_Message_Description(description);
		result.addMessage(msg);
	}
}
