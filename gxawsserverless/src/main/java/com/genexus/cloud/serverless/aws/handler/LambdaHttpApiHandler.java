package com.genexus.cloud.serverless.aws.handler;

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.genexus.ApplicationContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import com.genexus.util.IniFile;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.Application;
import java.util.Map;

public class LambdaHttpApiHandler implements RequestHandler<HttpApiV2ProxyRequest, AwsProxyResponse> {
	private static final String BASE_REST_PATH = "/rest/";
	private static final String GX_APPLICATION_CLASS = "GXApplication";
	public static JerseyLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler = null;
	private static ILogger logger = null;
	private static ResourceConfig jerseyApplication = null;

	public LambdaHttpApiHandler() throws Exception {
		if (LambdaHttpApiHandler.jerseyApplication == null) {
			JerseyLambdaContainerHandler.getContainerConfig().setDefaultContentCharset("UTF-8");
			LambdaHttpApiHandler.jerseyApplication = ResourceConfig.forApplication(initialize());
			if (jerseyApplication.getClasses().size() == 0) {
				String errMsg = "No endpoints found for this application";
				logger.error(errMsg);
				throw new Exception(errMsg);
			}
			LambdaHttpApiHandler.handler = JerseyLambdaContainerHandler.getHttpApiV2ProxyHandler(LambdaHttpApiHandler.jerseyApplication);
		}
	}

	private static Application initialize() throws Exception {
		logger = LogManager.initialize(".", LambdaHttpApiHandler.class);
		Connect.init();
		IniFile config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", null);
		String className = config.getProperty("Client", "PACKAGE", null);
		Class<?> cls;
		try {
			cls = Class.forName(className.isEmpty() ? GX_APPLICATION_CLASS : String.format("%s.%s", className, GX_APPLICATION_CLASS));
			Application app = (Application) cls.getDeclaredConstructor().newInstance();
			ApplicationContext appContext = ApplicationContext.getInstance();
			appContext.setServletEngine(true);
			appContext.setServletEngineDefaultPath("");
			com.genexus.Application.init(cls);
			return app;
		} catch (Exception e) {
			logger.error("Failed to initialize App", e);
			throw e;
		}
	}

	@Override
	public AwsProxyResponse handleRequest(HttpApiV2ProxyRequest awsProxyRequest, Context context) {
		if (logger.isDebugEnabled()) {
			dumpRequest(awsProxyRequest);
		}
		String path = awsProxyRequest.getRawPath();
		prepareSpecialMethods(awsProxyRequest);
		dumpRequest(awsProxyRequest);

		logger.debug("Before handle Request");

		awsProxyRequest.setRawPath(path.replace(BASE_REST_PATH, "/"));
		AwsProxyResponse response = handler.proxy(awsProxyRequest, context);

		int statusCode = response.getStatusCode();
		logger.debug("After handle Request - Status Code: " + statusCode);

		if (statusCode >= 404 && statusCode <= 499) {
			logger.warn(String.format("Request could not be handled (%d): %s", response.getStatusCode(), path));
		}
		return response;
	}

	private void prepareSpecialMethods(HttpApiV2ProxyRequest awsProxyRequest) {
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