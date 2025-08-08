package com.genexus.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

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
		return mapper;
	}
}
