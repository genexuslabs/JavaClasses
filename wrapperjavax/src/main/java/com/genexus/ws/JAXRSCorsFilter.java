package com.genexus.ws;

import com.genexus.cors.CORSHelper;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;

@Provider
public class JAXRSCorsFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext,
					   ContainerResponseContext responseContext) {
		HashMap<String, String> corsHeaders = CORSHelper.getCORSHeaders();
		if (corsHeaders == null) {
			return;
		}
		for (String headerName : corsHeaders.keySet()) {
			if (!responseContext.getHeaders().containsKey(headerName)) {
				responseContext.getHeaders().add(headerName, corsHeaders.get(headerName));
			}
		}
	}
}
