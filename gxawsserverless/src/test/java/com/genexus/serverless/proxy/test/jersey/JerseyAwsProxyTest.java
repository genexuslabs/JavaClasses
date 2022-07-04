/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.genexus.serverless.proxy.test.jersey;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.genexus.specific.java.Connect;
import com.genexus.webpanels.GXObjectUploadServices;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.internal.servlet.*;
import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.test.jersey.model.MapResponseModel;
import com.amazonaws.serverless.proxy.test.jersey.model.SingleValueModel;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.aws.LambdaHandler;
import org.junit.*;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import javax.servlet.*;

/**
 * Unit test class for the Jersey AWS_PROXY default implementation
 */
public class JerseyAwsProxyTest {

	private static final String CUSTOM_HEADER_KEY = "x-custom-header";
	private static final String CUSTOM_HEADER_VALUE = "my-custom-value";

	private ResourceConfig app;
	private LambdaHandler l;

	@Before
	public void setUpStreams() {
		Connect.init();

		try {
			System.setProperty("LAMBDA_TASK_ROOT", ".");
			l = new LambdaHandler();
			handler = LambdaHandler.handler;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JerseyAwsProxyTest() {

	}

	private JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler = null;

	private static ObjectMapper objectMapper = new ObjectMapper();

	private static Context lambdaContext = new MockLambdaContext();


	@Test
	public void headers_getHeaders_echo() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/echo/headers", "GET")
			.json()
			.header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(200, output.getStatusCode());
	}

	@Test
	public void headers_servletRequest_echo() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/echo/servlet-headers", "GET")
			.json()
			.header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(200, output.getStatusCode());
		validateMapResponseModel(output);
	}

	@Test
	public void queryString_uriInfo_echo() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/echo/query-string", "GET")
			.json()
			.queryString(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(200, output.getStatusCode());
		validateMapResponseModel(output);
	}


	@Test
	public void errors_unknownRoute_expect404() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/echo/test33", "GET").build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(404, output.getStatusCode());
	}

	@Test
	public void error_contentType_invalidContentType() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/echo/json-body", "POST")
			.header("Content-Type", "application/octet-stream")
			.body("asdasdasd")
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(415, output.getStatusCode());
	}

	@Test
	public void error_statusCode_methodNotAllowed() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/echo/status-code", "POST")
			.json()
			.queryString("status", "201")
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(405, output.getStatusCode());
	}

	@Test
	public void responseBody_responseWriter_validBody() throws JsonProcessingException {
		SingleValueModel singleValueModel = new SingleValueModel();
		singleValueModel.setValue(CUSTOM_HEADER_VALUE);
		AwsProxyRequest request = new AwsProxyRequestBuilder("/echo/json-body", "POST")
			.json()
			.body(objectMapper.writeValueAsString(singleValueModel))
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(200, output.getStatusCode());
		assertNotNull(output.getBody());

		validateSingleValueModel(output, CUSTOM_HEADER_VALUE);
	}

	@Test
	public void statusCode_responseStatusCode_customStatusCode() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/echo/status-code", "GET")
			.json()
			.queryString("status", "201")
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(201, output.getStatusCode());
	}


	private void validateMapResponseModel(AwsProxyResponse output) {
		try {
			MapResponseModel response = objectMapper.readValue(output.getBody(), MapResponseModel.class);
			assertNotNull(response.getValues().get(CUSTOM_HEADER_KEY));
			assertEquals(CUSTOM_HEADER_VALUE, response.getValues().get(CUSTOM_HEADER_KEY));
		} catch (IOException e) {
			fail("Exception while parsing response body: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void validateSingleValueModel(AwsProxyResponse output, String value) {
		try {
			SingleValueModel response = objectMapper.readValue(output.getBody(), SingleValueModel.class);
			assertNotNull(response.getValue());
			assertEquals(value, response.getValue());
		} catch (IOException e) {
			fail("Exception while parsing response body: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
