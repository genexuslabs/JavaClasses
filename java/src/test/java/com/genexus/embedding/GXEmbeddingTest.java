package com.genexus.embedding;

import com.genexus.Application;
import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.db.GXEmbedding;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class GXEmbeddingTest {
	@Test
	public void EmbeddingTest(){
		Connect.init();
		LogManager.initialize(".");
		Application.init(GXcfg.class);
		GXBaseCollection<SdtMessages_Message> AV7Messages = new GXBaseCollection<>();
		List<Float> embedding = GXEmbedding.getEmbedding("openai/text-embedding-3-small", 512, "Hello World", AV7Messages);
		String result = embedding.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
		System.out.println("Embedding: " + result);
		Assert.assertNotNull(embedding);

		GXBaseCollection<SdtMessages_Message> AV8Messages = new GXBaseCollection<>();
		GXEmbedding A7ProductEmbedding = new GXEmbedding("openai/text-embedding-3-small",512) ;
		GXEmbedding AV9ProductEmbedding = GXEmbedding.generateEmbedding(A7ProductEmbedding, "", AV8Messages) ;
		result = AV9ProductEmbedding.toString();
		System.out.println("Empty Embedding: " + result);
	}
}
