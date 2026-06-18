package com.genexus.ws;

import com.genexus.cors.CORSHelper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;

@Provider
@PreMatching
public class JAXRSCorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) {
		String method = requestContext.getMethod();
		String origin = requestContext.getHeaderString(CORSHelper.ORIGIN_HEADER_NAME);
		String requestedMethod = requestContext.getHeaderString(CORSHelper.REQUEST_METHOD_HEADER_NAME);

		if (!CORSHelper.isPreflight(method, origin, requestedMethod)) {
			return;
		}
		String requestedHeaders = requestContext.getHeaderString(CORSHelper.REQUEST_HEADERS_HEADER_NAME);
		HashMap<String, String> corsHeaders = CORSHelper.getCORSHeaders(method, origin, requestedMethod, requestedHeaders);
		if (corsHeaders == null) {
			return;
		}
		Response.ResponseBuilder builder = Response.noContent();
		for (String headerName : corsHeaders.keySet()) {
			builder.header(headerName, corsHeaders.get(headerName));
		}
		requestContext.abortWith(builder.build());
	}

	@Override
	public void filter(ContainerRequestContext requestContext,
					   ContainerResponseContext responseContext) {
		HashMap<String, String> corsHeaders = CORSHelper.getCORSHeaders(requestContext.getMethod(), requestContext.getHeaders());
		if (corsHeaders == null) {
			return;
		}
		for (String headerName : corsHeaders.keySet()) {
			responseContext.getHeaders().putSingle(headerName, corsHeaders.get(headerName));
		}
	}
}
