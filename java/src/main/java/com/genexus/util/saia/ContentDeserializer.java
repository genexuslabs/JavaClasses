package com.genexus.util.saia;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;

class ContentDeserializer extends JsonDeserializer<OpenAIResponse.Content> {
	public ContentDeserializer() {}

	@Override
	public OpenAIResponse.Content deserialize(JsonParser p, DeserializationContext ct) throws IOException {
		ObjectCodec codec = p.getCodec();
		JsonNode node = codec.readTree(p);

		if (node.isTextual()) {
			return new OpenAIResponse.StringContent(node.asText());
		} else if (node.isArray()) {
			ArrayList<OpenAIResponse.StructuredContentItem> items = new ArrayList<>();
			for (JsonNode itemNode : node) {
				OpenAIResponse.StructuredContentItem item = codec.treeToValue(itemNode, OpenAIResponse.StructuredContentItem.class);
				items.add(item);
			}
			return new OpenAIResponse.StructuredContent(items);
		}

		throw new IOException("Invalid content format");
	}
}
