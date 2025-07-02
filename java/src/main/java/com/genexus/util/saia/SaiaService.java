package com.genexus.util.saia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.SdtMessages_Message;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpClient;
import org.json.JSONObject;
import com.genexus.util.CallResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaiaService {
	private static final ILogger logger = LogManager.getLogger(SaiaService.class);
	private static final String apiKey = (String) SpecificImplementation.Application.getProperty("AI_PROVIDER_API_KEY", "");;
	private static final String aiProvider = (String) SpecificImplementation.Application.getProperty("AI_PROVIDER", "");
	private static final Logger log = LoggerFactory.getLogger(SaiaService.class);

	public static OpenAIResponse call(OpenAIRequest request, HttpClient client, CallResult result) {
		return call(request, false, client, result);
	}

	public static OpenAIResponse call(OpenAIRequest request, boolean isEmbedding, CallResult result) {
		return call(request, isEmbedding, new HttpClient(), result);
	}

	public static OpenAIResponse call(OpenAIRequest request, boolean isEmbedding, HttpClient client, CallResult result) {
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
			client.execute("POST", providerURL);
			if (client.getStatusCode() == 200) {
				if (client.getHeader("Content-Type").contains("text/event-stream")){
					return null;
				}
				else {
					String saiaResponse = client.getString();
					logger.debug("Agent response: " + saiaResponse);
					JSONObject jsonResponse = new JSONObject(saiaResponse);
					return new ObjectMapper().readValue(jsonResponse.toString(), OpenAIResponse.class);
				}
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
