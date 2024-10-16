package com.genexus.db;

import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.embedding.EmbeddingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GXEmbedding {

	private String model;
	private int dimensions;
	private List<Float> embedding;

	public GXEmbedding() {
	}

	public GXEmbedding(String model, int dimensions) {
		this.model = model;
		this.dimensions = dimensions;
		embedding = new ArrayList<>(dimensions);
	}

	public GXEmbedding(Float[] embedding, String model, int dimensions) {
		this.model = model;
		this.dimensions = dimensions;
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
		this.embedding = embedding;
	}

	public Float[] getFloatArray() {
		return embedding.toArray(new Float[0]);
	}

	public static GXEmbedding generateEmbedding(GXEmbedding embeddingInfo, String text, GXBaseCollection<SdtMessages_Message> Messages) {
		try {
			List<Float> embedding = EmbeddingService.getInstance().getEmbedding(embeddingInfo.getModel(), embeddingInfo.getDimensions(), text);
			embeddingInfo.setEmbedding(embedding);
		}
		catch (Exception ex) {
			CommonUtil.ErrorToMessages("GenerateEmbedding Error", ex.getMessage(), Messages);
		}
		return embeddingInfo;
	}

	public String toString()
	{
		return embedding.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
	}
}
