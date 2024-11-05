package com.genexus.util.saia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpClient;
import org.json.JSONObject;

public class SaiaService {
	private static final ILogger logger = LogManager.getLogger(SaiaService.class);
	private static final String apiKey = (String) SpecificImplementation.Application.getProperty("AI_PROVIDER_API_KEY", "");;
	private static final String aiProvider = (String) SpecificImplementation.Application.getProperty("AI_PROVIDER", "");

	public static OpenAIResponse call(OpenAIRequest request) {
		return call(request, false);
	}

	public static OpenAIResponse call(OpenAIRequest request, boolean isEmbedding) {
		try {
			String jsonRequest = new ObjectMapper().writeValueAsString(request);

			HttpClient client = new HttpClient();
			client.setSecure(1);
			client.addHeader("Content-Type", "application/json");
			client.addHeader("Authorization", "Bearer " + apiKey);
			if (isEmbedding)
				client.addHeader("X-Saia-Source", "Embedding");
			client.addString(jsonRequest);
			client.execute("POST", aiProvider);
			if (client.getStatusCode() == 200) {
				JSONObject jsonResponse = new JSONObject(client.getString());
				return new ObjectMapper().readValue(jsonResponse.toString(), OpenAIResponse.class);
			}
			else {
				logger.error(String.format("Error calling Enterprise AI API, StatusCode: %d, ReasonLine: %s",
					client.getStatusCode(),
					client.getReasonLine()));
			}
		}
		catch (Exception e) {
			logger.error("Calling Enterprise AI API Error", e);
		}
		return null;
	}
}
