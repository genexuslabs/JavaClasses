package com.genexus.ws;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JAXRSObjectMapper implements ContextResolver<ObjectMapper> {

	private final ObjectMapper mapper;

	public JAXRSObjectMapper() {
		this.mapper = createObjectMapper();
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}

	private ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		String bodyLengthLimit = System.getProperty("gx.jackson.bodylengthlimit");
		if (bodyLengthLimit != null)
			mapper.getFactory().setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(Integer.parseInt(bodyLengthLimit)).build());
		return mapper;
	}
}
