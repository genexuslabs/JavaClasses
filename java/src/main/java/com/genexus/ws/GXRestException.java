package com.genexus.ws;

import com.genexus.Application;
import org.apache.logging.log4j.Logger;

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
		log.error("Error executing REST service", ex);
		return Response.status(500).entity(Application.getClientLocalUtil().getMessages().getMessage("GXM_runtimeappsrv")).type("text/plain").build();
	}
}
