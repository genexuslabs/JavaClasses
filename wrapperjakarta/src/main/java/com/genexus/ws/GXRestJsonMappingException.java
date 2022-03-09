package com.genexus.ws;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.genexus.WrapperUtils;
import jakarta.annotation.Priority;

@Provider
@Priority(1)
public class GXRestJsonMappingException extends Throwable implements ExceptionMapper<JsonMappingException>
{
	private static final long serialVersionUID = 1L;

	@Override
	public Response toResponse(JsonMappingException ex)
	{
		int statusCode[] = {0};
		String jsonString = WrapperUtils.getJsonFromRestException(statusCode, "", false, ex);
		return Response.status(statusCode[0]).entity(jsonString).type("application/json").build();
	}
}