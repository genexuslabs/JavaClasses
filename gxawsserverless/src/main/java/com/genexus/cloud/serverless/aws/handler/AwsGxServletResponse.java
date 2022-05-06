package com.genexus.cloud.serverless.aws;

import com.amazonaws.serverless.proxy.model.AwsProxyResponse;

public class AwsGxServletResponse {
	private AwsProxyResponse response;

	public AwsGxServletResponse(AwsProxyResponse response){
		this.response = response;
	}
	public AwsGxServletResponse(){
	}

	public boolean wasHandled(){
		return response != null;
	}

	public AwsProxyResponse getAwsProxyResponse() {
		return response;
	}
}
