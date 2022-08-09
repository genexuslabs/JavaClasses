package com.genexus.cloud.serverless.aws.handler.internal;

import com.amazonaws.serverless.proxy.*;
import com.amazonaws.serverless.proxy.internal.servlet.*;
import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;

import javax.ws.rs.core.Application;

public class GxJerseyLambdaContainerHandlerFactory {
	public static JerseyLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> getHttpApiV2ProxyHandler(Application jaxRsApplication) {
		JerseyLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> newHandler = new JerseyLambdaContainerHandler<>(
			HttpApiV2ProxyRequest.class,
			AwsProxyResponse.class,
			new GxAwsHttpApiV2HttpServletRequestReader(),
			new AwsProxyHttpServletResponseWriter(true),
			new AwsHttpApiV2SecurityContextWriter(),
			new AwsProxyExceptionHandler(),
			jaxRsApplication);
		newHandler.initialize();
		return newHandler;
	}
}
