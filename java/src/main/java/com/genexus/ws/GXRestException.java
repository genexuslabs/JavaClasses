package com.genexus.ws;

import com.genexus.Application;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GXRestException extends Throwable implements ExceptionMapper<Throwable>
{
	private static final long serialVersionUID = 1L;
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GXRestException.class);

	@Override
	public Response toResponse(Throwable ex)
	{
		int statusCode = 500;
		String reasonPhrase = "Internal Server Error";
		if (ex instanceof WebApplicationException)
		{
			statusCode = ((WebApplicationException)ex).getResponse().getStatus();
			reasonPhrase = ((WebApplicationException)ex).getResponse().getStatusInfo().getReasonPhrase();
		}
		else if (ex instanceof com.fasterxml.jackson.core.JsonProcessingException)
		{
			statusCode = 400;
			reasonPhrase = "Bad Request";
		}
		log.error("Error executing REST service", ex);
		JSONObject errorJson = new JSONObject();
		try
		{
			JSONObject obj = new JSONObject();
			obj.put("code", statusCode);
			obj.put("message", reasonPhrase);
			errorJson.put("error", obj);
		}
		catch(JSONException e)
		{
			log.error("Invalid JSON", e);
		}
		return Response.status(statusCode).entity(errorJson.toString()).type("application/json").build();
	}
}
