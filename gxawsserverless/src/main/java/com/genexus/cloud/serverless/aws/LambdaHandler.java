package com.genexus.cloud.serverless.aws;

import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.serverless.proxy.internal.servlet.AwsHttpServletResponse;
import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletRequest;
import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletResponseWriter;
import com.amazonaws.serverless.proxy.internal.servlet.AwsServletContext;
import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.genexus.cloud.serverless.aws.handler.AwsGxServletResponse;
import com.genexus.cloud.serverless.aws.handler.LambdaApplicationHelper;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.LogManager;
import com.genexus.webpanels.GXOAuthAccessToken;
import com.genexus.webpanels.GXOAuthLogout;
import com.genexus.webpanels.GXOAuthUserInfo;
import com.genexus.webpanels.GXWebObjectStub;

import org.glassfish.jersey.server.ResourceConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {
	private static ILogger logger = null;
	public static JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler = null;
	private static ResourceConfig jerseyApplication = null;
	private static final String BASE_REST_PATH = "/rest/";

	public LambdaHandler() throws Exception {
		if (LambdaHandler.jerseyApplication == null) {
			JerseyLambdaContainerHandler.getContainerConfig().setDefaultContentCharset("UTF-8");
			logger = LogManager.initialize(".", LambdaHandler.class);
			LambdaHandler.jerseyApplication = ResourceConfig.forApplication(LambdaApplicationHelper.initialize());
			if (jerseyApplication.getClasses().size() == 0) {
				logger.error("No HTTP endpoints found for this application");
			}
			LambdaHandler.handler = JerseyLambdaContainerHandler.getAwsProxyHandler(LambdaHandler.jerseyApplication);
		}
	}

	@Override
	public AwsProxyResponse handleRequest(AwsProxyRequest awsProxyRequest, Context context) {
		if (logger.isDebugEnabled()) {
			dumpRequest(awsProxyRequest);
		}
		String path = awsProxyRequest.getPath();
		prepareSpecialMethods(awsProxyRequest);
		dumpRequest(awsProxyRequest);

		logger.debug("Before handle Request");

		awsProxyRequest.setPath(path.replace(BASE_REST_PATH, "/"));
		AwsProxyResponse response = this.handler.proxy(awsProxyRequest, context);

		//This code should be removed when GAM services get implemented via API Object.
		if (response.getStatusCode() == 404) {
			awsProxyRequest.setPath(path);
			logger.debug("Trying servlet request: " + path);
			AwsGxServletResponse servletResponse = handleServletRequest(awsProxyRequest, context);
			if (servletResponse.wasHandled()) {
				response = servletResponse.getAwsProxyResponse();
			}
		}

		int statusCode = response.getStatusCode();
		logger.debug("After handle Request - Status Code: " + statusCode);

		if (statusCode >= 404 && statusCode <= 499) {
			logger.warn(String.format("Request could not be handled (%d): %s", response.getStatusCode(), path));
		}
		return response;
	}

	private void prepareSpecialMethods(AwsProxyRequest awsProxyRequest) {
		String path = awsProxyRequest.getPath();

		if (path.startsWith("/gxmulticall")) { //Gxmulticall does not respect queryString standard, so we need to transform.
			String parmValue = awsProxyRequest.getQueryString().replace("?", "").replace("=", "");
			MultiValuedTreeMap<String, String> qString = new MultiValuedTreeMap<>();
			qString.add("", parmValue);
			awsProxyRequest.setMultiValueQueryStringParameters(qString);
		}

		if (awsProxyRequest.getMultiValueHeaders() == null) {
			return;
		}
		// In Jersey lambda context, the Referer Header has a special meaning. So we copy it to another Header.
		List<String> referer = awsProxyRequest.getMultiValueHeaders().get("Referer");
		if (referer != null && !referer.isEmpty()) {
			awsProxyRequest.getMultiValueHeaders().put("GX-Referer", referer);
		}
	}

	private AwsGxServletResponse handleServletRequest(AwsProxyRequest awsProxyRequest, Context context) {
		try {
			GXWebObjectStub servlet = resolveServlet(awsProxyRequest);
			if (servlet != null) {
				CountDownLatch latch = new CountDownLatch(0);
				ServletContext servletContext = new AwsServletContext(null);
				AwsProxyHttpServletRequest servletRequest = new AwsProxyHttpServletRequest(awsProxyRequest, context, null);
				servletRequest.setAttribute(RequestReader.API_GATEWAY_CONTEXT_PROPERTY, awsProxyRequest.getRequestContext());
				servlet.init(new ServletConfig() {
					@Override
					public String getServletName() {
						return "";
					}

					@Override
					public ServletContext getServletContext() {
						return servletContext;
					}

					@Override
					public String getInitParameter(String s) {
						return "";
					}

					@Override
					public Enumeration<String> getInitParameterNames() {
						return null;
					}
				});
				AwsHttpServletResponse response = new AwsHttpServletResponse(servletRequest, latch);
				servletRequest.setServletContext(servletContext);
				servlet.service(servletRequest, response);
				return new AwsGxServletResponse(new AwsProxyHttpServletResponseWriter().writeResponse(response, context));
			} else {
				return new AwsGxServletResponse(new AwsProxyResponse(404));
			}
		} catch (Exception e) {
			logger.error("Error processing servlet request", e);
		}
		return new AwsGxServletResponse();
	}

	private GXWebObjectStub resolveServlet(AwsProxyRequest awsProxyRequest) {
		//TODO: Use web.xml catalog to obtain Handler Class Name to instantiate.
		GXWebObjectStub handler = null;
		String path = awsProxyRequest.getPath();
		switch (path) {
			case "/oauth/access_token":
				handler = new GXOAuthAccessToken();
				break;
			case "/oauth/logout":
				handler = new GXOAuthLogout();
				break;
			case "/oauth/userinfo":
				handler = new GXOAuthUserInfo();
				break;
			default:
				logger.debug("Could not handle Servlet Path: " + path);
		}
		return handler;
	}

	private void dumpRequest(AwsProxyRequest awsProxyRequest) {
		String lineSeparator = System.lineSeparator();
		String reqData = String.format("Path: %s", awsProxyRequest.getPath()) + lineSeparator;
		reqData += String.format("Method: %s", awsProxyRequest.getHttpMethod()) + lineSeparator;
		reqData += String.format("QueryString: %s", awsProxyRequest.getQueryString()) + lineSeparator;
		reqData += String.format("Body: %sn", awsProxyRequest.getBody()) + lineSeparator;
		logger.debug(reqData);
	}
}

