package com.genexus.embedding;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingService
{
	private static final ILogger logger = LogManager.getLogger(EmbeddingService.class);

	private static EmbeddingService instance;
	private final HttpClient client;
	private String apiKey;
	private String aiProvider;

	public EmbeddingService() {
		client = new HttpClient();
		aiProvider = (String) SpecificImplementation.Application.getProperty("AI_PROVIDER", "");
		apiKey = (String) SpecificImplementation.Application.getProperty("AI_PROVIDER_API_KEY", "");
	}
	public static EmbeddingService getInstance() {
		if (instance == null)
			instance = new EmbeddingService();
		return instance;
	}

	public List<Float> getEmbedding(String model, int dimensions, String input) {
		List<String> inputList = new ArrayList<>();
		inputList.add(input);
		return getEmbedding(model, dimensions, inputList);
	}

	public List<Float> getEmbedding(String model, int dimensions, List<String> inputList) {
		List<Float> embeddingsList = new ArrayList<>();
		try {
			String requestBody = String.format(
				"{\n" +
					"  \"model\": \"%s\",\n" +
					"  \"input\": %s,\n" +
					"  \"dimensions\": %d\n" +
					"}",
				model, new JSONArray(inputList), dimensions
			);
			client.setSecure(1);
			client.addHeader("Content-Type", "application/json");
			client.addHeader("Authorization", "Bearer " + apiKey);
			client.addHeader("X-Saia-Source", "Embedding");
			client.addString(requestBody);
			client.execute("POST", aiProvider);
			if (client.getStatusCode() == 200) {
				JSONObject jsonResponse = new JSONObject(client.getString());
				JSONArray embeddingsArray = jsonResponse.getJSONArray("data");

				for (int i = 0; i < embeddingsArray.length(); i++) {
					JSONArray embedding = ((JSONObject)embeddingsArray.get(0)).getJSONArray("embedding");;
					for (int j = 0; j < embedding.length(); j++) {
						embeddingsList.add((float) embedding.getDouble(j));
					}
				}
			}
			else {
				logger.error(String.format("Error calling embedding API, StatusCode: %d, ReasonLine: %s",
					client.getStatusCode(),
					client.getReasonLine()));
			}
		} catch (Exception e) {
			logger.error("GenerateEmbedding Error", e);
		}
		return embeddingsList;
	}
}
