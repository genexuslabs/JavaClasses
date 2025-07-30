package com.genexus.db;

import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.util.CallResult;
import com.genexus.util.saia.OpenAIRequest;
import com.genexus.util.saia.OpenAIResponse;
import com.genexus.util.saia.SaiaService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GXEmbedding {

	private String model;
	private int dimensions;
	private List<Float> embedding;

	public GXEmbedding() {
		embedding = new ArrayList<>();
	}

	public GXEmbedding(String model, int dimensions) {
		this.model = model;
		this.dimensions = dimensions;
		embedding = new ArrayList<>(Collections.nCopies(dimensions, 0.0f));
	}

	public GXEmbedding(Float[] embedding, String model, int dimensions) {
		this.model = model;
		this.dimensions = dimensions;
		if (embedding == null)
			this.embedding = new ArrayList<>(Collections.nCopies(dimensions, 0.0f));
		else
			this.embedding = Arrays.asList(embedding);
	}

	public GXEmbedding(List<Float> embedding) {
		this.embedding = embedding;
	}

	public String getModel() {
		return model;
	}

	public int getDimensions() {
		return dimensions;
	}

	public void setEmbedding(List<Float> embedding) {
		if (!embedding.isEmpty())
			this.embedding = embedding;
	}

	public Float[] getFloatArray() {
		return embedding.toArray(new Float[0]);
	}

	public static GXEmbedding generateEmbedding(GXEmbedding embeddingInfo, String text, GXBaseCollection<SdtMessages_Message> Messages) {
		try {
			List<Float> embedding = getEmbedding(embeddingInfo.getModel(), embeddingInfo.getDimensions(), text);
			embeddingInfo.setEmbedding(embedding);
		}
		catch (Exception ex) {
			CommonUtil.ErrorToMessages("GenerateEmbedding Error", ex.getMessage(), Messages);
		}
		return embeddingInfo;
	}

	public static List<Float> getEmbedding(String model, int dimensions, String input) {
		if (input.isEmpty())
			return new ArrayList<>();
		ArrayList<String> inputList = new ArrayList<>();
		inputList.add(input);
		return getEmbedding(model, dimensions, inputList);
	}

	public static List<Float> getEmbedding(String model, int dimensions, ArrayList<String> inputList) {
		OpenAIRequest aiRequest = new OpenAIRequest();
		aiRequest.setModel(model);
		aiRequest.setInput(inputList);
		aiRequest.setDimension(dimensions);
		OpenAIResponse aiResponse = SaiaService.call(aiRequest, true, new CallResult());
		if (aiResponse != null)
			return aiResponse.getData().get(0).getEmbedding().stream()
				.map(Double::floatValue)
				.collect(Collectors.toList());

		return new ArrayList<>();
	}

	public String toString()
	{
		return embedding.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
	}
}
