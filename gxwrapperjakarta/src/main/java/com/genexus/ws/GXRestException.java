package com.genexus.ws;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import com.genexus.WrapperUtils;
import jakarta.annotation.Priority;

@Provider
@Priority(2)
public class GXRestException extends Throwable implements ExceptionMapper<Throwable>
{
	private static final long serialVersionUID = 1L;

	@Override
	public Response toResponse(Throwable ex)
	{
		int statusCode[] = {0};
		String reasonPhrase = "";
		boolean applicationException = false;
		if (ex instanceof WebApplicationException)
		{
			statusCode[0] = ((WebApplicationException)ex).getResponse().getStatus();
			reasonPhrase = ((WebApplicationException)ex).getResponse().getStatusInfo().getReasonPhrase();
			applicationException = true;
		}
		
		String jsonString = WrapperUtils.getJsonFromRestException(statusCode, reasonPhrase, applicationException, ex);
		return Response.status(statusCode[0]).entity(jsonString).type("application/json").build();
	}
}
