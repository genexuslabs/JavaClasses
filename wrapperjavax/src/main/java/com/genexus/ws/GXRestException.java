package com.genexus.ws;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.genexus.WrapperUtils;

@Provider
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

		String jsonString = WrapperUtils.getJsonFromRestExcpetion(statusCode, reasonPhrase, applicationException, ex);
		return Response.status(statusCode[0]).entity(jsonString).type("application/json").build();
	}
}
