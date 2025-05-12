package com.genexus.util.saia;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

class ContentSerializer extends JsonSerializer<OpenAIResponse.Content> {
	public ContentSerializer() {
	}

	@Override
	public void serialize(OpenAIResponse.Content value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		ObjectMapper mapper = (ObjectMapper) gen.getCodec();

		if (value instanceof OpenAIResponse.StringContent) {
			gen.writeString(((OpenAIResponse.StringContent) value).getValue());
		} else if (value instanceof OpenAIResponse.StructuredContent) {
			gen.writeStartArray();
			for (OpenAIResponse.StructuredContentItem item : ((OpenAIResponse.StructuredContent) value).getItems()) {
				mapper.writeValue(gen, item);
			}
			gen.writeEndArray();
		} else {
			gen.writeNull();
		}
	}
}
