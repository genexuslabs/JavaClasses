package com.genexus.embedding;

import com.genexus.Application;
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
		EmbeddingService service = EmbeddingService.getInstance();
		List<Float> embedding = service.getEmbedding("openai/text-embedding-3-small", 512, "Hello World");
		String result = embedding.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
		System.out.println("Embedding: " + result);
		Assert.assertNotNull(embedding);
	}
}
