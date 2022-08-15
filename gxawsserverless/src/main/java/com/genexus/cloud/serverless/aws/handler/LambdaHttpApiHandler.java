package com.genexus.cloud.serverless.aws.handler;

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequestContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.genexus.cloud.serverless.aws.LambdaHandler;
import com.genexus.cloud.serverless.aws.handler.internal.GxJerseyLambdaContainerHandlerFactory;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.LogManager;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Map;

public class LambdaHttpApiHandler implements RequestHandler<HttpApiV2ProxyRequest, AwsProxyResponse> {
	private static final String BASE_REST_PATH = "/rest/";
	public static JerseyLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler = null;
	private static ILogger logger = null;
	private static ResourceConfig jerseyApplication = null;

	public LambdaHttpApiHandler() throws Exception {
		if (LambdaHttpApiHandler.jerseyApplication == null) {
			JerseyLambdaContainerHandler.getContainerConfig().setDefaultContentCharset("UTF-8");
			logger = LogManager.initialize(".", LambdaHandler.class);
			LambdaHttpApiHandler.jerseyApplication = ResourceConfig.forApplication(LambdaHelper.initialize());

			if (jerseyApplication.getClasses().size() == 0) {
				logger.error("No HTTP endpoints found for this application");
			}

			handler = GxJerseyLambdaContainerHandlerFactory.getHttpApiV2ProxyHandler(LambdaHttpApiHandler.jerseyApplication);
		}
	}

	@Override
	public AwsProxyResponse handleRequest(HttpApiV2ProxyRequest awsProxyRequest, Context context) {
		if (logger.isDebugEnabled()) {
			dumpRequest(awsProxyRequest);
		}

		String path = awsProxyRequest.getRawPath();
		prepareRequest(awsProxyRequest);

		logger.debug("Before handle Request");

		awsProxyRequest.setRawPath(path.replace(BASE_REST_PATH, "/"));
		AwsProxyResponse response = handler.proxy(awsProxyRequest, context);

		int statusCode = response.getStatusCode();
		logger.debug("After handle Request - Status Code: " + statusCode);

		if (statusCode >= 404 && statusCode <= 599) {
			logger.warn(String.format("Request could not be handled (%d): %s", response.getStatusCode(), path));
		}
		return response;
	}

	private void prepareRequest(HttpApiV2ProxyRequest awsProxyRequest) {
		Map<String, String> headers = awsProxyRequest.getHeaders();

		if (headers == null) {
			return;
		}

		// In Jersey lambda context, the Referer Header has a special meaning. So we copy it to another Header.
		String referer = headers.get("Referer");
		if (referer != null && !referer.isEmpty()) {
			headers.put("GX-Referer", referer);
		}
	}

	private void dumpRequest(HttpApiV2ProxyRequest awsProxyRequest) {
		String lineSeparator = System.lineSeparator();
		String reqData = String.format("Path: %s", awsProxyRequest.getRawPath()) + lineSeparator;
		reqData += String.format("Method: %s", awsProxyRequest.getRequestContext().getHttp().getMethod()) + lineSeparator;
		reqData += String.format("QueryString: %s", awsProxyRequest.getRawQueryString()) + lineSeparator;
		reqData += String.format("Body: %sn", awsProxyRequest.getBody()) + lineSeparator;
		logger.debug(reqData);
	}

}