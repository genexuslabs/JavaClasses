package com.genexus.xml.ws;

public class WebServiceContext {
	javax.xml.ws.WebServiceContext webServiceContext;

	public WebServiceContext(javax.xml.ws.WebServiceContext webServiceContext) {
		this.webServiceContext = webServiceContext;
	}

	public javax.xml.ws.handler.MessageContext getMessageContext() {
		return webServiceContext.getMessageContext();
	}
}
