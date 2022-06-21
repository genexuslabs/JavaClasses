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


import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.test.jersey.model.MapResponseModel;
import com.amazonaws.serverless.proxy.test.jersey.model.SingleValueModel;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.aws.LambdaHandler;
import com.genexus.specific.java.Connect;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit test class for the Jersey AWS_PROXY default implementation
 */
public class GeneXusAppAwsProxyTest {

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

	public GeneXusAppAwsProxyTest() {

	}

	private JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler = null;

	private static ObjectMapper objectMapper = new ObjectMapper();

	private static Context lambdaContext = new MockLambdaContext();


	@Test
	public void testGXDataProvider() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/Test", "GET")
			.queryString("Itemnumber", "9")
			.json()
			.header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(200, output.getStatusCode());
		assertEquals("{\"ItemId\":9,\"ItemName\":\"9 Item\"}", output.getBody());
	}

	@Test
	public void testGXDataProviderWithParams() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/Test/12", "GET")
			.queryString("Itemnumber", "9")
			.json()
			.header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(200, output.getStatusCode());
		assertEquals("{\"ItemId\":12,\"ItemName\":\"12 Item\"}", output.getBody());
	}

	@Test
	public void testGxMultiCall() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/gxmulticall", "POST")
			.queryString("", "receivenumber")
			.body("[[\"5\"],[\"6\"]]")
			.json()
			.header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(200, output.getStatusCode());
	}

	@Test
	public void testUpload() {
		AwsProxyRequest request = new AwsProxyRequestBuilder("/Test/gxobject", "POST")
			.body("iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAMAAAD04JH5AAAAkFBMVEVHcEzxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzTxYzX////94Nf/+PX5vKj3oITybEH0flf708f6ybn96uP2lnbzdUz1iWb4qpH83NH+8ez4sZnxYzScvitNAAAAHXRSTlMAR/DQIvbKjAjcVQS2/Oet1kyRG0IphxbBoT9taYkD32YAAAQCSURBVHhevNKLlYQgEETRVuQrKOCwzpwJovLPbhNYXcHGm0HVedRkSl+9vuMyyhDkuMT3qr9pokdsLiuJP40qu416MkO2ABBwzObBUBdz+oy4ZPykmbhNekGF5Ye3iJdHNf8iLk6giUjEIQk0UwPdVTxu8YXuMDrgpqANNRssGNjWGk0Gk9x0QolgIxpKcBKMpKM6cwYzTTWMB7u1IoRdoAO100WbRRdxo0smi07sVLefn73wwW7RUfy3AyPQlTJ0avbobKVTGd1pOuHwAEeHisQDZKEDJuIRwlQF8Eus3egmDsMAADalP+L6B1zFlW2YtrCNsQ2//9uddDfJ0hqxJE7rPMFHQ5z45xBw0f+1BeNakAWgHwIAqDFuwMoG0GEfALAybUJGNoAj4vEsBtDOcAJKWwB272JAOT4JBVkDEJ/PQgAVo/yHXADYDUIAfc+Zlm4AxJc3GSAdxWBHAHYXEYA2ow/gCEDsfQDmT9CQDwCPVwGAHoBX4QdA7AUAPggQky8AP87eAIo5CPoDEN99ARwO80oCwOc3T0CVcxCSAPBz8AJwMNoLAByVPAD7r3s4EgPw8+IDiHJ+iAgBiP3JEcB7sA0CwNerG4Cf6CsZgNfNGbD+l4xSKAB+XB0B9IcvQgHAPioZk5StAOAalYx5WhoUgN3gAmgBIAkLQHw52QMigJhCA/D1Yg2gGDYhARyVbAELeAwK4KhkCXiCbBIA4s0OkEEdGsBvJQvAoYZ2GoA5gyPDOVxPA+AM7j5gCZUE4BKVzIAKIgnAJSqZAREkEwE4Kp3uARIoJwYgDvcAJZS6X6CERPc/kECkewoiqHTjQAXrKSMh0c+BqFW9C6iFWvc2rCHTfQ9k8Kj7InqCje6bcAExab6K6TdApJkXRACQamZGLadmOrlhxsmpTnb8K2h6fjuQT3oOK70KyZpLNDo1ooyLVPIqGXkAGi7TqdQJo5wLlSqV0j2XalVqxbSQFas59kiL1bDT6RfsZA0L7pjIGxZQaPSMCmnTin++vGkFy/n7humocyvonMobl5DO3TtOR+MT83bPqfFt33/lXFJALRhgCDFBUcamEY75Zkho5z3E0g+HqYZYoKG55ojoQXmQKXOaJQwPWOaWw2zhAX+Lt6MUhkEgiqJjIo44gIZYlZA9uP/dFSjFr6bFaN/ZgPgjAu++PmIP+JzvAtfpEl0RD550kjWYUWuTA2bWix8246fdzR4w4/YmG8y8v7EeGziQjH+REjZyWVxH5rOOzHywoRML9YljUq+Dutl0P3ZLFpr7eU13KVO7mUgjbKbz+A0bvR40kk6g7LcR9Xv4rISmEMWhfhE4Cs2UHZflw80Lu0x/odXJvqzv/H8tnk+1U48n5FeadykPy1kAAAAASUVORK5CYII=")
			.header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
			.build();

		AwsProxyResponse output = handler.proxy(request, lambdaContext);
		assertEquals(201, output.getStatusCode());
		System.out.println(output.getBody());
	}


	@Test
	@Ignore
	public void gxTestOAuthAccessToken() {

		AwsProxyRequest request = new AwsProxyRequestBuilder("/oauth/access_token", "POST")
			.body("client_id=b0be5400435f42e588480fa06330f5ff&grant_type=password&username=ggallotti&password=gonzalo&scope=FullControl")
			.header("Content-Type", "application/x-www-form-urlencoded")
			//.header("Content-Length", "116")
			.build();

		AwsProxyResponse output = l.handleRequest(request, lambdaContext);
		System.out.println(output);
		//assertEquals(200, output.getStatusCode());

	}

}
