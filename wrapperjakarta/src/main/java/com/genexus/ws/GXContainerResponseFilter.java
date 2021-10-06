package com.genexus.ws;

import java.io.IOException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import com.genexus.WrapperUtils;

@Provider
public class GXContainerResponseFilter implements ContainerResponseFilter{

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

		if (responseContext.getStatus() == 400 && responseContext.getEntity() instanceof String) {
			int statusCode[] = {400};
			String reasonPhrase = "Bad Request";
			String jsonString = WrapperUtils.getJsonFromRestExcpetion(statusCode, reasonPhrase, true, new Throwable((String)responseContext.getEntity()));
			responseContext.getHeaders().remove("Content-Type");
			responseContext.getHeaders().add("Content-Type", "application/json");
			responseContext.setEntity(jsonString);
		}
	}
}
