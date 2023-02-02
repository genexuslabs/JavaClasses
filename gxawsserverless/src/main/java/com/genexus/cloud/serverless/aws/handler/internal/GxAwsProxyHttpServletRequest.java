package com.genexus.cloud.serverless.aws.handler.internal;

import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.SecurityContext;

public class GxAwsProxyHttpServletRequest extends AwsProxyHttpServletRequest {
	public GxAwsProxyHttpServletRequest(AwsProxyRequest awsProxyRequest, Context lambdaContext, SecurityContext awsSecurityContext) {
		super(awsProxyRequest, lambdaContext, awsSecurityContext);
	}

	@Override
	public HttpSession getSession(boolean b) {
		return super.getSession(b);
	}
}
