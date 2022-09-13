package com.genexus.cloud.serverless.aws.handler.internal;

import com.amazonaws.serverless.exceptions.InvalidRequestEventException;
import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.serverless.proxy.internal.servlet.AwsHttpApiV2ProxyHttpServletRequest;
import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import com.amazonaws.serverless.proxy.model.ContainerConfig;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

public class GxAwsHttpApiV2HttpServletRequestReader extends RequestReader<HttpApiV2ProxyRequest, HttpServletRequest> {
	static final String INVALID_REQUEST_ERROR = "The incoming event is not a valid HTTP API v2 proxy request";

	@Override
	public HttpServletRequest readRequest(HttpApiV2ProxyRequest request, SecurityContext securityContext, Context lambdaContext, ContainerConfig config) throws InvalidRequestEventException {
		if (request.getRequestContext() == null || request.getRequestContext().getHttp().getMethod() == null || request.getRequestContext().getHttp().getMethod().equals("")) {
			throw new InvalidRequestEventException(INVALID_REQUEST_ERROR);
		}

		// clean out the request path based on the container config
		request.setRawPath(stripBasePath(request.getRawPath(), config));

		AwsHttpApiV2ProxyHttpServletRequest servletRequest = new AwsHttpApiV2ProxyHttpServletRequest(request, lambdaContext, securityContext, config);

		AwsProxyRequestContext rContext = new AwsProxyRequestContext();
		rContext.setRequestId(request.getRequestContext().getRequestId());
		servletRequest.setAttribute(API_GATEWAY_CONTEXT_PROPERTY, rContext);
		servletRequest.setAttribute(HTTP_API_CONTEXT_PROPERTY, request.getRequestContext());
		servletRequest.setAttribute(HTTP_API_STAGE_VARS_PROPERTY, request.getStageVariables());
		servletRequest.setAttribute(HTTP_API_EVENT_PROPERTY, request);
		servletRequest.setAttribute(LAMBDA_CONTEXT_PROPERTY, lambdaContext);
		servletRequest.setAttribute(JAX_SECURITY_CONTEXT_PROPERTY, securityContext);

		return servletRequest;
	}

	@Override
	protected Class<? extends HttpApiV2ProxyRequest> getRequestClass() {
		return HttpApiV2ProxyRequest.class;
	}
}
