package com.genexus.xml.ws;

public class WebServiceContext {
	jakarta.xml.ws.WebServiceContext webServiceContext;

	public WebServiceContext(jakarta.xml.ws.WebServiceContext webServiceContext) {
		this.webServiceContext = webServiceContext;
	}

	public jakarta.xml.ws.handler.MessageContext getMessageContext() {
		return webServiceContext.getMessageContext();
	}
}
