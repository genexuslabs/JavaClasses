package com.genexus.cloud.serverless.aws.handler.internal;

import com.amazonaws.serverless.proxy.internal.servlet.AwsHttpApiV2ProxyHttpServletRequest;
import com.amazonaws.serverless.proxy.model.ContainerConfig;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.SecurityContext;

public class GxAwsHttpApiV2ProxyHttpServletRequest extends AwsHttpApiV2ProxyHttpServletRequest {
	/**
	 * Protected constructors for implementing classes. This should be called first with the context received from
	 * AWS Lambda
	 *
	 * @param req
	 * @param lambdaContext The Lambda function context. This object is used for utility methods such as log
	 * @param sc
	 * @param cfg
	 */
	public GxAwsHttpApiV2ProxyHttpServletRequest(HttpApiV2ProxyRequest req, Context lambdaContext, SecurityContext sc, ContainerConfig cfg) {
		super(req, lambdaContext, sc, cfg);
	}

	@Override
	public HttpSession getSession(boolean b) {
		return new GxAwsLambdaHttpSession();
	}
}
