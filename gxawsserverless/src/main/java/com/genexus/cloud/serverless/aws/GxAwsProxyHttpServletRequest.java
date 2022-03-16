package com.genexus.cloud.serverless.aws;

import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.ContainerConfig;
import com.amazonaws.services.lambda.runtime.Context;

import javax.ws.rs.core.SecurityContext;

public class GxAwsProxyHttpServletRequest extends AwsProxyHttpServletRequest {

	public GxAwsProxyHttpServletRequest(AwsProxyRequest awsProxyRequest, Context lambdaContext, SecurityContext awsSecurityContext) {
		super(awsProxyRequest, lambdaContext, awsSecurityContext);
	}

	public GxAwsProxyHttpServletRequest(AwsProxyRequest awsProxyRequest, Context lambdaContext, SecurityContext awsSecurityContext, ContainerConfig config) {
		super(awsProxyRequest, lambdaContext, awsSecurityContext, config);
	}
}
