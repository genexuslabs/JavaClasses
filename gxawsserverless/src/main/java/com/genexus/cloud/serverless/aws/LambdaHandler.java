package com.genexus.cloud.serverless.aws;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;

import com.amazonaws.serverless.proxy.internal.servlet.AwsHttpServletResponse;
import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletRequest;
import com.amazonaws.serverless.proxy.internal.servlet.AwsServletContext;
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap;
import com.genexus.specific.java.LogManager;
import com.genexus.webpanels.GXWebObjectStub;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.genexus.ApplicationContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.util.IniFile;
import com.genexus.webpanels.*;

import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletResponseWriter;

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {
	private static ILogger logger = null;
	public static JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler = null;
	private static ResourceConfig jerseyApplication = null;
	private static final String BASE_REST_PATH = "/rest/";

	public LambdaHandler() throws Exception {
		if (LambdaHandler.jerseyApplication == null) {
			JerseyLambdaContainerHandler.getContainerConfig().setDefaultContentCharset("UTF-8");
			LambdaHandler.jerseyApplication = ResourceConfig.forApplication(initialize());
			if (jerseyApplication.getClasses().size() == 0) {
				String errMsg = "No endpoints found for this application";
				logger.error(errMsg);
				throw new Exception(errMsg);
			}
			LambdaHandler.handler = JerseyLambdaContainerHandler.getAwsProxyHandler(LambdaHandler.jerseyApplication);
		}
	}


	@Override
	public AwsProxyResponse handleRequest(AwsProxyRequest awsProxyRequest, Context context) {
		dumpRequest(awsProxyRequest);
		String path = awsProxyRequest.getPath();
		awsProxyRequest.setPath(path.replace(BASE_REST_PATH, "/"));
		handleSpecialMethods(awsProxyRequest);
		dumpRequest(awsProxyRequest);

		logger.debug("Before handle Request");
		AwsProxyResponse response = this.handler.proxy(awsProxyRequest, context);
		int statusCode = response.getStatusCode();
		logger.debug("After handle Request - Status Code: " + statusCode);

		if (statusCode >= 400 && statusCode <= 599) {
			logger.warn(String.format("Request could not be handled (%d): %s", response.getStatusCode(), path));
		}
		return response;
	}

	private void handleSpecialMethods(AwsProxyRequest awsProxyRequest) {
		if (awsProxyRequest.getPath().startsWith("/gxmulticall")) { //Gxmulticall does not respect queryString standard, so we need to transform.
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

	private AwsProxyResponse handleServletRequest(AwsProxyRequest awsProxyRequest, Context context) {
		try {
			GXWebObjectStub servlet = resolveServlet(awsProxyRequest);
			if (servlet != null) {
				CountDownLatch latch = new CountDownLatch(0);
				ServletContext servletContext = new AwsServletContext(null);//AwsServletContext.getInstance(lambdaContext, null);
				AwsProxyHttpServletRequest servletRequest = new AwsProxyHttpServletRequest(awsProxyRequest, context, null);
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
				return new AwsProxyHttpServletResponseWriter().writeResponse(response, context);
			} else {
				return new AwsProxyResponse(404);
			}
		} catch (Exception e) {
			logger.error("Error processing servlet request", e);
		}
		return new AwsProxyResponse(500);
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
				logger.error("Could not handle Servlet Path: " + path);
		}
		return handler;
	}

	private static Application initialize() throws Exception {
		logger = LogManager.initialize(".", LambdaHandler.class);
		IniFile config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", null);
		String className = config.getProperty("Client", "PACKAGE", null);
		Class<?> cls;
		try {
			cls = Class.forName(className + ".GXApplication");
			Application app = (Application) cls.newInstance();
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

	private void dumpRequest(AwsProxyRequest awsProxyRequest){
		String lineSeparator = System.lineSeparator();
		String reqData = String.format("Path: %s", awsProxyRequest.getPath()) + lineSeparator;
		reqData += String.format("Method: %s", awsProxyRequest.getHttpMethod()) + lineSeparator;
		reqData += String.format("QueryString: %s", awsProxyRequest.getQueryString()) + lineSeparator;
		reqData += String.format("Body: %sn", awsProxyRequest.getBody()) + lineSeparator;
		logger.debug(reqData);
	}
}